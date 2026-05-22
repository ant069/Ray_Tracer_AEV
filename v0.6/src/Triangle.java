import java.awt.Color;

/**
 * Triangle v0.5 – Phong shading support via per-vertex normals.
 *
 * NEW in v0.5:
 *   Each vertex can carry its own normal (n0, n1, n2) loaded from the OBJ
 *   file's vn entries. When per-vertex normals are present, the normal at
 *   any interior point is computed by barycentric interpolation:
 *
 *     N(u,v) = normalize( (1-u-v)*n0 + u*n1 + v*n2 )
 *
 *   where u, v are the Möller-Trumbore barycentric coordinates already
 *   computed during intersection. This is Phong (normal) interpolation
 *   as described in Session 24 slide 6 and slide 8.
 *
 *   If no per-vertex normals are supplied (null), the triangle falls back
 *   to the flat face normal computed from vertex positions (v0.4 behaviour).
 *
 * Smoothing groups (slide 9–10):
 *   The ObjReader assigns vertex normals only within the same smoothing group.
 *   Vertices on a hard edge (different smoothing groups) keep distinct normals,
 *   preserving the sharp crease. This is handled in ObjModel/ObjReader; the
 *   Triangle itself just stores whatever normals it receives.
 */
public class Triangle extends Object3D {
    private static final double EPSILON = 1e-8;

    // ── vertices ──────────────────────────────────────────────────────────────
    private final Vector3D v0, v1, v2;

    // ── pre-computed edges (Möller-Trumbore) ──────────────────────────────────
    private final Vector3D v1v0; // v1 - v0
    private final Vector3D v2v0; // v2 - v0

    // ── normals ───────────────────────────────────────────────────────────────
    /** Flat face normal – always available, used as fallback. */
    private final Vector3D faceNormal;

    /**
     * Per-vertex normals for Phong interpolation (slide 8).
     * Null when not provided (flat-shading fallback).
     */
    private final Vector3D n0, n1, n2;

    // ── constructors ──────────────────────────────────────────────────────────

    /**
     * Flat-shading constructor (v0.4 compatible).
     * Uses the computed face normal for every point on the triangle.
     */
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Color color) {
        this(v0, v1, v2, null, null, null, color);
    }

    /**
     * Phong-shading constructor (NEW in v0.5).
     *
     * @param v0, v1, v2   triangle vertices
     * @param n0, n1, n2   per-vertex normals (may be null → flat fallback)
     * @param color        object color
     */
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2,
                    Vector3D n0, Vector3D n1, Vector3D n2,
                    Color color) {
        super(color);
        this.v0 = v0;  this.v1 = v1;  this.v2 = v2;
        this.v1v0 = v1.subtract(v0);
        this.v2v0 = v2.subtract(v0);

        // Face normal – slide 10 convention: V = v1-v0, W = v0-v2, N = V×W
        Vector3D V = v1.subtract(v0);
        Vector3D W = v0.subtract(v2);
        this.faceNormal = V.cross(W).normalize();

        // Per-vertex normals (normalize defensively)
        this.n0 = (n0 != null) ? n0.normalize() : null;
        this.n1 = (n1 != null) ? n1.normalize() : null;
        this.n2 = (n2 != null) ? n2.normalize() : null;
    }

    // ── intersection ──────────────────────────────────────────────────────────

    @Override
    public Intersection intersect(Ray ray) {
        // Möller-Trumbore – unchanged from v0.4
        Vector3D P = ray.direction.cross(v1v0);
        double det = v2v0.dot(P);
        if (Math.abs(det) < EPSILON) return Intersection.miss();

        double invDet = 1.0 / det;
        Vector3D T = ray.origin.subtract(v0);

        double u = invDet * T.dot(P);
        if (u < 0.0 || u > 1.0) return Intersection.miss();

        Vector3D Q = T.cross(v2v0);
        double v = invDet * ray.direction.dot(Q);
        if (v < 0.0 || (u + v) > (1.0 + EPSILON)) return Intersection.miss();

        double t = invDet * Q.dot(v1v0);
        if (t < EPSILON) return Intersection.miss();

        Vector3D point = ray.pointAt(t);

        // ── normal selection ──────────────────────────────────────────────────
        // Phong: interpolate per-vertex normals using barycentric coords (u, v).
        //   w = 1 - u - v  (weight of v0)
        //   N = normalize( w*n0 + u*n1 + v*n2 )          (slide 6 / slide 8)
        Vector3D normal;
        if (n0 != null && n1 != null && n2 != null) {
            double w = 1.0 - u - v;
            normal = n0.multiply(w)
                       .add(n1.multiply(u))
                       .add(n2.multiply(v))
                       .normalize();
        } else {
            normal = faceNormal; // flat fallback
        }

        return new Intersection(true, t, point, normal, this);
    }

    // ── accessors ─────────────────────────────────────────────────────────────
    public Vector3D getFaceNormal() { return faceNormal; }
    public Vector3D getV0() { return v0; }
    public Vector3D getV1() { return v1; }
    public Vector3D getV2() { return v2; }
    public boolean  hasVertexNormals() { return n0 != null; }
}
