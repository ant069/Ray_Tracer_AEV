import java.awt.Color;
import java.util.List;

/**
 * PhongShader v0.5 – smooth (Phong) shading using interpolated normals.
 *
 * The key difference from FlatShader:
 *   • FlatShader (v0.4): uses the flat face normal – same N for every pixel
 *     on a polygon → visible polygon edges (faceted look).
 *   • PhongShader (v0.5): uses the normal stored in the Intersection record,
 *     which Triangle already interpolated from per-vertex normals using
 *     barycentric coordinates (slide 8: "each vertex has a normal").
 *     The shading formula is identical (Lambertian diffuse), but N varies
 *     smoothly across the surface → smooth appearance (slide 11).
 *
 * Formula (same as v0.4 slide 9):
 *   Diffuse = LC × OC × LI × (N · L)
 *
 * The interpolated N comes from Triangle.intersect(), which computes:
 *   N = normalize( w·n0 + u·n1 + v·n2 )    (slide 6 / slide 8)
 *
 * If the triangle has no vertex normals (flat fallback), hit.normal is
 * the face normal and the result is identical to FlatShader.
 *
 * Ambient term is added to avoid completely black unlit surfaces.
 */
public class PhongShader {

    private final double ambientIntensity;

    public PhongShader(double ambientIntensity) {
        this.ambientIntensity = ambientIntensity;
    }

    /**
     * Computes the shaded color using the interpolated normal in the hit record.
     *
     * @param hit    intersection (contains the already-interpolated normal)
     * @param lights all lights in the scene
     * @return       final pixel color
     */
    public Color shade(Intersection hit, List<Light> lights) {
        // OC – object color [0, 1]
        Color oc = hit.object.getColor();
        double ocR = oc.getRed()   / 255.0;
        double ocG = oc.getGreen() / 255.0;
        double ocB = oc.getBlue()  / 255.0;

        // N – interpolated normal at this point (Phong interpolation, slide 8)
        // For flat objects this is just the face normal (same as FlatShader).
        Vector3D N = hit.normal;

        // Start with ambient
        double r = ambientIntensity * ocR;
        double g = ambientIntensity * ocG;
        double b = ambientIntensity * ocB;

        for (Light light : lights) {
            // L – direction toward the light
            Vector3D L = light.getLightDirection(hit.point);
            if (L == null) continue;   // outside spot cone

            // N · L = cos θ  (Lambertian, slide 7 of session 23)
            double NdotL = Math.max(0.0, N.dot(L));
            if (NdotL == 0.0) continue;

            // Spot attenuation
            double spotFactor = 1.0;
            if (light instanceof SpotLight) {
                spotFactor = ((SpotLight) light).getSpotFactor(hit.point);
                if (spotFactor == 0.0) continue;
            }

            // LC [0, 1]
            Color lc = light.getColor();
            double lcR = lc.getRed()   / 255.0;
            double lcG = lc.getGreen() / 255.0;
            double lcB = lc.getBlue()  / 255.0;

            // Diffuse = LC × OC × LI × (N · L)
            double factor = light.getIntensity() * NdotL * spotFactor;
            r += lcR * ocR * factor;
            g += lcG * ocG * factor;
            b += lcB * ocB * factor;
        }

        return new Color(
            clamp(r),
            clamp(g),
            clamp(b)
        );
    }

    private static int clamp(double v) {
        return (int) Math.min(255, Math.max(0, v * 255));
    }
}
