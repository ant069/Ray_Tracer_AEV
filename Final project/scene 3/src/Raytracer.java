import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Raytracer v1.0 – Recursive ray tracer with parallel rendering.
 *
 * Features:
 *   - Blinn-Phong shading with lighting
 *   - Hard shadows
 *   - Specular reflections (multiple bounces)
 *   - Refractions with Snell's Law
 *   - Anti-aliasing 2x2 supersampling
 *   - Multi-threaded rendering (one thread per scanline)
 */
public class Raytracer {
    private final Scene       scene;
    private final Camera      camera;
    private final PhongShader shader;
    private final int         maxBounces;
    private final double      epsilon;

    public Raytracer(Scene scene, Camera camera, int maxBounces) {
        this.scene      = scene;
        this.camera     = camera;
        this.maxBounces = Math.max(1, maxBounces);
        this.epsilon    = 1e-4;
        this.shader     = new PhongShader(0.28, 0.22, 32);
    }

    public Raytracer(Scene scene, Camera camera) {
        this(scene, camera, 4);
    }

    public BufferedImage render() {
        int w = camera.getWidth();
        int h = camera.getHeight();
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        double tNear = camera.getTNear();
        double tFar  = camera.getTFar();

        final int    AA     = 2;
        final double invAA2 = 1.0 / (AA * AA);

        // Warm up BVH before launching threads
        scene.intersect(new Ray(camera.getPosition(), new Vector3D(0, -1, 0)), 0, 1e-6);

        int numThreads = Runtime.getRuntime().availableProcessors();
        System.out.printf("[Raytracer] Rendering %dx%d | %d bounces | %dx%d AA | %d threads%n",
                          w, h, maxBounces, AA, AA, numThreads);

        ExecutorService pool = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(h);
        AtomicInteger done   = new AtomicInteger(0);

        for (int y = 0; y < h; y++) {
            final int row = y;
            pool.submit(() -> {
                for (int x = 0; x < w; x++) {
                    double rAcc = 0, gAcc = 0, bAcc = 0;
                    for (int sy = 0; sy < AA; sy++) {
                        for (int sx = 0; sx < AA; sx++) {
                            double px = x + (sx + 0.5) / AA;
                            double py = row + (sy + 0.5) / AA;
                            Ray   ray = camera.getRay(px, py);
                            Color col = traceRay(ray, tNear, tFar, 0);
                            rAcc += col.getRed();
                            gAcc += col.getGreen();
                            bAcc += col.getBlue();
                        }
                    }
                    img.setRGB(x, row, new Color(
                        clamp(rAcc * invAA2),
                        clamp(gAcc * invAA2),
                        clamp(bAcc * invAA2)
                    ).getRGB());
                }
                int n = done.incrementAndGet();
                if (n % 50 == 0 || n == h)
                    System.out.printf("  Scanline %d / %d%n", n, h);
                latch.countDown();
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        pool.shutdown();

        return img;
    }

    private Color traceRay(Ray ray, double tNear, double tFar, int depth) {
        if (depth > maxBounces) return scene.getBackgroundColor();

        Intersection hit = scene.intersect(ray, tNear, tFar);
        if (!hit.hit) return scene.getBackgroundColor();

        Material mat = hit.object.getMaterial();
        Vector3D V   = ray.direction.negate().normalize();

        Color baseColor = shader.shade(hit, scene.getLights(), scene, ray.direction);

        if (mat.isOpaque()) return baseColor;

        double r = baseColor.getRed()   / 255.0;
        double g = baseColor.getGreen() / 255.0;
        double b = baseColor.getBlue()  / 255.0;

        double reflectivity = mat.getReflectivity();
        double refractivity = mat.getRefractivity();

        if (reflectivity > 0.01) {
            Ray reflRay = reflectionRay(hit, V);
            Color rc = traceRay(reflRay, epsilon, tFar, depth + 1);
            r = lerp(r, rc.getRed()   / 255.0, reflectivity);
            g = lerp(g, rc.getGreen() / 255.0, reflectivity);
            b = lerp(b, rc.getBlue()  / 255.0, reflectivity);
        }

        if (refractivity > 0.01) {
            Ray refrRay = refractionRay(hit, V, mat.getIOR());
            if (refrRay == null) refrRay = reflectionRay(hit, V); // total internal reflection fallback
            Color rc = traceRay(refrRay, epsilon, tFar, depth + 1);
            r = lerp(r, rc.getRed()   / 255.0, refractivity);
            g = lerp(g, rc.getGreen() / 255.0, refractivity);
            b = lerp(b, rc.getBlue()  / 255.0, refractivity);
        }

        return new Color(clamp(r * 255), clamp(g * 255), clamp(b * 255));
    }

    private Ray reflectionRay(Intersection hit, Vector3D V) {
        Vector3D N = hit.normal;
        if (N.dot(V) < 0) N = N.negate();
        Vector3D D = V.negate();
        Vector3D R = D.subtract(N.multiply(2.0 * D.dot(N))).normalize();
        return new Ray(hit.point.add(N.multiply(epsilon)), R);
    }

    private Ray refractionRay(Intersection hit, Vector3D V, double iorMat) {
        Vector3D N    = hit.normal;
        double NdotV  = N.dot(V);
        double iorIn, iorOut;
        Vector3D Nout;

        if (NdotV > 0) {
            iorIn = 1.0; iorOut = iorMat; Nout = N;
        } else {
            iorIn = iorMat; iorOut = 1.0; Nout = N.negate(); NdotV = -NdotV;
        }

        double eta  = iorIn / iorOut;
        double disc = 1.0 - eta * eta * (1.0 - NdotV * NdotV);
        if (disc < 0) return null;

        Vector3D T = V.negate().multiply(eta).add(Nout.multiply(eta * NdotV - Math.sqrt(disc))).normalize();
        return new Ray(hit.point.add(Nout.multiply(-epsilon)), T);
    }

    private static double lerp(double a, double b, double t) { return a + t * (b - a); }
    private static int clamp(double v) { return (int) Math.min(255, Math.max(0, v)); }
}
