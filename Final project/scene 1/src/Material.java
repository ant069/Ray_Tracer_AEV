import java.awt.Color;

/**
 * Material – Define propiedades ópticas de un objeto
 *
 * Propiedades:
 *   - color: Color base del material
 *   - reflectivity: Intensidad de reflexión especular (0.0 = mate, 1.0 = espejo)
 *   - refractivity: Intensidad de refracción (0.0 = opaco, 1.0 = vidrio puro)
 *   - ior: Índice de refracción (aire=1.0, vidrio~1.5, diamante~2.42)
 *   - shininess: Exponente de brillo Phong (para reflexiones borrosas)
 */
public class Material {
    private final Color   color;
    private final double  reflectivity;    // 0.0 a 1.0
    private final double  refractivity;    // 0.0 a 1.0
    private final double  ior;             // Índice de refracción
    private final int     shininess;       // Phong exponent

    /**
     * Constructor completo
     */
    public Material(Color color, double reflectivity, double refractivity, double ior, int shininess) {
        this.color         = color;
        this.reflectivity  = clamp(reflectivity);
        this.refractivity  = clamp(refractivity);
        this.ior           = Math.max(0.1, ior);
        this.shininess     = Math.max(1, shininess);
    }

    /**
     * Material difuso (mate, sin reflexión ni refracción)
     */
    public static Material diffuse(Color color) {
        return new Material(color, 0.0, 0.0, 1.0, 32);
    }

    /**
     * Material especular (espejo)
     */
    public static Material mirror(Color color, double reflectivity) {
        return new Material(color, reflectivity, 0.0, 1.0, 128);
    }

    /**
     * Material refractivo (vidrio, agua, etc.)
     */
    public static Material glass(Color color, double ior, double transparency) {
        return new Material(color, 0.1, transparency, ior, 64);
    }

    /**
     * Material brillante (metálico con reflexión borrosa)
     */
    public static Material metallic(Color color, double reflectivity, int shininess) {
        return new Material(color, reflectivity, 0.0, 1.0, shininess);
    }

    // ── Getters ─────────────────────────────────────────────────────────────

    public Color getColor()            { return color; }
    public double getReflectivity()    { return reflectivity; }
    public double getRefractivity()    { return refractivity; }
    public double getIOR()             { return ior; }
    public int getShininess()          { return shininess; }

    /**
     * ¿Es principalmente refractivo (transparente)?
     */
    public boolean isRefractive() {
        return refractivity > reflectivity && refractivity > 0.1;
    }

    /**
     * ¿Es principalmente reflectivo (especular)?
     */
    public boolean isReflective() {
        return reflectivity > refractivity && reflectivity > 0.1;
    }

    /**
     * ¿Es completamente opaco?
     */
    public boolean isOpaque() {
        return reflectivity < 0.01 && refractivity < 0.01;
    }

    private static double clamp(double v) {
        return Math.min(1.0, Math.max(0.0, v));
    }
}
