import java.awt.Color;

/**
 * Triangle con soporte de normales por vértice (Phong shading).
 * Usa el algoritmo de Möller-Trumbore para intersección.
 * Soporta materiales con reflexión y refracción.
 */
public class Triangle extends Object3D {
    private static final double EPSILON = 1e-8;

    private final Vector3D v0, v1, v2;
    private final Vector3D edge1, edge2;   // v1-v0, v2-v0
    private final Vector3D faceNormal;

    // Normales por vértice para interpolación (Phong). Null = flat shading.
    private final Vector3D n0, n1, n2;

    /** Constructor flat shading con Material */
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Material material) {
        this(v0, v1, v2, null, null, null, material);
    }

    /** Constructor Phong shading con Material */
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2,
                    Vector3D n0, Vector3D n1, Vector3D n2, Material material) {
        super(material);
        this.v0 = v0; this.v1 = v1; this.v2 = v2;
        this.edge1 = v1.subtract(v0);
        this.edge2 = v2.subtract(v0);
        this.faceNormal = edge1.cross(edge2).normalize();
        this.n0 = (n0 != null) ? n0.normalize() : null;
        this.n1 = (n1 != null) ? n1.normalize() : null;
        this.n2 = (n2 != null) ? n2.normalize() : null;
    }

    /** Constructor flat shading con Color (compatibilidad hacia atrás) */
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Color color) {
        this(v0, v1, v2, null, null, null, color);
    }

    /** Constructor Phong shading con Color (compatibilidad hacia atrás) */
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
    }

    @Override
    public Intersection intersect(Ray ray) {
        Vector3D P   = ray.direction.cross(edge1);
        double   det = edge2.dot(P);
        if (Math.abs(det) < EPSILON) return Intersection.miss();

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

        // Interpolación de normal por vértice (Phong) o flat
        Vector3D normal;
        if (n0 != null && n1 != null && n2 != null) {
            double w = 1.0 - u - v;
            normal = n0.multiply(w).add(n1.multiply(u)).add(n2.multiply(v)).normalize();
        } else {
            normal = faceNormal;
        }

        return new Intersection(true, t, point, normal, this);
    }

    public Vector3D getFaceNormal() { return faceNormal; }
    public Vector3D getV0() { return v0; }
    public Vector3D getV1() { return v1; }
    public Vector3D getV2() { return v2; }
    public boolean  hasVertexNormals() { return n0 != null; }
}
