import java.awt.Color;
import java.util.*;

/**
 * ObjModel v0.5 – adds per-vertex normal support for Phong shading.
 *
 * NEW in v0.5:
 *   addToScene() now constructs Triangle objects with per-vertex normals
 *   (n0, n1, n2) read from the OBJ vn entries, enabling Phong (normal)
 *   interpolation at render time.
 *
 *   When a face has no vn indices (triVN entries are -1), the triangle is
 *   created without vertex normals and falls back to flat shading.
 *
 *   Smoothing groups (slide 9-10):
 *   The ObjReader already stores the smoothing group per face in triSG.
 *   addToScene() uses this to build averaged vertex normals: for each
 *   vertex, it sums the face normals of all adjacent faces in the SAME
 *   smoothing group, then normalizes. This matches the formula from slide 6:
 *     n = (n1+n2+n3+n4) / |n1+n2+n3+n4|
 *   Faces with smoothing off (sg == 0) always get flat normals (no averaging).
 */
public class ObjModel {

    // ── raw parsed data ───────────────────────────────────────────────────────
    private final List<Vector3D> vertices;
    private final List<Vector3D> normals;    // vn entries from OBJ
    private final List<double[]> texCoords;

    // per-triangle index arrays (all parallel, one entry per triangle)
    private final List<int[]> triV;   // vertex indices       [3]
    private final List<int[]> triVN;  // normal indices       [3], -1 = absent
    private final List<int[]> triVT;  // texcoord indices     [3], -1 = absent
    private final List<Integer> triSG; // smoothing group per triangle (0 = off)

    // ── constructor ───────────────────────────────────────────────────────────
    public ObjModel(List<Vector3D> vertices,
                    List<Vector3D> normals,
                    List<double[]> texCoords,
                    List<int[]>    triV,
                    List<int[]>    triVN,
                    List<int[]>    triVT,
                    List<Integer>  triSG) {
        this.vertices  = vertices;
        this.normals   = normals;
        this.texCoords = texCoords;
        this.triV      = triV;
        this.triVN     = triVN;
        this.triVT     = triVT;
        this.triSG     = triSG;
    }

    // ── scene integration ─────────────────────────────────────────────────────

    /**
     * Adds all triangles to the scene with Phong-ready per-vertex normals.
     *
     * Strategy (slide 9-10):
     *   1. If the OBJ provides vn indices for a face → use those directly.
     *   2. If vn indices are absent → compute averaged vertex normals from
     *      adjacent faces in the same smoothing group (Phong interpolation).
     *   3. If smoothing group == 0 (off) → use flat face normal (no blending).
     *
     * @param scene   target scene
     * @param color   color for all triangles
     * @param offset  world-space translation
     * @param scale   uniform scale factor
     */
    public void addToScene(Scene scene, Color color, Vector3D offset, double scale) {

        // Build averaged per-vertex normals grouped by smoothing group.
        // Key: vertex index.  Value: map from smoothing-group → accumulated normal.
        Map<Integer, Map<Integer, Vector3D>> vertexSGNormals = new HashMap<>();

        for (int i = 0; i < triV.size(); i++) {
            int sg = triSG.get(i);
            if (sg == 0) continue; // smoothing off → skip averaging for this face

            int[] vi = triV.get(i);
            // Compute face normal from geometry (not from OBJ vn)
            Vector3D a = vertices.get(vi[0]);
            Vector3D b = vertices.get(vi[1]);
            Vector3D c = vertices.get(vi[2]);
            Vector3D faceN = b.subtract(a).cross(c.subtract(a)).normalize();

            // Accumulate into each of the 3 corner vertices for this SG
            for (int corner : vi) {
                vertexSGNormals
                    .computeIfAbsent(corner, k -> new HashMap<>())
                    .merge(sg, faceN, Vector3D::add);
            }
        }

        // Now build Triangle objects
        for (int i = 0; i < triV.size(); i++) {
            int[] vi  = triV.get(i);
            int[] vni = triVN.get(i);
            int   sg  = triSG.get(i);

            Vector3D a = transform(vertices.get(vi[0]), offset, scale);
            Vector3D b = transform(vertices.get(vi[1]), offset, scale);
            Vector3D c = transform(vertices.get(vi[2]), offset, scale);

            Vector3D pn0 = null, pn1 = null, pn2 = null;

            if (vni[0] >= 0 && vni[1] >= 0 && vni[2] >= 0) {
                // Case 1: OBJ supplies explicit vn indices → use them directly
                pn0 = normals.get(vni[0]);
                pn1 = normals.get(vni[1]);
                pn2 = normals.get(vni[2]);

            } else if (sg != 0) {
                // Case 2: No explicit vn but smoothing is on →
                //         use averaged vertex normals for this smoothing group
                // n = (n1+n2+...+nk) / |n1+n2+...+nk|   (slide 6)
                Map<Integer, Vector3D> m0 = vertexSGNormals.get(vi[0]);
                Map<Integer, Vector3D> m1 = vertexSGNormals.get(vi[1]);
                Map<Integer, Vector3D> m2 = vertexSGNormals.get(vi[2]);

                if (m0 != null && m1 != null && m2 != null
                        && m0.containsKey(sg) && m1.containsKey(sg) && m2.containsKey(sg)) {
                    pn0 = m0.get(sg).normalize();
                    pn1 = m1.get(sg).normalize();
                    pn2 = m2.get(sg).normalize();
                }
                // else: fall through to flat (pn* stay null)
            }
            // Case 3: sg == 0 → pn* remain null → Triangle uses flat normal

            scene.addObject(new Triangle(a, b, c, pn0, pn1, pn2, color));
        }
    }

