import java.awt.Color;

/**
 * Light – abstract base for all light types (v0.4).
 *
 * Every light has a color and intensity (LI and LC from the flat shading formula):
 *   Diffuse = LC × OC × LI × (N · L)
 *
 * Subclasses implement getLightDirection() to return the normalized
 * direction FROM the hit point TOWARD the light source.
 */
public abstract class Light {

    protected final Color  color;      // LC – light color
    protected final double intensity;  // LI – light intensity

    protected Light(Color color, double intensity) {
        this.color     = color;
        this.intensity = intensity;
    }

    /** Normalized direction from the hit point toward the light source. */
    public abstract Vector3D getLightDirection(Vector3D hitPoint);

    public Color  getColor()     { return color;     }
    public double getIntensity() { return intensity;  }
}
