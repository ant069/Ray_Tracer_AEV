import java.awt.Color;

public class Sphere extends Object3D {
    private final Vector3D center;
    private final double radius;

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
        double discriminant = b * b - 4 * a * c;

        if (discriminant < 0) return Intersection.miss();

        double sqrtDisc = Math.sqrt(discriminant);
        double t0 = (-b - sqrtDisc) / (2.0 * a);
        double t1 = (-b + sqrtDisc) / (2.0 * a);
        double t = (t0 >= 0) ? t0 : t1;
        if (t < 0) return Intersection.miss();

        Vector3D point = ray.pointAt(t);
        Vector3D normal = point.subtract(center).normalize();
        return new Intersection(true, t, point, normal, this);
    }
}