import java.awt.Color;
import java.util.List;

/**
 * Shader con:
 *   - Iluminación difusa Lambertiana: LC × OC × LI × (N·L)
 *   - Especular Phong: LC × specularColor × LI × (R·V)^shininess
 *   - Atenuación por distancia en PointLight (1/d²)
 *   - Shadow rays para sombras duras
 *   - Término ambiente para evitar negro absoluto
 */
public class PhongShader {
    private final double ambientIntensity;
    private final double specularStrength;
    private final int    shininess;

    public PhongShader(double ambientIntensity, double specularStrength, int shininess) {
        this.ambientIntensity = ambientIntensity;
        this.specularStrength = specularStrength;
        this.shininess        = shininess;
    }

    public Color shade(Intersection hit, List<Light> lights, Scene scene, Vector3D viewDir) {
        Color oc = hit.object.getColor();
        double ocR = oc.getRed()   / 255.0;
        double ocG = oc.getGreen() / 255.0;
        double ocB = oc.getBlue()  / 255.0;

        Vector3D N = hit.normal;
        // Asegurar que la normal apunta hacia el viewer
        if (N.dot(viewDir.negate()) < 0) N = N.negate();

        // Ambiente
        double r = ambientIntensity * ocR;
        double g = ambientIntensity * ocG;
        double b = ambientIntensity * ocB;

        for (Light light : lights) {
            // Test de sombra
            if (scene.isInShadow(hit.point, N, light)) continue;

            Vector3D L = light.getLightDirection(hit.point);
            if (L == null) continue;

            double NdotL = Math.max(0.0, N.dot(L));
            if (NdotL == 0.0) continue;

            // Intensidad efectiva (con falloff para PointLight)
            double LI = (light instanceof PointLight)
                ? ((PointLight) light).getEffectiveIntensity(hit.point)
                : light.getIntensity();

            Color lc = light.getColor();
            double lcR = lc.getRed()   / 255.0;
            double lcG = lc.getGreen() / 255.0;
            double lcB = lc.getBlue()  / 255.0;

            // Difuso: LC × OC × LI × (N·L)
            double diff = LI * NdotL;
            r += lcR * ocR * diff;
            g += lcG * ocG * diff;
            b += lcB * ocB * diff;

            // Especular: LC × specular × LI × (R·V)^shininess
            // R = reflect(-L, N) = 2(N·L)N - L  →  pero usamos L ya positivo
            Vector3D R = N.multiply(2.0 * NdotL).subtract(L).normalize();
            Vector3D V = viewDir.negate().normalize(); // dirección hacia cámara
            double RdotV = Math.max(0.0, R.dot(V));
            double spec  = specularStrength * LI * Math.pow(RdotV, shininess);
            r += lcR * spec;
            g += lcG * spec;
            b += lcB * spec;
        }

        return new Color(clamp(r), clamp(g), clamp(b));
    }

    private static int clamp(double v) {
        return (int) Math.min(255, Math.max(0, v * 255));
    }
}
