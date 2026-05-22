import java.awt.Color;

public abstract class Light {
    protected final Color  color;
    protected final double intensity;

    protected Light(Color color, double intensity) {
        this.color     = color;
        this.intensity = intensity;
    }

    /** Dirección normalizada desde hitPoint HACIA la luz. Null si no aplica. */
    public abstract Vector3D getLightDirection(Vector3D hitPoint);

    public Color  getColor()     { return color;     }
    public double getIntensity() { return intensity;  }
}
