import java.io.*;

/**
 * OBJ model simplifier — reduces triangle count while preserving shape,
 * using simple face decimation (keeps every N-th face).
 */
public class ObjSimplifier {
    public static void simplify(String inputPath, String outputPath, double reductionRatio) throws IOException {
        System.out.println("[ObjSimplifier] Loading " + inputPath + "...");

        BufferedReader br = new BufferedReader(new FileReader(inputPath));
        String line;
        int totalFaces = 0;
        while ((line = br.readLine()) != null) {
            if (line.trim().startsWith("f ")) {
                totalFaces++;
            }
        }
        br.close();

        int step = Math.max(1, (int) Math.round(1.0 / reductionRatio));
        System.out.printf("[ObjSimplifier] Total original faces: %d\n", totalFaces);
        System.out.printf("[ObjSimplifier] Keeping every %d-th face (~%d faces output)\n", step, totalFaces / step);

        br = new BufferedReader(new FileReader(inputPath));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath));

        int faceIndex = 0;
        while ((line = br.readLine()) != null) {
            if (line.trim().startsWith("f ")) {
                if (faceIndex % step == 0) {
                    bw.write(line);
                    bw.newLine();
                }
                faceIndex++;
            } else {
                bw.write(line);
                bw.newLine();
            }
        }

        br.close();
        bw.close();

        System.out.printf("[ObjSimplifier] Simplified file saved to %s\n", outputPath);
        System.out.printf("[ObjSimplifier] Faces written: %d\n", faceIndex / step);
    }

    public static void main(String[] args) throws IOException {
        simplify("../models/koenigsegg.obj", "../models/koenigsegg_simplified.obj", 0.10);
    }
}
