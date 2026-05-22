import java.awt.Color;
import java.util.List;

/**
 * FlatShader – implements flat (Lambertian) shading as taught in Session 22.
 *
 * Formula (slide 9):
 *   Diffuse = LC × OC × LI × (N · L)
 *
 *   LC = light color
 *   OC = object color
 *   LI = light intensity
 *   N  = surface normal (pre-computed per polygon for flat shading, slide 6)
 *   L  = normalized direction from hit point toward the light (slide 7)
 *   N·L = cos θ  (Lambert's cosine law, slide 7)
 *
 * When multiple lights are present their diffuse contributions are summed.
 * An ambient term is added so surfaces never go completely black.
 */
public class FlatShader {

    private final double ambientIntensity; // small constant to avoid pitch-black shadows

    public FlatShader(double ambientIntensity) {
        this.ambientIntensity = ambientIntensity;
    }

    /**
     * Computes the shaded color at the hit point.
     *
     * @param hit    the intersection record (contains normal, point, object)
     * @param lights all lights in the scene
     * @return       final shaded pixel color
     */
    public Color shade(Intersection hit, List<Light> lights) {
        // OC – object color, normalized to [0, 1]
        Color oc = hit.object.getColor();
        double ocR = oc.getRed()   / 255.0;
        double ocG = oc.getGreen() / 255.0;
        double ocB = oc.getBlue()  / 255.0;

        // N – surface normal (flat: same for every point on the polygon, slide 6)
        Vector3D N = hit.normal;

        // Accumulate lighting contribution: start with ambient
        double r = ambientIntensity * ocR;
        double g = ambientIntensity * ocG;
        double b = ambientIntensity * ocB;

        for (Light light : lights) {
            // L – direction from hit point toward the light (slide 7)
            Vector3D L = light.getLightDirection(hit.point);

            // SpotLight may return null when hit point is outside the cone
            if (L == null) continue;

            // N · L = cos θ  (Lambert's cosine law, slide 7)
            // Clamped to [0, 1]: negative values mean the surface faces away
            double NdotL = Math.max(0.0, N.dot(L));

            if (NdotL == 0.0) continue; // surface faces away, no contribution

            // Spot attenuation (only SpotLights; other lights return 1.0)
            double spotFactor = 1.0;
            if (light instanceof SpotLight) {
                spotFactor = ((SpotLight) light).getSpotFactor(hit.point);
                if (spotFactor == 0.0) continue;
            }

            // LC – light color, normalized to [0, 1]
            Color lc = light.getColor();
            double lcR = lc.getRed()   / 255.0;
            double lcG = lc.getGreen() / 255.0;
            double lcB = lc.getBlue()  / 255.0;

            // LI – light intensity
            double LI = light.getIntensity();

            // Diffuse = LC × OC × LI × (N · L)   (slide 9)
            double factor = LI * NdotL * spotFactor;
            r += lcR * ocR * factor;
            g += lcG * ocG * factor;
            b += lcB * ocB * factor;
        }

        // Clamp to [0, 1] and convert back to [0, 255]
        int ri = (int) Math.min(255, Math.max(0, r * 255));
        int gi = (int) Math.min(255, Math.max(0, g * 255));
        int bi = (int) Math.min(255, Math.max(0, b * 255));

        return new Color(ri, gi, bi);
    }
}
