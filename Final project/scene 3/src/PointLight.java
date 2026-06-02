import java.awt.Color;

/** Point light: emits in all directions with quadratic distance attenuation. */
public class PointLight extends Light {
    private final Vector3D position;

    public PointLight(Vector3D position, Color color, double intensity) {
        super(color, intensity);
        this.position = position;
    }

    @Override
    public Vector3D getLightDirection(Vector3D hitPoint) {
        return position.subtract(hitPoint).normalize();
    }

    public double getDistance(Vector3D hitPoint) {
        return position.subtract(hitPoint).length();
    }

    /** Effective intensity with smooth falloff: intensity/(d²+intensity), maximum 1.0. */
    public double getEffectiveIntensity(Vector3D hitPoint) {
        double d = getDistance(hitPoint);
        if (d < 1e-6) return 1.0;
        return intensity / (d * d + intensity);
    }

    public Vector3D getPosition() { return position; }
}
