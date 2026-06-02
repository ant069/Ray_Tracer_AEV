import java.awt.Color;
import java.util.*;

/**
 * Parsed OBJ model. Allows adding it to the scene with a transform
 * (offset + uniform scale) and per-vertex normals for Phong shading.
 * Supports both Color and Material, and per-group materials (usemtl).
 */
public class ObjModel {
    private final List<Vector3D>  vertices;
    private final List<Vector3D>  normals;
    private final List<double[]>  texCoords;
    private final List<int[]>     triV, triVN, triVT;
    private final List<String>    triMat;   // material name per triangle

    public ObjModel(List<Vector3D> vertices, List<Vector3D> normals,
                    List<double[]> texCoords,
                    List<int[]> triV, List<int[]> triVN, List<int[]> triVT,
                    List<Integer> triSG, List<String> triMat) {
        this.vertices  = vertices;
        this.normals   = normals;
        this.texCoords = (texCoords != null) ? texCoords : Collections.emptyList();
        this.triV      = triV;
        this.triVN     = triVN;
        this.triVT     = (triVT != null) ? triVT : Collections.emptyList();
        this.triMat    = (triMat != null) ? triMat : Collections.emptyList();
    }

    /** Adds all triangles with a single material. */
    public void addToScene(Scene scene, Material material, Vector3D offset, double scale) {
        addToScene(scene, Collections.emptyMap(), material, offset, scale);
    }

    /**
     * Adds triangles assigning materials by group name (usemtl).
     * If a triangle's material name is not in the map, uses defaultMat.
     *
     * @param matMap     material-name → Material map
     * @param defaultMat fallback material for groups not in the map
     */
    public void addToScene(Scene scene, Map<String, Material> matMap,
                           Material defaultMat, Vector3D offset, double scale) {
        for (int i = 0; i < triV.size(); i++) {
            int[] vi  = triV.get(i);
            int[] vni = triVN.get(i);

            Vector3D a = xform(vertices.get(vi[0]), offset, scale);
            Vector3D b = xform(vertices.get(vi[1]), offset, scale);
            Vector3D c = xform(vertices.get(vi[2]), offset, scale);

            Vector3D pn0 = null, pn1 = null, pn2 = null;
            if (vni[0] >= 0 && vni[1] >= 0 && vni[2] >= 0 && !normals.isEmpty()) {
                pn0 = normals.get(Math.min(vni[0], normals.size()-1));
                pn1 = normals.get(Math.min(vni[1], normals.size()-1));
                pn2 = normals.get(Math.min(vni[2], normals.size()-1));
            }

            // UV texture coordinates per vertex
            double[] tUV0 = null, tUV1 = null, tUV2 = null;
            if (i < triVT.size() && !texCoords.isEmpty()) {
                int[] vti = triVT.get(i);
                if (vti[0] >= 0 && vti[1] >= 0 && vti[2] >= 0) {
                    tUV0 = texCoords.get(Math.min(vti[0], texCoords.size()-1));
                    tUV1 = texCoords.get(Math.min(vti[1], texCoords.size()-1));
                    tUV2 = texCoords.get(Math.min(vti[2], texCoords.size()-1));
                }
            }

            String  matName = (i < triMat.size()) ? triMat.get(i) : "";
            Material mat    = matMap.getOrDefault(matName, defaultMat);

            scene.addObject(new Triangle(a, b, c, pn0, pn1, pn2, tUV0, tUV1, tUV2, mat, true));
        }
    }

    /**
     * Adds all triangles to the scene with a Color.
     * Backward compatibility overload.
     */
    public void addToScene(Scene scene, Color color, Vector3D offset, double scale) {
        addToScene(scene, Material.diffuse(color), offset, scale);
    }

    /** No transform with Material (offset=0, scale=1). */
    public void addToScene(Scene scene, Material material) {
        addToScene(scene, material, new Vector3D(0,0,0), 1.0);
    }

    /** No transform with Color (offset=0, scale=1). */
    public void addToScene(Scene scene, Color color) {
        addToScene(scene, color, new Vector3D(0,0,0), 1.0);
    }

    private Vector3D xform(Vector3D v, Vector3D off, double s) {
        return new Vector3D(v.x*s + off.x, v.y*s + off.y, v.z*s + off.z);
    }

    public int vertexCount()   { return vertices.size(); }
    public int triangleCount() { return triV.size(); }

    /**
     * Centroid (average of all vertices).
     * More robust than bounding-box center in the presence of outlier vertices.
     */
    public Vector3D centroid() {
        if (vertices.isEmpty()) return new Vector3D(0, 0, 0);
        double sx = 0, sy = 0, sz = 0;
        for (Vector3D v : vertices) { sx += v.x; sy += v.y; sz += v.z; }
        int n = vertices.size();
        return new Vector3D(sx / n, sy / n, sz / n);
    }

    public Vector3D[] boundingBox() {
        if (vertices.isEmpty())
            return new Vector3D[]{ new Vector3D(0,0,0), new Vector3D(0,0,0) };
        double mnX=Double.MAX_VALUE, mnY=Double.MAX_VALUE, mnZ=Double.MAX_VALUE;
        double mxX=-Double.MAX_VALUE, mxY=-Double.MAX_VALUE, mxZ=-Double.MAX_VALUE;
        for (Vector3D v : vertices) {
            if (v.x<mnX) mnX=v.x; if (v.x>mxX) mxX=v.x;
            if (v.y<mnY) mnY=v.y; if (v.y>mxY) mxY=v.y;
            if (v.z<mnZ) mnZ=v.z; if (v.z>mxZ) mxZ=v.z;
        }
        return new Vector3D[]{ new Vector3D(mnX,mnY,mnZ), new Vector3D(mxX,mxY,mxZ) };
    }

    public void printSummary() {
        Vector3D[] bb = boundingBox();
        System.out.printf("[ObjModel] %d vertices | %d triangles%n",
            vertexCount(), triangleCount());
        System.out.printf("[ObjModel] BB min=%s  max=%s%n", bb[0], bb[1]);
        Vector3D c = bb[0].add(bb[1]).multiply(0.5);
        System.out.printf("[ObjModel] Center: %s%n", c);
    }
}
