import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Raytracer v0.9 – Ray tracing recursivo
 *
 * Características:
 *   - Phong shading con iluminación
 *   - Sombras duras
 *   - Reflexiones especulares (≥2 bounces)
 *   - Refracciones con Ley de Snell
 *   - Límite de profundidad recursiva
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
        this.maxBounces = Math.max(2, maxBounces);
        this.epsilon    = 1e-4;
        this.shader     = new PhongShader(0.08, 0.5, 32);
    }

    public Raytracer(Scene scene, Camera camera) {
        this(scene, camera, 4);  // Default: 4 bounces
    }

    public BufferedImage render() {
        int w = camera.getWidth();
        int h = camera.getHeight();
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        double tNear = camera.getTNear();
        double tFar  = camera.getTFar();

        System.out.printf("[Raytracer] Renderizando %dx%d con hasta %d bounces...%n", 
                          w, h, maxBounces);
        long t0 = System.currentTimeMillis();

        for (int y = 0; y < h; y++) {
            if (y % 50 == 0)
                System.out.printf("  Línea %d / %d%n", y, h);
            for (int x = 0; x < w; x++) {
                Ray ray = camera.getRay(x, y);
                Color col = traceRay(ray, tNear, tFar, 0);
                img.setRGB(x, y, col.getRGB());
            }
        }

        long elapsed = System.currentTimeMillis() - t0;
        System.out.printf("[Raytracer] Render completo en %.2f s%n", elapsed / 1000.0);
        return img;
    }

    /**
     * Traza un rayo recursivamente, integrando iluminación, reflexión y refracción
     */
    private Color traceRay(Ray ray, double tNear, double tFar, int depth) {
        if (depth > maxBounces) {
            return scene.getBackgroundColor();
        }

        Intersection hit = scene.intersect(ray, tNear, tFar);
        if (!hit.hit) return scene.getBackgroundColor();

        Material mat = hit.object.getMaterial();
        Vector3D V = ray.direction.negate().normalize();  // dirección hacia cámara

        // Base: iluminación Phong
        Color baseColor = shader.shade(hit, scene.getLights(), scene, ray.direction);

        // Si es completamente opaco, devolver solo iluminación
        if (mat.isOpaque()) {
            return baseColor;
        }

        // Inicializar color acumulado
        double r = baseColor.getRed()   / 255.0;
        double g = baseColor.getGreen() / 255.0;
        double b = baseColor.getBlue()  / 255.0;

        double reflectivity = mat.getReflectivity();
        double refractivity = mat.getRefractivity();

        // ── Reflexión ─────────────────────────────────────────────────────────
        if (reflectivity > 0.01) {
            Ray reflectionRay = createReflectionRay(hit, V);
            Color reflColor = traceRay(reflectionRay, epsilon, tFar, depth + 1);
            double rf = reflColor.getRed()   / 255.0;
            double gf = reflColor.getGreen() / 255.0;
            double bf = reflColor.getBlue()  / 255.0;

            r = lerp(r, rf, reflectivity);
            g = lerp(g, gf, reflectivity);
            b = lerp(b, bf, reflectivity);
        }

        // ── Refracción ────────────────────────────────────────────────────────
        if (refractivity > 0.01) {
            Ray refractionRay = createRefractionRay(hit, V, mat.getIOR());
            if (refractionRay != null) {
                Color refrColor = traceRay(refractionRay, epsilon, tFar, depth + 1);
                double rf = refrColor.getRed()   / 255.0;
                double gf = refrColor.getGreen() / 255.0;
                double bf = refrColor.getBlue()  / 255.0;

                r = lerp(r, rf, refractivity);
                g = lerp(g, gf, refractivity);
                b = lerp(b, bf, refractivity);
            }
        }

        return new Color(clamp(r), clamp(g), clamp(b));
    }

    /**
     * Crea un rayo reflejado
     * Fórmula: R = D - 2(D·N)N
     */
    private Ray createReflectionRay(Intersection hit, Vector3D V) {
        Vector3D N = hit.normal;
        if (N.dot(V) < 0) N = N.negate();

        Vector3D D = V.negate();
        Vector3D R = D.subtract(N.multiply(2.0 * D.dot(N))).normalize();

        // Pequeño offset para evitar auto-shadowing
        Vector3D origin = hit.point.add(N.multiply(epsilon));
        return new Ray(origin, R);
    }

    /**
     * Crea un rayo refractado usando Ley de Snell
     * Determina automáticamente si entramos o salimos del material
     */
    private Ray createRefractionRay(Intersection hit, Vector3D V, double iorMaterial) {
        Vector3D N = hit.normal;
        double NdotV = N.dot(V);

        // Determinar si estamos entrando o saliendo
        double iorIn, iorOut;
        Vector3D Nout;

        if (NdotV > 0) {
            // Entrando al material
            iorIn = 1.0;  // aire
            iorOut = iorMaterial;
            Nout = N;
        } else {
            // Saliendo del material
            iorIn = iorMaterial;
            iorOut = 1.0;  // aire
            Nout = N.negate();
            NdotV = -NdotV;
        }

        double eta = iorIn / iorOut;
        double disc = 1.0 - eta * eta * (1.0 - NdotV * NdotV);

        // Reflexión total interna
        if (disc < 0) return null;

        double sqrtDisc = Math.sqrt(disc);
        Vector3D T = V.multiply(eta).add(Nout.multiply(eta * NdotV - sqrtDisc)).normalize();

        // Offset pequeño para evitar auto-intersection
        Vector3D origin = hit.point.add(Nout.multiply(-epsilon));
        return new Ray(origin, T);
    }

    /**
     * Interpolación lineal entre dos valores
     */
    private static double lerp(double a, double b, double t) {
        return a * (1.0 - t) + b * t;
    }

    /**
     * Clamp a [0, 255]
     */
    private static int clamp(double v) {
        return (int) Math.min(255, Math.max(0, v * 255));
    }
}

