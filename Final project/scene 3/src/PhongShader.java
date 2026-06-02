import java.awt.Color;
import java.util.List;

/**
 * Blinn-Phong shader with AreaLight support (soft shadows).
 *
 * Point/directional lights → hard shadow (1 shadow ray).
 * AreaLight → soft shadow: fires samples² rays toward stratified points
 * across the light area; the visible/total ratio produces penumbra.
 */
public class PhongShader {
    private final double ambientIntensity;
    private final double specularStrength;
    private final int    shininess;
    private static final double SHADOW_EPS = 1e-4;

    public PhongShader(double ambientIntensity, double specularStrength, int shininess) {
        this.ambientIntensity = ambientIntensity;
        this.specularStrength = specularStrength;
        this.shininess        = shininess;
    }

    public Color shade(Intersection hit, List<Light> lights, Scene scene, Vector3D viewDir) {
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

        // Normal (with normal map support)
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
        if (N.dot(viewDir.negate()) < 0) N = N.negate();

        double r = ambientIntensity * ocR;
        double g = ambientIntensity * ocG;
        double b = ambientIntensity * ocB;

        Vector3D V = viewDir.negate().normalize();
        int matShininess = mat.getShininess();

        for (Light light : lights) {

            // AreaLight: soft shadows via stratified sampling
            if (light instanceof AreaLight) {
                AreaLight al    = (AreaLight) light;
                List<Vector3D> samples = al.getSamplePoints();
                int   litCount  = 0;
                double sumDiff  = 0, sumSpec = 0;

                for (Vector3D samplePt : samples) {
                    Vector3D Ls    = samplePt.subtract(hit.point);
                    double   distS = Ls.length();
                    if (distS < 1e-6) continue;
                    Ls = Ls.multiply(1.0 / distS);

                    double NdotLs = Math.max(0.0, N.dot(Ls));
                    if (NdotLs == 0.0) continue;

                    // Shadow ray toward this area sample point
                    Vector3D orig = hit.point.add(N.multiply(SHADOW_EPS));
                    Ray      sRay = new Ray(orig, Ls);
                    Intersection sHit = scene.intersect(sRay, SHADOW_EPS, distS - SHADOW_EPS);
                    if (sHit.hit && sHit.object.getMaterial().getRefractivity() < 0.1) continue;

                    litCount++;
                    sumDiff += NdotLs;
                    Vector3D Hs    = Ls.add(V).normalize();
                    double   NdotH = Math.max(0.0, N.dot(Hs));
                    sumSpec += Math.pow(NdotH, matShininess);
                }

                if (litCount == 0) continue;

                // softFactor in (0,1] — the penumbra arises from this fraction
                double softFactor = (double) litCount / samples.size();
                double LI   = al.getIntensity() * softFactor;
                Color  lc   = light.getColor();
                double lcR  = lc.getRed()   / 255.0;
                double lcG  = lc.getGreen() / 255.0;
                double lcB  = lc.getBlue()  / 255.0;
                double avgD = sumDiff / samples.size();
                double avgS = sumSpec / samples.size();

                r += lcR * ocR * LI * avgD;
                g += lcG * ocG * LI * avgD;
                b += lcB * ocB * LI * avgD;
                double ks = specularStrength * mat.getSpecularCoeff();
                r += lcR * ks * LI * avgS;
                g += lcG * ks * LI * avgS;
                b += lcB * ks * LI * avgS;
                continue;
            }

            // Point/directional light: standard hard shadow
            if (scene.isInShadow(hit.point, N, light)) continue;

            Vector3D L = light.getLightDirection(hit.point);
            if (L == null) continue;

            double NdotL = Math.max(0.0, N.dot(L));
            if (NdotL == 0.0) continue;

            double LI = (light instanceof PointLight)
                ? ((PointLight) light).getEffectiveIntensity(hit.point)
                : light.getIntensity();

            Color lc = light.getColor();
            double lcR = lc.getRed()   / 255.0;
            double lcG = lc.getGreen() / 255.0;
            double lcB = lc.getBlue()  / 255.0;

            double diff = LI * NdotL;
            r += lcR * ocR * diff;
            g += lcG * ocG * diff;
            b += lcB * ocB * diff;

            // Blinn-Phong: H = normalize(L+V), spec = (N·H)^shininess
            // Produces softer highlights than Phong (R·V) at any camera angle
            Vector3D H    = L.add(V).normalize();
            double NdotH  = Math.max(0.0, N.dot(H));
            double spec   = specularStrength * mat.getSpecularCoeff() * LI * Math.pow(NdotH, matShininess);
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
