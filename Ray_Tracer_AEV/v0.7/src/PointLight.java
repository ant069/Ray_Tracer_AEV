import java.awt.Color;

/**
 * PointLight v0.7 – adds light falloff (Session 24, slides 4-6).
 *
 * Falloff formula (slide 5):
 *   LI_effective = intensity / d²
 *
 * where d = distance from the light position to the hit point.
 *
 * Three variants shown in slide 6:
 *   LI = intensity / d    → soft falloff
 *   LI = intensity / d²   → physically correct (inverse-square law)
 *   LI = intensity / d³   → very aggressive falloff
 *
 * This implementation uses d² (slide 5 default).
 * Falloff only applies to PointLight — DirectionalLight has no distance
 * so its LI is constant (the sun doesn't get dimmer with distance).
 */
public class PointLight extends Light {

    private final Vector3D position;

    /**
     * @param position   world-space position of the point light
     * @param color      LC – light color
     * @param intensity  LI base intensity (before falloff)
     */
    public PointLight(Vector3D position, Color color, double intensity) {
        super(color, intensity);
        this.position = position;
    }

    /** Direction FROM hit point TOWARD the light. */
    @Override
    public Vector3D getLightDirection(Vector3D hitPoint) {
        return position.subtract(hitPoint).normalize();
    }

    /** Distance from the light to hitPoint – used for falloff and shadow check. */
    public double getDistance(Vector3D hitPoint) {
        return position.subtract(hitPoint).length();
    }

    /**
     * Effective intensity at hitPoint after inverse-square falloff.
     *   LI = intensity / d²   (slide 5)
     */
    public double getEffectiveIntensity(Vector3D hitPoint) {
        double d = getDistance(hitPoint);
        if (d < 1e-6) return intensity;
        return intensity / (d * d);
    }

    public Vector3D getPosition() { return position; }
}
