import java.awt.Color;

/**
 * Spot light: point light with an illumination cone.
 * Inside the inner cone → full intensity with distance falloff.
 * Between inner and outer cone → quadratic smooth falloff.
 * Outside the outer cone → no light.
 */
public class SpotLight extends PointLight {
    private final Vector3D direction;  // normalized, from light toward target
    private final double   cosInner;
    private final double   cosOuter;

    /**
     * @param position  light position
     * @param target    point the spot aims at
     * @param innerDeg  inner angle (degrees) — full intensity zone
     * @param outerDeg  outer angle (degrees) — cone edge
     */
    public SpotLight(Vector3D position, Vector3D target, Color color,
                     double intensity, double innerDeg, double outerDeg) {
        super(position, color, intensity);
        this.direction = target.subtract(position).normalize();
        this.cosInner  = Math.cos(Math.toRadians(innerDeg));
        this.cosOuter  = Math.cos(Math.toRadians(outerDeg));
    }

    @Override
    public double getEffectiveIntensity(Vector3D point) {
        double base = super.getEffectiveIntensity(point);

        // Angle between the spot direction and the direction toward the point
        Vector3D toPoint = point.subtract(getPosition()).normalize();
        double cosAngle  = toPoint.dot(direction);

        if (cosAngle < cosOuter) return 0.0;
        if (cosAngle >= cosInner) return base;

        // Quadratic smooth falloff between inner and outer cone
        double t = (cosAngle - cosOuter) / (cosInner - cosOuter);
        return base * t * t;
    }
}
