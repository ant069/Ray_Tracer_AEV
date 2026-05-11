import java.awt.Color;

/**
 * PointLight – emits light equally in all directions from a single position.
 *
 * NEW in v0.6 (Session 23 slide 5):
 *   Position (0.0, 1.0, 0.0)
 *   Color    (255, 255, 255)
 *   Intensity (0.9)
 *
 * Unlike a DirectionalLight, the direction toward the light changes for every
 * hit point: L = normalize(position - hitPoint).
 */
public class PointLight extends Light {

    private final Vector3D position;

    /**
     * @param position   world-space position of the point light
     * @param color      LC – light color
     * @param intensity  LI – light intensity
     */
    public PointLight(Vector3D position, Color color, double intensity) {
        super(color, intensity);
        this.position = position;
    }

    /**
     * Direction FROM hit point TOWARD the light — varies per surface point.
     */
    @Override
    public Vector3D getLightDirection(Vector3D hitPoint) {
        return position.subtract(hitPoint).normalize();
    }

    public Vector3D getPosition() { return position; }
}
