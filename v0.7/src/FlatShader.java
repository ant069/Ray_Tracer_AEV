import java.awt.Color;
import java.util.List;

/**
 * FlatShader v0.7
 *
 * New vs v0.6:
 *
 * 1. LIGHT FALLOFF (Session 24, slides 4-6)
 *    For PointLight, the effective intensity is attenuated by distance squared:
 *      LI = intensity / d^2
 *    DirectionalLight has no distance so no falloff applies.
 *
 * 2. SHADOWS (Session 24, slides 7-13)
 *    Before computing each light's contribution, calls Scene.isInShadow().
 *    If the hit point is occluded, that light is skipped entirely.
 *    Shadow rays are offset by SHADOW_BIAS along the normal to prevent
 *    self-shadowing (slide 13: "do not collide with yourself, same triangle").
 *
 * Flat shading formula (unchanged):
 *   Diffuse = LC x OC x LI x (N . L)
 * where LI now includes falloff for point lights.
 */
public class FlatShader {

    private final double ambientIntensity;

    public FlatShader(double ambientIntensity) {
        this.ambientIntensity = ambientIntensity;
    }

    /**
     * Compute shaded colour with falloff and shadows.
     *
     * @param hit    intersection record (point, normal, object)
     * @param lights all lights in the scene
     * @param scene  needed to cast shadow rays
     */
    public Color shade(Intersection hit, List<Light> lights, Scene scene) {

        Color oc = hit.object.getColor();
        double ocR = oc.getRed()   / 255.0;
        double ocG = oc.getGreen() / 255.0;
        double ocB = oc.getBlue()  / 255.0;

        Vector3D N = hit.normal;

        // Ambient baseline
        double r = ambientIntensity * ocR;
        double g = ambientIntensity * ocG;
        double b = ambientIntensity * ocB;

        for (Light light : lights) {

            // SHADOW TEST (slides 7-13)
            if (scene.isInShadow(hit.point, N, light)) continue;

            Vector3D L = light.getLightDirection(hit.point);
            if (L == null) continue;

            double NdotL = Math.max(0.0, N.dot(L));
            if (NdotL == 0.0) continue;

            // FALLOFF (slides 4-6): PointLight uses LI = intensity / d^2
            double LI;
            if (light instanceof PointLight) {
                LI = ((PointLight) light).getEffectiveIntensity(hit.point);
            } else {
                LI = light.getIntensity(); // DirectionalLight: no falloff
            }

            Color lc = light.getColor();
            double lcR = lc.getRed()   / 255.0;
            double lcG = lc.getGreen() / 255.0;
            double lcB = lc.getBlue()  / 255.0;

            double factor = LI * NdotL;
            r += lcR * ocR * factor;
            g += lcG * ocG * factor;
            b += lcB * ocB * factor;
        }

        return new Color(clamp(r), clamp(g), clamp(b));
    }

    private static int clamp(double v) {
        return (int) Math.min(255, Math.max(0, v * 255));
    }
}
