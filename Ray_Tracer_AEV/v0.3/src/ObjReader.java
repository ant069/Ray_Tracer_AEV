import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ObjReader – custom OBJ file parser (no external libraries).
 *
 * Supported OBJ tokens (from Session 22 slide 3):
 *   v   x y z          → geometric vertex
 *   vn  x y z          → vertex normal
 *   vt  u v            → texture coordinate
 *   o   name           → object name (stored for info, not used in geometry)
 *   g   name           → group name  (stored for info, not used in geometry)
 *   s   1|off          → smooth group flag (stored; shading uses per-face normal for now)
 *   f   ...            → face (triangles or quads, see below)
 *   #   ...            → comment line – ignored
 *
 * Face formats supported (slide 4):
 *   f v v v            → vertex indices only
 *   f v/vt v/vt v/vt   → vertex + texture
 *   f v//vn v//vn v//vn → vertex + normal
 *   f v/vt/vn ...      → vertex + texture + normal
 *
 * Quads (4 vertices per face) are automatically triangulated using the
 * fan method taught in the Polygons slide (slide 6):
 *   Triangle 1 → v0 v1 v2
 *   Triangle 2 → v0 v2 v3
 *
 * Polygons with more than 4 vertices are also fan-triangulated from v0.
 *
 * OBJ indices are 1-based. This reader converts them to 0-based internally.
 * Negative indices (relative references) are resolved correctly.
 */
public class ObjReader {

    // ── internal data containers ──────────────────────────────────────────────

    /** One index triple from a face token: vertex / texcoord / normal (all optional). */
    private static class FaceIndex {
        int v  = -1;   // 0-based vertex index   (-1 = not present)
        int vt = -1;   // 0-based texcoord index  (-1 = not present)
        int vn = -1;   // 0-based normal index    (-1 = not present)
    }

    // ── public parse entry point ──────────────────────────────────────────────

