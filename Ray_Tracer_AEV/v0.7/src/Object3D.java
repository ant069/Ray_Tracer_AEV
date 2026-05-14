import java.awt.Color;

public abstract class Object3D {
    protected final Color color;

    protected Object3D(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public abstract Intersection intersect(Ray ray);
}