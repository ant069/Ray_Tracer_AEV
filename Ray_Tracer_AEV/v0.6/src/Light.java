import java.awt.Color;

/**
 * Light – abstract base for all light types (Session 23).
 *
 * Every light has:
 *   color     (LC) – light color
 *   intensity (LI) – scalar brightness multiplier
 *
 * Used in the flat shading formula (slide 9):
 *   Diffuse = LC × OC × LI × (N · L)
 *
 * Subclasses implement getLightDirection() which returns the unit vector
 * FROM the hit point TOWARD the light source.
 */
public abstract class Light {

    protected final Color  color;      // LC
    protected final double intensity;  // LI

    protected Light(Color color, double intensity) {
        this.color     = color;
        this.intensity = intensity;
    }

    /**
     * Returns the unit vector from hitPoint toward the light.
     * Returns null if the hit point receives no contribution (e.g. outside spot cone).
     */
    public abstract Vector3D getLightDirection(Vector3D hitPoint);

    public Color  getColor()     { return color;     }
    public double getIntensity() { return intensity; }
}
