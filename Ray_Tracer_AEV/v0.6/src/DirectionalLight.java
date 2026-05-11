import java.awt.Color;

/**
 * DirectionalLight – parallel rays from an infinite distance (e.g. the sun).
 *
 * Session 23 slide 3: Direction (0.0, 0.0, 1.0), Color (255,255,255), Intensity 1.1
 * Session 23 slide 4: Direction (0.0,-1.0, 0.0), Color (255,0,0),     Intensity 1.1
 *
 * The direction stored is WHERE THE LIGHT TRAVELS TO (toward the scene).
 * getLightDirection() returns the negation — FROM hit point TOWARD the light.
 */
public class DirectionalLight extends Light {

    /** Direction light rays travel – stored normalised. */
    private final Vector3D direction;

    /**
     * @param direction  direction the light rays travel (will be normalised)
     * @param color      LC – light color
     * @param intensity  LI – light intensity
     */
    public DirectionalLight(Vector3D direction, Color color, double intensity) {
        super(color, intensity);
        this.direction = direction.normalize();
    }

    /**
     * For a directional light L is always the negation of the travel direction,
     * regardless of where on the surface the ray hit.
     */
    @Override
    public Vector3D getLightDirection(Vector3D hitPoint) {
        return direction.negate();   // toward the light
    }
}
