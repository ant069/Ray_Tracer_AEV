import java.awt.Color;
import java.util.*;

/**
 * Modelo OBJ parseado. Permite añadirlo a la escena con transformación
 * (offset + scale uniforme) y normales por vértice para Phong shading.
 * Soporta tanto Color como Material.
 */
public class ObjModel {
    private final List<Vector3D> vertices;
    private final List<Vector3D> normals;
    private final List<int[]>    triV, triVN;
    public ObjModel(List<Vector3D> vertices, List<Vector3D> normals,
                    List<double[]> texCoords,
                    List<int[]> triV, List<int[]> triVN, List<int[]> triVT,
                    List<Integer> triSG) {
        this.vertices  = vertices;
        this.normals   = normals;
        this.triV      = triV;
        this.triVN     = triVN;
    }

    /**
     * Añade todos los triángulos del modelo a la escena con Material.
     *
     * @param scene    escena destino
     * @param material material del modelo
     * @param offset   traslación en espacio mundo
     * @param scale    escala uniforme
     */
    public void addToScene(Scene scene, Material material, Vector3D offset, double scale) {
        for (int i = 0; i < triV.size(); i++) {
            int[] vi  = triV.get(i);
            int[] vni = triVN.get(i);

            Vector3D a = xform(vertices.get(vi[0]), offset, scale);
            Vector3D b = xform(vertices.get(vi[1]), offset, scale);
            Vector3D c = xform(vertices.get(vi[2]), offset, scale);

            // Usar normales por vértice si están disponibles
            Vector3D pn0 = null, pn1 = null, pn2 = null;
            if (vni[0] >= 0 && vni[1] >= 0 && vni[2] >= 0 && !normals.isEmpty()) {
                pn0 = normals.get(Math.min(vni[0], normals.size()-1));
                pn1 = normals.get(Math.min(vni[1], normals.size()-1));
                pn2 = normals.get(Math.min(vni[2], normals.size()-1));
            }

            scene.addObject(new Triangle(a, b, c, pn0, pn1, pn2, material));
        }
    }

    /**
     * Añade todos los triángulos del modelo a la escena con Color.
     * Compatibilidad hacia atrás.
     */
    public void addToScene(Scene scene, Color color, Vector3D offset, double scale) {
        addToScene(scene, Material.diffuse(color), offset, scale);
    }

    /** Sin transformación con Material (offset=0, scale=1). */
    public void addToScene(Scene scene, Material material) {
        addToScene(scene, material, new Vector3D(0,0,0), 1.0);
    }

    /** Sin transformación con Color (offset=0, scale=1). */
    public void addToScene(Scene scene, Color color) {
        addToScene(scene, color, new Vector3D(0,0,0), 1.0);
    }

    private Vector3D xform(Vector3D v, Vector3D off, double s) {
        return new Vector3D(v.x*s + off.x, v.y*s + off.y, v.z*s + off.z);
    }

    public int vertexCount()   { return vertices.size(); }
    public int triangleCount() { return triV.size(); }

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
        System.out.printf("[ObjModel] %d vértices | %d triángulos%n",
            vertexCount(), triangleCount());
        System.out.printf("[ObjModel] BB min=%s  max=%s%n", bb[0], bb[1]);
        Vector3D c = bb[0].add(bb[1]).multiply(0.5);
        System.out.printf("[ObjModel] Centro: %s%n", c);
    }
}
