import java.awt.Color;

/**
 * PointLight – emits light equally in all directions from a single position.
 *
 * Shown on slide 4 as the middle light type (rays going outward in all directions).
 * The direction toward the light changes per hit point (unlike directional lights).
 */
public class PointLight extends Light {

    private final Vector3D position;

    /**
     * @param position   world-space position of the light source
     * @param color      light color (LC)
     * @param intensity  light intensity (LI)
     */
    public PointLight(Vector3D position, Color color, double intensity) {
        super(color, intensity);
        this.position = position;
    }

    /**
     * Returns the normalized direction FROM the hit point TOWARD the light.
     */
    @Override
    public Vector3D getLightDirection(Vector3D hitPoint) {
        return position.subtract(hitPoint).normalize();
    }

    public Vector3D getPosition() { return position; }
}