    /**
     * Reads an OBJ file and returns an {@link ObjModel} containing all
     * parsed vertices, normals, texture coordinates, and triangulated faces.
     *
     * @param filePath path to the .obj file
     * @return parsed model ready to add to the scene
     * @throws IOException if the file cannot be read
     */
    public static ObjModel parse(String filePath) throws IOException {

        // Raw data lists (1-indexed in the file → stored 0-indexed here)
        List<Vector3D> vertices  = new ArrayList<>();
        List<Vector3D> normals   = new ArrayList<>();
        List<double[]> texCoords = new ArrayList<>();

        // Output triangles: each entry is an int[3] of vertex indices (0-based)
        List<int[]>    triV      = new ArrayList<>();   // vertex indices
        List<int[]>    triVN     = new ArrayList<>();   // normal indices (-1 if absent)
        List<int[]>    triVT     = new ArrayList<>();   // texcoord indices (-1 if absent)

        // Metadata
        String currentObject = "";
        String currentGroup  = "";
        boolean smoothing    = false;
        int lineNumber       = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // ── strip inline comments and trim whitespace ─────────────────
                int hashPos = line.indexOf('#');
                if (hashPos >= 0) line = line.substring(0, hashPos);
                line = line.trim();

                // Skip blank lines
                if (line.isEmpty()) continue;

                // ── split on any whitespace ───────────────────────────────────
                String[] tokens = line.split("\\s+");
                if (tokens.length == 0) continue;

                String keyword = tokens[0];

                switch (keyword) {

                    // ── geometric vertex ─────────────────────────────────────
                    case "v": {
                        if (tokens.length < 4) {
                            warn(lineNumber, "v needs at least x y z → skipping");
                            break;
                        }
                        double x = parseDouble(tokens[1]);
                        double y = parseDouble(tokens[2]);
                        double z = parseDouble(tokens[3]);
                        vertices.add(new Vector3D(x, y, z));
                        break;
                    }

                    // ── vertex normal ─────────────────────────────────────────
                    case "vn": {
                        if (tokens.length < 4) {
                            warn(lineNumber, "vn needs at least x y z → skipping");
                            break;
                        }
                        double x = parseDouble(tokens[1]);
                        double y = parseDouble(tokens[2]);
                        double z = parseDouble(tokens[3]);
                        normals.add(new Vector3D(x, y, z).normalize());
                        break;
                    }

                    // ── texture coordinate ────────────────────────────────────
                    case "vt": {
                        if (tokens.length < 3) {
                            warn(lineNumber, "vt needs at least u v → skipping");
                            break;
                        }
                        double u = parseDouble(tokens[1]);
                        double v = parseDouble(tokens[2]);
                        texCoords.add(new double[]{u, v});
                        break;
                    }

                    // ── object name ───────────────────────────────────────────
                    case "o": {
                        currentObject = tokens.length > 1 ? tokens[1] : "";
                        System.out.println("[ObjReader] Object: " + currentObject);
                        break;
                    }

                    // ── group name ────────────────────────────────────────────
                    case "g": {
                        currentGroup = tokens.length > 1 ? tokens[1] : "";
                        System.out.println("[ObjReader] Group: " + currentGroup);
                        break;
                    }

                    // ── smooth shading group ──────────────────────────────────
                    case "s": {
                        // "s off" or "s 0" disables smoothing
                        smoothing = tokens.length > 1
                                    && !tokens[1].equalsIgnoreCase("off")
                                    && !tokens[1].equals("0");
                        break;
                    }

                    // ── face ──────────────────────────────────────────────────
                    case "f": {
                        // tokens[0] = "f", tokens[1..n] = face indices
                        int faceVertCount = tokens.length - 1;
                        if (faceVertCount < 3) {
                            warn(lineNumber, "face has fewer than 3 vertices → skipping");
                            break;
                        }

                        // Parse each face vertex token into a FaceIndex
                        FaceIndex[] fi = new FaceIndex[faceVertCount];
                        for (int i = 0; i < faceVertCount; i++) {
                            fi[i] = parseFaceIndex(tokens[i + 1],
                                                   vertices.size(),
                                                   texCoords.size(),
                                                   normals.size(),
                                                   lineNumber);
                        }

                        // Fan triangulation (slide 6: "Triangle 1 – v0 v1 v2, Triangle 2 – v0 v2 v3")
                        // Works for any polygon: fan from fi[0]
                        for (int i = 1; i < faceVertCount - 1; i++) {
                            FaceIndex a = fi[0];
                            FaceIndex b = fi[i];
                            FaceIndex c = fi[i + 1];

                            triV.add( new int[]{ a.v,  b.v,  c.v  });
                            triVN.add(new int[]{ a.vn, b.vn, c.vn });
                            triVT.add(new int[]{ a.vt, b.vt, c.vt });
                        }
                        break;
                    }

                    // ── mtllib / usemtl – acknowledged but not implemented ─────
                    case "mtllib":
                    case "usemtl":
                        // Material support is out of scope for v0.3
                        break;

                    // ── unknown keyword ───────────────────────────────────────
                    default:
                        // Silently ignore unrecognised keywords (e.g. "l", "cstype")
                        break;
                }
            }
        }

        System.out.printf("[ObjReader] Parsed: %d vertices, %d normals, "
                          + "%d texcoords, %d triangles%n",
                          vertices.size(), normals.size(),
                          texCoords.size(), triV.size());

        return new ObjModel(vertices, normals, texCoords, triV, triVN, triVT);
    }

    // ── face-index token parser ───────────────────────────────────────────────

    /**
     * Parses a single face-vertex token.
     *
     * Supported formats:
     *   "v"          → vertex only
     *   "v/vt"       → vertex + texture
     *   "v//vn"      → vertex + normal
     *   "v/vt/vn"    → vertex + texture + normal
     *
     * OBJ indices are 1-based and can be negative (relative).
     * This method converts them to 0-based absolute indices.
     */
    private static FaceIndex parseFaceIndex(String token,
                                             int vCount, int vtCount, int vnCount,
                                             int lineNumber) {
        FaceIndex fi = new FaceIndex();
        String[] parts = token.split("/", -1);  // -1 keeps empty strings between //

        // vertex index (always present)
        if (parts.length >= 1 && !parts[0].isEmpty()) {
            fi.v = resolveIndex(parseInt(parts[0]), vCount);
        }

        // texture index (may be absent → empty string between slashes)
        if (parts.length >= 2 && !parts[1].isEmpty()) {
            fi.vt = resolveIndex(parseInt(parts[1]), vtCount);
        }

        // normal index
        if (parts.length >= 3 && !parts[2].isEmpty()) {
            fi.vn = resolveIndex(parseInt(parts[2]), vnCount);
        }

        return fi;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Converts a 1-based OBJ index to a 0-based Java index.
     * Negative OBJ indices count from the end of the current list.
     *
     * @param objIndex   raw index from the OBJ token (1-based, possibly negative)
     * @param listSize   current length of the target list
     * @return           0-based absolute index
     */
    private static int resolveIndex(int objIndex, int listSize) {
        if (objIndex > 0) return objIndex - 1;          // positive: 1-based → 0-based
        if (objIndex < 0) return listSize + objIndex;   // negative: relative from end
        return 0;                                        // 0 is invalid in OBJ, treat as first
    }

    private static double parseDouble(String s) {
        return Double.parseDouble(s.trim());
    }

    private static int parseInt(String s) {
        return Integer.parseInt(s.trim());
    }

    private static void warn(int line, String msg) {
        System.err.println("[ObjReader] WARNING line " + line + ": " + msg);
    }
}
