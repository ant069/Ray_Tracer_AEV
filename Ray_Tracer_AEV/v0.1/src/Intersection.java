public class Intersection {
    public final boolean hit;
    public final double distance;
    public final Vector3D point;
    public final Vector3D normal;
    public final Object3D object;

    public Intersection(boolean hit, double distance, Vector3D point, Vector3D normal, Object3D object) {
        this.hit = hit;
        this.distance = distance;
        this.point = point;
        this.normal = normal;
        this.object = object;
    }

    public static Intersection miss() {
        return new Intersection(false, Double.POSITIVE_INFINITY, null, null, null);
    }
}