    /** Convenience overload – no offset, no scale change. */
    public void addToScene(Scene scene, Color color) {
        addToScene(scene, color, new Vector3D(0, 0, 0), 1.0);
    }

    // ── transform helper ──────────────────────────────────────────────────────
    private Vector3D transform(Vector3D v, Vector3D offset, double scale) {
        return new Vector3D(
            v.x * scale + offset.x,
            v.y * scale + offset.y,
            v.z * scale + offset.z
        );
    }

    // ── accessors ─────────────────────────────────────────────────────────────
    public List<Vector3D> getVertices()   { return vertices;  }
    public List<Vector3D> getNormals()    { return normals;   }
    public List<double[]> getTexCoords()  { return texCoords; }
    public List<int[]>    getTriV()       { return triV;      }
    public List<int[]>    getTriVN()      { return triVN;     }
    public List<int[]>    getTriVT()      { return triVT;     }
    public List<Integer>  getTriSG()      { return triSG;     }
    public int vertexCount()              { return vertices.size();  }
    public int normalCount()              { return normals.size();   }
    public int triangleCount()            { return triV.size();      }

    public Vector3D[] boundingBox() {
        if (vertices.isEmpty())
            return new Vector3D[]{ new Vector3D(0,0,0), new Vector3D(0,0,0) };
        double minX=Double.MAX_VALUE, minY=Double.MAX_VALUE, minZ=Double.MAX_VALUE;
        double maxX=-Double.MAX_VALUE, maxY=-Double.MAX_VALUE, maxZ=-Double.MAX_VALUE;
        for (Vector3D v : vertices) {
            if (v.x<minX) minX=v.x; if (v.x>maxX) maxX=v.x;
            if (v.y<minY) minY=v.y; if (v.y>maxY) maxY=v.y;
            if (v.z<minZ) minZ=v.z; if (v.z>maxZ) maxZ=v.z;
        }
        return new Vector3D[]{ new Vector3D(minX,minY,minZ), new Vector3D(maxX,maxY,maxZ) };
    }

    public void printSummary() {
        Vector3D[] bb = boundingBox();
        System.out.printf("[ObjModel] %d vertices | %d normals | %d triangles%n",
                          vertexCount(), normalCount(), triangleCount());
        System.out.printf("[ObjModel] AABB min=%s  max=%s%n", bb[0], bb[1]);
    }
}
