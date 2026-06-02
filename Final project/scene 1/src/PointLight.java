import java.awt.Color;

/**
 * Point light with explicit quadratic attenuation.
 *
 * Standard formula (OpenGL / Blinn-Phong):
 *   I_ef = I / (Kc + Kl·d + Kq·d²)
 *
 * where:
 *   Kc = 1.0  — constant term (prevents division by zero at d=0)
 *   Kl = 0.0  — linear term    (no linear falloff)
 *   Kq = 1/I  — quadratic term normalized by intensity
 *               → at d = sqrt(I) the effective intensity = I/2 (half-distance)
 *
 * This guarantees I_ef <= 1.0 for any d >= 0,
 * and the light always diminishes with increasing distance.
 */
public class PointLight extends Light {
    private final Vector3D position;

    // Attenuation coefficients (standard Blinn-Phong model)
    private static final double KC = 1.0;   // constant
    private static final double KL = 0.0;   // linear
    // KQ is computed dynamically as 1/intensity to normalize the result to [0,1]

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

    /**
     * Effective intensity with explicit quadratic attenuation (Blinn-Phong model):
     *   I_ef = 1 / (Kc + Kl·d + Kq·d²)   with  Kq = 1/I
     *
     * Equivalent to the normalized form  I / (I + d²)  — guarantees I_ef in (0, 1].
     * The `intensity` parameter controls the falloff rate: higher-intensity lights
     * sustain their brightness over more meters before attenuating.
     */
    public double getEffectiveIntensity(Vector3D hitPoint) {
        double d  = getDistance(hitPoint);
        double kq = 1.0 / intensity;                       // Kq = 1/I — normalizes to [0,1]
        double attenuation = KC + KL * d + kq * d * d;    // Kc + Kl·d + Kq·d²
        return 1.0 / attenuation;                         // result in (0, 1]
    }

    public Vector3D getPosition() { return position; }
}
