public class Intersection {
    public final boolean   hit;
    public final double    distance;
    public final Vector3D  point;
    public final Vector3D  normal;
    public final Object3D  object;
    public final double    uvU, uvV;
    public final Vector3D  tangent;   // for normal mapping (may be null)

    public Intersection(boolean hit, double distance,
                        Vector3D point, Vector3D normal, Object3D object,
                        double uvU, double uvV, Vector3D tangent) {
        this.hit      = hit;
        this.distance = distance;
        this.point    = point;
        this.normal   = normal;
        this.object   = object;
        this.uvU      = uvU;
        this.uvV      = uvV;
        this.tangent  = tangent;
    }

    /** Backward-compatible — tangent defaults to null. */
    public Intersection(boolean hit, double distance,
                        Vector3D point, Vector3D normal, Object3D object,
                        double uvU, double uvV) {
        this(hit, distance, point, normal, object, uvU, uvV, null);
    }

    /** Backward-compatible — UV and tangent default to zero/null. */
    public Intersection(boolean hit, double distance,
                        Vector3D point, Vector3D normal, Object3D object) {
        this(hit, distance, point, normal, object, 0.0, 0.0, null);
    }

    public static Intersection miss() {
        return new Intersection(false, Double.POSITIVE_INFINITY, null, null, null, 0.0, 0.0, null);
    }
}
