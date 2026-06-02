import java.awt.Color;
import java.util.List;

/**
 * Blinn-Phong shader:
 *   - Lambertian diffuse:    LC × OC × Kd × LI × (N·L)
 *   - Blinn-Phong specular:  LC × Ks × LI × (N·H)^shininess
 *     where H = normalize(L + V) — half-vector between light and view direction
 *   - Explicit quadratic attenuation for PointLight (kc + kl·d + kq·d²)
 *   - Shadow rays for hard shadows
 *   - Global ambient term to avoid pure black
 *
 * Kd and Ks are encapsulated in each Material instance — different surfaces
 * (wet asphalt, red line, puddle) react differently to the same light.
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
        // Use texture color when available, otherwise fall back to material base color
        Material mat = hit.object.getMaterial();
        Color oc;
        if (mat.hasTexture()) {
            double[] tc = mat.getTexture().sample(hit.uvU, hit.uvV);
            oc = new Color((float)tc[0], (float)tc[1], (float)tc[2]);
        } else {
            oc = hit.object.getColor();
        }
        double ocR = oc.getRed()   / 255.0;
        double ocG = oc.getGreen() / 255.0;
        double ocB = oc.getBlue()  / 255.0;

        // Normal: geometric or perturbed by normal map (TBN space)
        Vector3D N = hit.normal;

        if (mat.hasNormalMap() && hit.tangent != null) {
            double[] nm = mat.getNormalMap().sample(hit.uvU, hit.uvV);
            double nx =  nm[0] * 2.0 - 1.0;
            double ny = -(nm[1] * 2.0 - 1.0);  // flip Y: DirectX → OpenGL convention
            double nz =  nm[2] * 2.0 - 1.0;
            Vector3D T = hit.tangent;
            Vector3D B = N.cross(T).normalize();
            Vector3D pN = T.multiply(nx).add(B.multiply(ny)).add(N.multiply(nz));
            double len = pN.length();
            if (len > 1e-8) N = pN.multiply(1.0 / len);
        }

        // Ensure the normal faces toward the viewer
        if (N.dot(viewDir.negate()) < 0) N = N.negate();

        // Ambient term
        double r = ambientIntensity * ocR;
        double g = ambientIntensity * ocG;
        double b = ambientIntensity * ocB;

        double matKd = mat.getKd();
        double matKs = mat.getKs();
        int    matShininess = mat.getShininess();

        // Camera direction (needed for the Blinn-Phong half-vector)
        Vector3D V = viewDir.negate().normalize();

        for (Light light : lights) {
            // Shadow test
            if (scene.isInShadow(hit.point, N, light)) continue;

            Vector3D L = light.getLightDirection(hit.point);
            if (L == null) continue;

            double NdotL = Math.max(0.0, N.dot(L));
            if (NdotL == 0.0) continue;

            // Explicit quadratic attenuation (kc + kl·d + kq·d²)
            double LI = (light instanceof PointLight)
                ? ((PointLight) light).getEffectiveIntensity(hit.point)
                : light.getIntensity();

            Color lc = light.getColor();
            double lcR = lc.getRed()   / 255.0;
            double lcG = lc.getGreen() / 255.0;
            double lcB = lc.getBlue()  / 255.0;

            // Lambertian diffuse: Kd × LC × OC × LI × (N·L)
            double diff = matKd * LI * NdotL;
            r += lcR * ocR * diff;
            g += lcG * ocG * diff;
            b += lcB * ocB * diff;

            // Blinn-Phong specular: Ks × LC × LI × (N·H)^shininess
            // H = normalize(L + V) — produces softer, more physically correct highlights
            // than Phong (R·V) and is well-defined even at grazing angles
            Vector3D H     = L.add(V).normalize();
            double   NdotH = Math.max(0.0, N.dot(H));
            double   spec  = matKs * LI * Math.pow(NdotH, matShininess);
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
