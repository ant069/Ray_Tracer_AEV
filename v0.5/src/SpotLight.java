import java.awt.Color;

/**
 * SpotLight – a point light restricted to a cone.
 *
 * Shown on slide 4 as the rightmost light type.
 * Light is only emitted within the cone defined by the cutoff angle.
 * Outside the cone the contribution is zero.
 */
public class SpotLight extends Light {

    private final Vector3D position;       // world-space position
    private final Vector3D direction;      // normalized direction the spot aims at
    private final double   cosCutoff;      // cos of the cutoff angle (inner cone)
    private final double   cosOuterCutoff; // cos of the outer cutoff (soft falloff edge)

    /**
     * @param position       world-space position
     * @param direction      direction the spot aims at (will be normalized)
     * @param innerDegrees   inner cone angle in degrees (full brightness inside)
     * @param outerDegrees   outer cone angle in degrees (falls off to zero at edge)
     * @param color          light color (LC)
     * @param intensity      light intensity (LI)
     */
    public SpotLight(Vector3D position, Vector3D direction,
                     double innerDegrees, double outerDegrees,
                     Color color, double intensity) {
        super(color, intensity);
        this.position       = position;
        this.direction      = direction.normalize();
        this.cosCutoff      = Math.cos(Math.toRadians(innerDegrees));
        this.cosOuterCutoff = Math.cos(Math.toRadians(outerDegrees));
    }

    /**
     * Returns the normalized direction from the hit point toward the light.
     * If the hit point is outside the cone, returns null (no contribution).
     */
    @Override
    public Vector3D getLightDirection(Vector3D hitPoint) {
        Vector3D toLight = position.subtract(hitPoint).normalize();

        // cosine of angle between spot direction and direction to hit point
        double cosAngle = direction.dot(toLight.negate());

        if (cosAngle < cosOuterCutoff) return null; // completely outside cone
        return toLight;
    }

    /**
     * Spot attenuation factor [0, 1] based on cone angle.
     * 1.0 inside inner cone, smooth falloff between inner and outer.
     */
    public double getSpotFactor(Vector3D hitPoint) {
        Vector3D toLight = position.subtract(hitPoint).normalize();
        double cosAngle  = direction.dot(toLight.negate());

        if (cosAngle >= cosCutoff)      return 1.0;
        if (cosAngle < cosOuterCutoff)  return 0.0;

        // Smooth step between inner and outer cones
        double t = (cosAngle - cosOuterCutoff) / (cosCutoff - cosOuterCutoff);
        return t * t * (3.0 - 2.0 * t);
    }

    public Vector3D getPosition()  { return position;  }
    public Vector3D getSpotDirection() { return direction; }
}
