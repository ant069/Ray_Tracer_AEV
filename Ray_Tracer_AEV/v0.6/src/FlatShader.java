import java.awt.Color;
import java.util.List;

/**
 * FlatShader – Lambertian flat shading (Session 22, slide 9).
 *
 * Formula:
 *   Diffuse = LC × OC × LI × (N · L)
 *
 *   LC  = light color
 *   OC  = object color
 *   LI  = light intensity
 *   N   = surface normal at the hit point (per-polygon for flat shading)
 *   L   = unit vector from hit point toward the light
 *   N·L = cos θ  (Lambert's cosine law, slide 7)
 *
 * Multiple lights are summed. An ambient term prevents completely black surfaces.
 *
 * BUG FIX (v0.6): this method is now actually called by Raytracer.traceRay().
 * In v0.4/v0.5 Raytracer returned hit.object.getColor() directly,
 * bypassing the shader entirely.
 */
public class FlatShader {

    /** Ambient intensity – keeps unlit areas from going pitch-black. */
    private final double ambientIntensity;

    public FlatShader(double ambientIntensity) {
        this.ambientIntensity = ambientIntensity;
    }

    /**
     * Compute the shaded colour at a hit point.
     *
     * @param hit    intersection record (normal, point, object)
     * @param lights all lights in the scene
     * @return       final pixel colour after lighting
     */
    public Color shade(Intersection hit, List<Light> lights) {

        // OC – object colour normalised to [0,1]
        Color oc = hit.object.getColor();
        double ocR = oc.getRed()   / 255.0;
        double ocG = oc.getGreen() / 255.0;
        double ocB = oc.getBlue()  / 255.0;

        // N – surface normal (flat shading: constant per polygon, slide 6)
        Vector3D N = hit.normal;

        // Accumulate: start with ambient
        double r = ambientIntensity * ocR;
        double g = ambientIntensity * ocG;
        double b = ambientIntensity * ocB;

        for (Light light : lights) {
            // L – direction from hit point TOWARD the light (slide 7)
            Vector3D L = light.getLightDirection(hit.point);
            if (L == null) continue;   // outside spot cone (unused in v0.6)

            // N · L = cos θ  – clamp to [0,1]: negative means back-face (slide 7)
            double NdotL = Math.max(0.0, N.dot(L));
            if (NdotL == 0.0) continue;

            // LC – light colour normalised to [0,1]
            Color lc = light.getColor();
            double lcR = lc.getRed()   / 255.0;
            double lcG = lc.getGreen() / 255.0;
            double lcB = lc.getBlue()  / 255.0;

            // Diffuse = LC × OC × LI × (N · L)   (slide 9)
            double factor = light.getIntensity() * NdotL;
            r += lcR * ocR * factor;
            g += lcG * ocG * factor;
            b += lcB * ocB * factor;
        }

        // Clamp to [0,255] and return
        return new Color(clamp(r), clamp(g), clamp(b));
    }

    private static int clamp(double v) {
        return (int) Math.min(255, Math.max(0, v * 255));
    }
}
