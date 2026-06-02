import java.awt.Color;

/**
 * Triangle with per-vertex normal support (Phong shading) and
 * per-face tangent (for normal mapping).
 * Uses the Möller-Trumbore algorithm for intersection.
 */
public class Triangle extends Object3D {
    private static final double EPSILON = 1e-8;

    private final Vector3D v0, v1, v2;
    private final Vector3D edge1, edge2;
    private final Vector3D faceNormal;
    private final Vector3D faceTangent;  // for normal mapping

    // Per-vertex normals for interpolation (Phong shading). Null = flat shading.
    private final Vector3D n0, n1, n2;

    // UV texture coordinates per vertex.
    private final double[] uv0, uv1, uv2;

    private final boolean singleSided;

    /** Flat shading constructor with Material. */
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Material material) {
        this(v0, v1, v2, null, null, null, null, null, null, material, false);
    }

    /** Phong shading constructor with Material. */
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2,
                    Vector3D n0, Vector3D n1, Vector3D n2, Material material) {
        this(v0, v1, v2, n0, n1, n2, null, null, null, material, false);
    }

    /** Phong shading constructor with Material and backface culling. */
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2,
                    Vector3D n0, Vector3D n1, Vector3D n2, Material material, boolean singleSided) {
        this(v0, v1, v2, n0, n1, n2, null, null, null, material, singleSided);
    }

    /** Full constructor. */
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2,
                    Vector3D n0, Vector3D n1, Vector3D n2,
                    double[] uv0, double[] uv1, double[] uv2,
                    Material material, boolean singleSided) {
        super(material);
        this.v0 = v0; this.v1 = v1; this.v2 = v2;
        this.edge1 = v1.subtract(v0);
        this.edge2 = v2.subtract(v0);
        this.faceNormal = edge1.cross(edge2).normalize();
        this.n0 = (n0 != null) ? n0.normalize() : null;
        this.n1 = (n1 != null) ? n1.normalize() : null;
        this.n2 = (n2 != null) ? n2.normalize() : null;
        this.uv0 = uv0; this.uv1 = uv1; this.uv2 = uv2;
        this.singleSided = singleSided;
        this.faceTangent = computeTangent(uv0, uv1, uv2);
    }

    /** Flat shading constructor with Color. */
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Color color) {
        this(v0, v1, v2, null, null, null, color);
    }

    /** Phong shading constructor with Color. */
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2,
                    Vector3D n0, Vector3D n1, Vector3D n2, Color color) {
        super(color);
        this.v0 = v0; this.v1 = v1; this.v2 = v2;
        this.edge1 = v1.subtract(v0);
        this.edge2 = v2.subtract(v0);
        this.faceNormal = edge1.cross(edge2).normalize();
        this.n0 = (n0 != null) ? n0.normalize() : null;
        this.n1 = (n1 != null) ? n1.normalize() : null;
        this.n2 = (n2 != null) ? n2.normalize() : null;
        this.uv0 = null; this.uv1 = null; this.uv2 = null;
        this.singleSided = false;
        this.faceTangent = null;
    }

    /** Computes the face tangent from UV deltas. */
    private Vector3D computeTangent(double[] uv0, double[] uv1, double[] uv2) {
        if (uv0 == null || uv1 == null || uv2 == null) {
            return arbitraryPerpendicular(faceNormal);
        }
        double du1 = uv1[0] - uv0[0], dv1 = uv1[1] - uv0[1];
        double du2 = uv2[0] - uv0[0], dv2 = uv2[1] - uv0[1];
        double det = du1 * dv2 - du2 * dv1;
        if (Math.abs(det) < 1e-10) return arbitraryPerpendicular(faceNormal);
        double f = 1.0 / det;
        return edge1.multiply(dv2 * f).subtract(edge2.multiply(dv1 * f)).normalize();
    }

    /** Arbitrary vector perpendicular to n (fallback when no UVs are available). */
    private static Vector3D arbitraryPerpendicular(Vector3D n) {
        Vector3D up = (Math.abs(n.y) < 0.9) ? new Vector3D(0, 1, 0) : new Vector3D(1, 0, 0);
        return up.subtract(n.multiply(n.dot(up))).normalize();
    }

    @Override
    public Intersection intersect(Ray ray) {
        Vector3D P   = ray.direction.cross(edge1);
        double   det = edge2.dot(P);

        if (singleSided) {
            if (det > -EPSILON) return Intersection.miss();
        } else {
            if (Math.abs(det) < EPSILON) return Intersection.miss();
        }

        double   invDet = 1.0 / det;
        Vector3D T      = ray.origin.subtract(v0);

        double u = invDet * T.dot(P);
        if (u < 0.0 || u > 1.0) return Intersection.miss();

        Vector3D Q = T.cross(edge2);
        double   v = invDet * ray.direction.dot(Q);
        if (v < 0.0 || (u + v) > 1.0 + EPSILON) return Intersection.miss();

        double t = invDet * Q.dot(edge1);
        if (t < EPSILON) return Intersection.miss();

        Vector3D point = ray.pointAt(t);
        double w = 1.0 - u - v;

        // Per-vertex normal interpolation (Phong) or flat shading
        Vector3D normal;
        if (n0 != null && n1 != null && n2 != null) {
            normal = n0.multiply(w).add(n1.multiply(u)).add(n2.multiply(v)).normalize();
        } else {
            normal = faceNormal;
        }

        // UV interpolation
        double texU = 0, texV = 0;
        if (uv0 != null && uv1 != null && uv2 != null) {
            texU = uv0[0]*w + uv1[0]*u + uv2[0]*v;
            texV = uv0[1]*w + uv1[1]*u + uv2[1]*v;
        }

        // Gram-Schmidt: re-orthogonalize tangent against the interpolated normal
        Vector3D tangent = null;
        if (faceTangent != null) {
            double proj = normal.dot(faceTangent);
            tangent = faceTangent.subtract(normal.multiply(proj)).normalize();
        }

        return new Intersection(true, t, point, normal, this, texU, texV, tangent);
    }

    public Vector3D getFaceNormal() { return faceNormal; }
    public Vector3D getV0()         { return v0; }
    public Vector3D getV1()         { return v1; }
    public Vector3D getV2()         { return v2; }
    public boolean  hasVertexNormals() { return n0 != null; }
    public Vector3D[] getVertices() { return new Vector3D[]{v0, v1, v2}; }
}
