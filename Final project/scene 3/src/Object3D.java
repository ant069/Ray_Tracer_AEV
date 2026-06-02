import java.awt.Color;

/**
 * Object3D – Base class for renderable objects.
 * Supports materials with optical properties (reflection, refraction, etc.)
 */
public abstract class Object3D {
    protected final Material material;

    /** Constructor with Material. */
    protected Object3D(Material material) {
        this.material = material;
    }

    /**
     * Constructor with Color (backward compatibility).
     * Automatically creates a diffuse material.
     */
    protected Object3D(Color color) {
        this.material = Material.diffuse(color);
    }

    public Material getMaterial()   { return material; }
    public Color getColor()         { return material.getColor(); }

    public abstract Intersection intersect(Ray ray);
}
