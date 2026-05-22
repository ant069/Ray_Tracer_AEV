import java.awt.Color;

/**
 * Object3D – Clase base para objetos renderizables
 * Soporta materiales con propiedades ópticas (reflexión, refracción, etc.)
 */
public abstract class Object3D {
    protected final Material material;

    /**
     * Constructor con Material
     */
    protected Object3D(Material material) {
        this.material = material;
    }

    /**
     * Constructor con Color (compatibilidad hacia atrás)
     * Crea un material difuso automáticamente
     */
    protected Object3D(Color color) {
        this.material = Material.diffuse(color);
    }

    public Material getMaterial()   { return material; }
    public Color getColor()         { return material.getColor(); }

    public abstract Intersection intersect(Ray ray);
}
