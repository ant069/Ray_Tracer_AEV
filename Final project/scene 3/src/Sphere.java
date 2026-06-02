import java.awt.Color;

public class Sphere extends Object3D {
    private final Vector3D center;
    private final double   radius;

    /** Constructor with Material. */
    public Sphere(Vector3D center, double radius, Material material) {
        super(material);
        this.center = center;
        this.radius = radius;
    }

    /** Constructor with Color (backward compatibility). */
    public Sphere(Vector3D center, double radius, Color color) {
        super(color);
        this.center = center;
        this.radius = radius;
    }

    @Override
    public Intersection intersect(Ray ray) {
        Vector3D oc = ray.origin.subtract(center);
        double a = ray.direction.dot(ray.direction);
        double b = 2.0 * oc.dot(ray.direction);
        double c = oc.dot(oc) - radius * radius;
        double disc = b*b - 4*a*c;
        if (disc < 0) return Intersection.miss();

        double sq = Math.sqrt(disc);
        double t  = (-b - sq) / (2.0*a);
        if (t < 0) t = (-b + sq) / (2.0*a);
        if (t < 0) return Intersection.miss();

        Vector3D point  = ray.pointAt(t);
        Vector3D normal = point.subtract(center).normalize();
        return new Intersection(true, t, point, normal, this);
    }
}
