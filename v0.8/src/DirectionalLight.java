import java.awt.Color;

/** Luz direccional (como el sol): rayos paralelos, sin atenuación por distancia. */
public class DirectionalLight extends Light {
    private final Vector3D direction; // dirección hacia donde viaja la luz

    public DirectionalLight(Vector3D direction, Color color, double intensity) {
        super(color, intensity);
        this.direction = direction.normalize();
    }

    @Override
    public Vector3D getLightDirection(Vector3D hitPoint) {
        return direction.negate(); // desde hitPoint HACIA la luz
    }
}
