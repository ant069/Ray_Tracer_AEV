import java.awt.Color;

/**
 * Spot light — a PointLight confined to a cone.
 * Intensity falls off smoothly between the inner (full) and outer (zero) cone angles.
 * Used to simulate Denji's bare overhead light bulb casting a dramatic downward pool.
 */
public class SpotLight extends PointLight {
    private final Vector3D direction;  // normalized spot direction (where it points)
    private final double   innerCos;   // cos of inner half-angle (full intensity inside)
    private final double   outerCos;   // cos of outer half-angle (zero intensity outside)

    /**
     * @param position   bulb position
     * @param direction  direction the spot points toward
     * @param innerDeg   inner cone half-angle in degrees (full intensity core)
     * @param outerDeg   outer cone half-angle in degrees (zero intensity boundary)
     * @param color      light color
     * @param intensity  base intensity (same scale as PointLight)
     */
    public SpotLight(Vector3D position, Vector3D direction,
                     double innerDeg, double outerDeg,
                     Color color, double intensity) {
        super(position, color, intensity);
        this.direction = direction.normalize();
        this.innerCos  = Math.cos(Math.toRadians(innerDeg));
        this.outerCos  = Math.cos(Math.toRadians(outerDeg));
    }

    /**
     * Effective intensity: base PointLight attenuation × smooth spot falloff.
     * Returns 0 for points outside the outer cone.
     */
    @Override
    public double getEffectiveIntensity(Vector3D hitPoint) {
        Vector3D toPoint = hitPoint.subtract(getPosition()).normalize();
        double cosAngle  = toPoint.dot(direction);
        if (cosAngle <= outerCos) return 0.0;
        double base = super.getEffectiveIntensity(hitPoint);
        double t    = (cosAngle - outerCos) / (innerCos - outerCos);
        // Smoothstep: eases from 0 to 1 across the penumbra zone
        double spot = Math.min(1.0, Math.max(0.0, t * t * (3.0 - 2.0 * t)));
        return base * spot;
    }
}
