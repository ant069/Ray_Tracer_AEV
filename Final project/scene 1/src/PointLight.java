import java.awt.Color;

/** Luz puntual: emite en todas direcciones con atenuación por distancia cuadrada. */
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

    /** Intensidad efectiva con falloff 1/d² (ley de cuadrado inverso). */
    public double getEffectiveIntensity(Vector3D hitPoint) {
        double d = getDistance(hitPoint);
        if (d < 1e-6) return intensity;
        return intensity / (d * d);
    }

    public Vector3D getPosition() { return position; }
}
