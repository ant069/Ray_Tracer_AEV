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
 *   - Post-processing: bloom, gamma correction (1.5), cinematic vignette
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
        // Ambient 0.16: close to original — enough to see the cave but dark.
        // SpecularStr 0.65: high for metallic car surfaces, chrome, brake discs.
        this.shader     = new PhongShader(0.16, 0.65, 32);
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

        System.out.println("[Raytracer] Applying post-processing (bloom, gamma 1.5, vignette)...");
        postProcess(img);

        return img;
    }

    /**
     * Post-processing pipeline:
     *   1. Bloom  — dramatic glow halo on the computer screens, platform LEDs,
     *               and SpotLight specular hot-spots on the Batmobile's chrome.
     *   2. Gamma  — 1.5 curve: reveals cave rock detail in shadows while
     *               preserving the high contrast of metallic surfaces.
     *   3. Vignette — focuses the eye on the Batmobile at the centre.
     */
    private void postProcess(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();

        float[] rBuf = new float[w * h];
        float[] gBuf = new float[w * h];
        float[] bBuf = new float[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                rBuf[y*w+x] = ((rgb >> 16) & 0xFF) / 255f;
                gBuf[y*w+x] = ((rgb >> 8)  & 0xFF) / 255f;
                bBuf[y*w+x] = ( rgb        & 0xFF) / 255f;
            }
        }

        // --- Bloom ---
        // Lower threshold (0.68) vs Scene 3: catches the coloured screen glow
        // AND the chrome/specular hot-spots on the Batmobile.
        final float BLOOM_THRESH   = 0.68f;
        final float BLOOM_STRENGTH = 0.40f;
        final int   bloomRadius    = Math.max(6, w / 130);

        float[] br = new float[w * h];
        float[] bg = new float[w * h];
        float[] bb = new float[w * h];
        for (int i = 0; i < w * h; i++) {
            float lum = 0.299f * rBuf[i] + 0.587f * gBuf[i] + 0.114f * bBuf[i];
            if (lum > BLOOM_THRESH) {
                float excess = (lum - BLOOM_THRESH) / (1f - BLOOM_THRESH);
                br[i] = rBuf[i] * excess;
                bg[i] = gBuf[i] * excess;
                bb[i] = bBuf[i] * excess;
            }
        }
        float[] tmp = new float[w * h];
        boxBlurH(br, tmp, w, h, bloomRadius); System.arraycopy(tmp, 0, br, 0, w*h);
        boxBlurH(bg, tmp, w, h, bloomRadius); System.arraycopy(tmp, 0, bg, 0, w*h);
        boxBlurH(bb, tmp, w, h, bloomRadius); System.arraycopy(tmp, 0, bb, 0, w*h);
        boxBlurV(br, tmp, w, h, bloomRadius); System.arraycopy(tmp, 0, br, 0, w*h);
        boxBlurV(bg, tmp, w, h, bloomRadius); System.arraycopy(tmp, 0, bg, 0, w*h);
        boxBlurV(bb, tmp, w, h, bloomRadius); System.arraycopy(tmp, 0, bb, 0, w*h);
        for (int i = 0; i < w * h; i++) {
            rBuf[i] += br[i] * BLOOM_STRENGTH;
            gBuf[i] += bg[i] * BLOOM_STRENGTH;
            bBuf[i] += bb[i] * BLOOM_STRENGTH;
        }

        // No gamma correction for this scene: the Batmobile must stay matte black.
        // Gamma lifts ALL values including the car body, making it read as grey.
        // Visual drama comes from bloom (screens, platform) not from gamma.
        final double INV_GAMMA = 1.0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int idx = y * w + x;
                double rv = Math.pow(Math.min(1.0, Math.max(0, rBuf[idx])), INV_GAMMA);
                double gv = Math.pow(Math.min(1.0, Math.max(0, gBuf[idx])), INV_GAMMA);
                double bv = Math.pow(Math.min(1.0, Math.max(0, bBuf[idx])), INV_GAMMA);
                double nx  = 2.0 * x / (w - 1) - 1.0;
                double ny  = 2.0 * y / (h - 1) - 1.0;
                double vig = 1.0 - 0.22 * (nx * nx + ny * ny);
                rv *= vig; gv *= vig; bv *= vig;
                img.setRGB(x, y, new Color(
                    clamp(rv * 255), clamp(gv * 255), clamp(bv * 255)
                ).getRGB());
            }
        }
    }

    private void boxBlurH(float[] src, float[] dst, int w, int h, int r) {
        float inv = 1f / (2 * r + 1);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float sum = 0;
                for (int dx = -r; dx <= r; dx++)
                    sum += src[y * w + Math.max(0, Math.min(w - 1, x + dx))];
                dst[y * w + x] = sum * inv;
            }
        }
    }

    private void boxBlurV(float[] src, float[] dst, int w, int h, int r) {
        float inv = 1f / (2 * r + 1);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                float sum = 0;
                for (int dy = -r; dy <= r; dy++)
                    sum += src[Math.max(0, Math.min(h - 1, y + dy)) * w + x];
                dst[y * w + x] = sum * inv;
            }
        }
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
            if (refrRay == null) refrRay = reflectionRay(hit, V); // TIR fallback
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
