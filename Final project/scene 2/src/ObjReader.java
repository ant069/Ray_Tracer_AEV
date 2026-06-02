import java.io.*;
import java.util.*;

/**
 * OBJ file parser.
 * Supports: v, vn, vt, f (triangles and quads), o, g, s, mtllib, usemtl.
 * If the OBJ has no normals (vn), computes them automatically by averaging
 * face normals at each shared vertex (automatic smooth shading).
 */
public class ObjReader {

    private static class FaceIdx {
        int v = -1, vt = -1, vn = -1;
    }

    public static ObjModel parse(String path) throws IOException {
        List<Vector3D> verts    = new ArrayList<>();
        List<Vector3D> normals  = new ArrayList<>();
        List<double[]> texCoords= new ArrayList<>();
        List<int[]>    triV     = new ArrayList<>();
        List<int[]>    triVN    = new ArrayList<>();
        List<int[]>    triVT    = new ArrayList<>();
        List<Integer>  triSG    = new ArrayList<>();
        List<String>   triMat   = new ArrayList<>();

        int    currentSG  = 0;
        String currentMat = "";

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                int hi = line.indexOf('#');
                if (hi >= 0) line = line.substring(0, hi);
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] tok = line.split("\\s+");
                if (tok.length == 0) continue;

                switch (tok[0]) {
                    case "v":
                        if (tok.length >= 4)
                            verts.add(new Vector3D(
                                Double.parseDouble(tok[1]),
                                Double.parseDouble(tok[2]),
                                Double.parseDouble(tok[3])));
                        break;
                    case "vn":
                        if (tok.length >= 4)
                            normals.add(new Vector3D(
                                Double.parseDouble(tok[1]),
                                Double.parseDouble(tok[2]),
                                Double.parseDouble(tok[3])).normalize());
                        break;
                    case "vt":
                        if (tok.length >= 3)
                            texCoords.add(new double[]{
                                Double.parseDouble(tok[1]),
                                Double.parseDouble(tok[2])});
                        break;
                    case "usemtl":
                        currentMat = tok.length >= 2 ? line.substring("usemtl".length()).trim() : "";
                        break;
                    case "s":
                        if (tok.length < 2 || tok[1].equalsIgnoreCase("off") || tok[1].equals("0"))
                            currentSG = 0;
                        else {
                            try { currentSG = Integer.parseInt(tok[1]); }
                            catch (NumberFormatException e) { currentSG = 0; }
                        }
                        break;
                    case "f":
                        int nv = tok.length - 1;
                        if (nv < 3) break;
                        FaceIdx[] fi = new FaceIdx[nv];
                        for (int i = 0; i < nv; i++)
                            fi[i] = parseIdx(tok[i+1], verts.size(), texCoords.size(), normals.size());
                        // Fan triangulation
                        for (int i = 1; i < nv - 1; i++) {
                            FaceIdx a = fi[0], b_ = fi[i], c = fi[i+1];
                            triV.add( new int[]{ a.v,  b_.v,  c.v  });
                            triVN.add(new int[]{ a.vn, b_.vn, c.vn });
                            triVT.add(new int[]{ a.vt, b_.vt, c.vt });
                            triSG.add(currentSG);
                            triMat.add(currentMat);
                        }
                        break;
                    default: break;
                }
            }
        }

        System.out.printf("[ObjReader] %d verts, %d normals, %d tris%n",
            verts.size(), normals.size(), triV.size());

        // Auto-compute smooth vertex normals if the OBJ has no vn entries
        if (normals.isEmpty()) {
            normals = computeSmoothedNormals(verts, triV);
            for (int i = 0; i < triV.size(); i++) {
                int[] vi = triV.get(i);
                triVN.set(i, new int[]{ vi[0], vi[1], vi[2] });
            }
            System.out.println("[ObjReader] Normals computed automatically (smooth).");
        }

        return new ObjModel(verts, normals, texCoords, triV, triVN, triVT, triSG, triMat);
    }

    /**
     * Computes smoothed per-vertex normals by averaging face normals
     * of all triangles sharing each vertex.
     */
    private static List<Vector3D> computeSmoothedNormals(
            List<Vector3D> verts, List<int[]> triV) {

        Vector3D[] accum = new Vector3D[verts.size()];
        for (int i = 0; i < accum.length; i++)
            accum[i] = new Vector3D(0, 0, 0);

        for (int[] vi : triV) {
            Vector3D a = verts.get(vi[0]);
            Vector3D b = verts.get(vi[1]);
            Vector3D c = verts.get(vi[2]);
            Vector3D fn = b.subtract(a).cross(c.subtract(a)).normalize();

            // Weight by interior angle at each vertex — more accurate than area weighting
            double ang0 = angle(b.subtract(a), c.subtract(a));
            double ang1 = angle(a.subtract(b), c.subtract(b));
            double ang2 = angle(a.subtract(c), b.subtract(c));
            accum[vi[0]] = accum[vi[0]].add(fn.multiply(ang0));
            accum[vi[1]] = accum[vi[1]].add(fn.multiply(ang1));
            accum[vi[2]] = accum[vi[2]].add(fn.multiply(ang2));
        }

        List<Vector3D> result = new ArrayList<>();
        for (Vector3D n : accum)
            result.add(n.normalize());
        return result;
    }

    private static double angle(Vector3D u, Vector3D v) {
        double d = u.length() * v.length();
        if (d < 1e-10) return 0.0;
        return Math.acos(Math.max(-1.0, Math.min(1.0, u.dot(v) / d)));
    }

    private static FaceIdx parseIdx(String tok, int nv, int nvt, int nvn) {
        FaceIdx fi = new FaceIdx();
        String[] p = tok.split("/", -1);
        if (p.length >= 1 && !p[0].isEmpty()) fi.v  = resolve(Integer.parseInt(p[0]), nv);
        if (p.length >= 2 && !p[1].isEmpty()) fi.vt = resolve(Integer.parseInt(p[1]), nvt);
        if (p.length >= 3 && !p[2].isEmpty()) fi.vn = resolve(Integer.parseInt(p[2]), nvn);
        return fi;
    }

    private static int resolve(int idx, int size) {
        if (idx > 0) return idx - 1;
        if (idx < 0) return size + idx;
        return 0;
    }
}
