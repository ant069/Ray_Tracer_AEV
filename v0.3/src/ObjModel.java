import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * ObjModel – holds the raw parsed data from an OBJ file and provides
 * a helper to convert the model into {@link Triangle} objects for the Scene.
 *
 * All index lists use 0-based indexing (already resolved by ObjReader).
 */
public class ObjModel {

    // ── raw data ──────────────────────────────────────────────────────────────
    private final List<Vector3D>  vertices;    // geometric vertices
    private final List<Vector3D>  normals;     // vertex normals (may be empty)
    private final List<double[]>  texCoords;   // UV coordinates  (may be empty)

    // Triangulated faces – parallel lists (one entry per triangle)
    private final List<int[]>     triV;        // vertex indices  [3]
    private final List<int[]>     triVN;       // normal indices  [3] (-1 = absent)
    private final List<int[]>     triVT;       // texcoord indices[3] (-1 = absent)

    // ── constructor (called by ObjReader) ─────────────────────────────────────
    public ObjModel(List<Vector3D> vertices,
                    List<Vector3D> normals,
                    List<double[]> texCoords,
                    List<int[]>    triV,
                    List<int[]>    triVN,
                    List<int[]>    triVT) {
        this.vertices  = vertices;
        this.normals   = normals;
        this.texCoords = texCoords;
        this.triV      = triV;
        this.triVN     = triVN;
        this.triVT     = triVT;
    }

    // ── scene integration ─────────────────────────────────────────────────────

    /**
     * Converts all triangulated faces into {@link Triangle} objects,
     * applies a translation + uniform scale, and adds them to the given scene.
     *
     * @param scene      target scene
     * @param color      colour to assign to every triangle
     * @param offset     world-space translation (moves the model's origin)
     * @param scale      uniform scale factor
     */
    public void addToScene(Scene scene, Color color,
                           Vector3D offset, double scale) {
        for (int i = 0; i < triV.size(); i++) {
            int[] vi = triV.get(i);

            // Look up and transform the three corner vertices
            Vector3D a = transform(vertices.get(vi[0]), offset, scale);
            Vector3D b = transform(vertices.get(vi[1]), offset, scale);
            Vector3D c = transform(vertices.get(vi[2]), offset, scale);

            scene.addObject(new Triangle(a, b, c, color));
        }
    }

    /**
     * Convenience overload – no offset, no scale change.
     */
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

    public List<Vector3D> getVertices()  { return vertices;  }
    public List<Vector3D> getNormals()   { return normals;   }
    public List<double[]> getTexCoords() { return texCoords; }
    public List<int[]>    getTriV()      { return triV;      }
    public List<int[]>    getTriVN()     { return triVN;     }
    public List<int[]>    getTriVT()     { return triVT;     }

    public int vertexCount()  { return vertices.size();  }
    public int normalCount()  { return normals.size();   }
    public int triangleCount(){ return triV.size();      }

    /**
     * Returns the axis-aligned bounding box of the model as two vectors:
     * [0] = min corner, [1] = max corner.
     */
    public Vector3D[] boundingBox() {
        if (vertices.isEmpty()) {
            return new Vector3D[]{ new Vector3D(0,0,0), new Vector3D(0,0,0) };
        }
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (Vector3D v : vertices) {
            if (v.x < minX) minX = v.x;  if (v.x > maxX) maxX = v.x;
            if (v.y < minY) minY = v.y;  if (v.y > maxY) maxY = v.y;
            if (v.z < minZ) minZ = v.z;  if (v.z > maxZ) maxZ = v.z;
        }
        return new Vector3D[]{
            new Vector3D(minX, minY, minZ),
            new Vector3D(maxX, maxY, maxZ)
        };
    }

    /** Prints a short summary to stdout. */
    public void printSummary() {
        Vector3D[] bb = boundingBox();
        System.out.printf("[ObjModel] %d vertices | %d normals | %d triangles%n",
                          vertexCount(), normalCount(), triangleCount());
        System.out.printf("[ObjModel] AABB min=%s  max=%s%n", bb[0], bb[1]);
    }
}
