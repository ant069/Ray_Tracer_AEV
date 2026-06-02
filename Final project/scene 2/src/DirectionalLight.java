import java.awt.Color;

/** Directional light (like the sun): parallel rays, no distance attenuation. */
public class DirectionalLight extends Light {
    private final Vector3D direction; // direction the light travels toward

    public DirectionalLight(Vector3D direction, Color color, double intensity) {
        super(color, intensity);
        this.direction = direction.normalize();
    }

    @Override
    public Vector3D getLightDirection(Vector3D hitPoint) {
        return direction.negate(); // from hitPoint TOWARD the light
    }
}
