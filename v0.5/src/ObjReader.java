import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ObjReader v0.5 – adds smoothing-group tracking per triangle.
 *
 * NEW in v0.5:
 *   The current smoothing group (integer) is recorded for every triangle
 *   produced from an "f" line. The value comes from the most recent "s"
 *   directive:
 *     s off  or  s 0  → smoothing group 0 (disabled)
 *     s N            → smoothing group N  (positive integer)
 *
 *   This list is stored in ObjModel.triSG and used by ObjModel.addToScene()
 *   to decide which vertices share a smoothing group (slide 10:
 *   "Only interpolate vertices in the same smoothing group").
 *
 * Everything else is identical to v0.4 (v, vn, vt, f, o, g parsing).
 */
public class ObjReader {

    private static class FaceIndex {
        int v  = -1;
        int vt = -1;
        int vn = -1;
    }

    public static ObjModel parse(String filePath) throws IOException {

        List<Vector3D> vertices  = new ArrayList<>();
        List<Vector3D> normals   = new ArrayList<>();
        List<double[]> texCoords = new ArrayList<>();

        List<int[]>   triV   = new ArrayList<>();
        List<int[]>   triVN  = new ArrayList<>();
        List<int[]>   triVT  = new ArrayList<>();
        List<Integer> triSG  = new ArrayList<>();  // NEW: smoothing group per tri

        int currentSG  = 0;   // 0 = smoothing off
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                int hashPos = line.indexOf('#');
                if (hashPos >= 0) line = line.substring(0, hashPos);
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] tokens = line.split("\\s+");
                if (tokens.length == 0) continue;

                switch (tokens[0]) {

                    case "v": {
                        if (tokens.length < 4) { warn(lineNumber, "v needs x y z"); break; }
                        vertices.add(new Vector3D(
                            parseDouble(tokens[1]),
                            parseDouble(tokens[2]),
                            parseDouble(tokens[3])));
                        break;
                    }

                    case "vn": {
                        if (tokens.length < 4) { warn(lineNumber, "vn needs x y z"); break; }
                        normals.add(new Vector3D(
                            parseDouble(tokens[1]),
                            parseDouble(tokens[2]),
                            parseDouble(tokens[3])).normalize());
                        break;
                    }

                    case "vt": {
                        if (tokens.length < 3) { warn(lineNumber, "vt needs u v"); break; }
                        texCoords.add(new double[]{
                            parseDouble(tokens[1]),
                            parseDouble(tokens[2])});
                        break;
                    }

                    case "o":
                        System.out.println("[ObjReader] Object: " +
                            (tokens.length > 1 ? tokens[1] : ""));
                        break;

                    case "g":
                        System.out.println("[ObjReader] Group: " +
                            (tokens.length > 1 ? tokens[1] : ""));
                        break;

                    // ── smoothing group (NEW in v0.5) ─────────────────────────
                    case "s": {
                        if (tokens.length < 2
                                || tokens[1].equalsIgnoreCase("off")
                                || tokens[1].equals("0")) {
                            currentSG = 0;   // smoothing disabled
                        } else {
                            try {
                                currentSG = Integer.parseInt(tokens[1]);
                            } catch (NumberFormatException e) {
                                currentSG = 0;
                            }
                        }
                        break;
                    }

                    case "f": {
                        int faceVertCount = tokens.length - 1;
                        if (faceVertCount < 3) {
                            warn(lineNumber, "face < 3 verts, skipping"); break;
                        }
                        FaceIndex[] fi = new FaceIndex[faceVertCount];
                        for (int i = 0; i < faceVertCount; i++) {
                            fi[i] = parseFaceIndex(tokens[i + 1],
                                vertices.size(), texCoords.size(), normals.size(),
                                lineNumber);
                        }
                        // Fan triangulation
                        for (int i = 1; i < faceVertCount - 1; i++) {
                            FaceIndex a = fi[0], b = fi[i], c = fi[i + 1];
                            triV.add( new int[]{ a.v,  b.v,  c.v  });
                            triVN.add(new int[]{ a.vn, b.vn, c.vn });
                            triVT.add(new int[]{ a.vt, b.vt, c.vt });
                            triSG.add(currentSG);   // record smoothing group
                        }
                        break;
                    }

                    case "mtllib":
                    case "usemtl":
                        break;  // materials out of scope

                    default:
                        break;
                }
            }
        }

        System.out.printf("[ObjReader] Parsed: %d verts, %d normals, " +
                          "%d texcoords, %d triangles%n",
                          vertices.size(), normals.size(),
                          texCoords.size(), triV.size());

        return new ObjModel(vertices, normals, texCoords,
                            triV, triVN, triVT, triSG);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static FaceIndex parseFaceIndex(String token,
            int vCount, int vtCount, int vnCount, int lineNumber) {
        FaceIndex fi = new FaceIndex();
        String[] parts = token.split("/", -1);
        if (parts.length >= 1 && !parts[0].isEmpty())
            fi.v  = resolveIndex(parseInt(parts[0]), vCount);
        if (parts.length >= 2 && !parts[1].isEmpty())
            fi.vt = resolveIndex(parseInt(parts[1]), vtCount);
        if (parts.length >= 3 && !parts[2].isEmpty())
            fi.vn = resolveIndex(parseInt(parts[2]), vnCount);
        return fi;
    }

    private static int resolveIndex(int objIndex, int listSize) {
        if (objIndex > 0) return objIndex - 1;
        if (objIndex < 0) return listSize + objIndex;
        return 0;
    }

    private static double parseDouble(String s) { return Double.parseDouble(s.trim()); }
    private static int    parseInt(String s)     { return Integer.parseInt(s.trim()); }
    private static void   warn(int line, String msg) {
        System.err.println("[ObjReader] WARNING line " + line + ": " + msg);
    }
}
