import java.awt.Color;

/**
 * Triangle primitive using the Möller-Trumbore intersection algorithm.
 *
 * Based on barycentric coordinates:
 *   P = wA + uB + vC,  where w = 1 - u - v
 *   P = A + u(B-A) + v(C-A)
 *
 * The ray equation P = O + tD is substituted:
 *   O - A = -tD + u(B-A) + v(C-A)
 *
 * O-A is viewed as a transformation moving the triangle to the origin (Möller-Trumbore).
 * Cramer's Rule is then applied to solve for [t, u, v].
 *
 * Variables follow the lecture naming:
 *   v0, v1, v2  → triangle vertices (A, B, C)
 *   v1v0        → v1 - v0  (edge B-A)
 *   v2v0        → v2 - v0  (edge C-A)
 *   P           → D × v1v0
 *   determinant → v2v0 · P
 *   T           → O - v0
 *   u           → invDet × (T · P)
 *   Q           → T × v2v0
 *   v           → invDet × (D · Q)
 *   t           → invDet × (Q · v1v0)
 */
public class Triangle extends Object3D {
    private static final double EPSILON = 1e-8;

    // Vertices (lecture notation: v0=A, v1=B, v2=C)
    private final Vector3D v0;
    private final Vector3D v1;
    private final Vector3D v2;

    // Pre-computed edges
    private final Vector3D v1v0; // v1 - v0  (B - A)
    private final Vector3D v2v0; // v2 - v0  (C - A)

    // Pre-computed face normal
    private final Vector3D normal;

    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Color color) {
        super(color);
        this.v0   = v0;
        this.v1   = v1;
        this.v2   = v2;
        this.v1v0 = v1.subtract(v0);   // B - A
        this.v2v0 = v2.subtract(v0);   // C - A
        this.normal = v2v0.cross(v1v0).normalize(); // face normal
    }

    /**
     * Möller-Trumbore ray-triangle intersection.
     *
     * Step 1:  P = D × v1v0  (lecture slide 10)
     * Step 2:  determinant = v2v0 · P  (lecture slide 10)
     *          (near-zero det → ray is parallel to triangle → no hit)
     * Step 3:  invDet = 1 / determinant
     * Step 4:  T = O - v0  (lecture slide 11)
     * Step 5:  u = invDet × (T · P)  (lecture slide 11)
     *          if u < 0 or u > 1 → miss
     * Step 6:  Q = T × v2v0  (lecture slide 12)
     * Step 7:  v = invDet × (D · Q)  (lecture slide 12)
     *          if v < 0 or u+v > 1+ε → miss
     * Step 8:  t = invDet × (Q · v1v0)  — t is the distance along the ray
     */
    @Override
    public Intersection intersect(Ray ray) {
        // Step 1 – P = D × v1v0  (lecture slide 10)
        Vector3D P = ray.direction.cross(v1v0);

        // Step 2 – determinant = v2v0 · P  (lecture slide 10)
        double determinant = v2v0.dot(P);

        // Parallel check: avoid extra operations when det ≈ 0
        if (Math.abs(determinant) < EPSILON) return Intersection.miss();

        // Step 3
        double invDet = 1.0 / determinant;

        // Step 4 – T = O - v0  (lecture slide 11)
        Vector3D T = ray.origin.subtract(v0);

        // Step 5 – u = invDet × (T · P)  (lecture slide 11)
        double u = invDet * T.dot(P);
        if (u < 0.0 || u > 1.0) return Intersection.miss();

        // Step 6 – Q = T × v2v0  (lecture slide 12)
        Vector3D Q = T.cross(v2v0);

        // Step 7 – v = invDet × (D · Q)  (lecture slide 12)
        double v = invDet * ray.direction.dot(Q);
        if (v < 0.0 || (u + v) > (1.0 + EPSILON)) return Intersection.miss();

        // Step 8 – t = invDet × (Q · v1v0)  — t is the distance  (lecture slide 12)
        double t = invDet * Q.dot(v1v0);
        if (t < EPSILON) return Intersection.miss(); // behind the ray origin

        Vector3D point = ray.pointAt(t);
        return new Intersection(true, t, point, normal, this);
    }

    public Vector3D getV0() { return v0; }
    public Vector3D getV1() { return v1; }
    public Vector3D getV2() { return v2; }
}