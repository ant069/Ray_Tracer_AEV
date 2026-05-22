import java.awt.Color;

/**
 * DirectionalLight – simulates a light source infinitely far away
 * (e.g. the sun). All rays are parallel and share the same direction.
 *
 * Shown on slide 4 as the leftmost light type.
 * Used in the Lambertian surface example on slide 8:
 *   directions like (0.0, 0.0, 1.0), (0.0, -0.1, 1.0), etc.
 */
public class DirectionalLight extends Light {

    // Normalized direction the light travels (from light toward scene)
    private final Vector3D direction;

    /**
     * @param direction  direction the light rays travel (will be normalized).
     *                   The direction from a hit point toward the light is the negation.
     * @param color      light color (LC)
     * @param intensity  light intensity (LI)
     */
    public DirectionalLight(Vector3D direction, Color color, double intensity) {
        super(color, intensity);
        this.direction = direction.normalize();
    }

    /**
     * For a directional light the vector toward the light is always
     * the negation of the light's travel direction, regardless of hit position.
     */
    @Override
    public Vector3D getLightDirection(Vector3D hitPoint) {
        return direction.negate();   // toward the light source
    }
}
