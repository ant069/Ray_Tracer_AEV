import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

/**
 * SCENE 3: DENJI'S SHACK — BEFORE EVERYTHING CHANGED (Chainsaw Man)
 *
 * Story: Before contracts, before power, before the Public Safety Devil Hunters —
 * just a broke kid named Denji and his devil-dog Pochita, living in a rundown shack
 * on the outskirts of rural Japan. A bare bulb flickers overhead. Moonlight seeps
 * through the cracked window. This is where the story of Chainsaw Man begins.
 */
public class App_Scene3 {

    public static void main(String[] args) throws Exception {

        boolean finalRender = true;     // true → 4096×2160
        int width  = finalRender ? 4096 : 1024;
        int height = finalRender ? 2160 :  576;

        System.out.println("+---------------------------------------------------------------+");
        System.out.println("|     SCENE 3: DENJI'S SHACK — BEFORE EVERYTHING CHANGED       |");
        System.out.printf( "|                    Resolution: %dx%d%n", width, height);
        System.out.println("+---------------------------------------------------------------+");

        // Low-angle intimate frame — ground-level, close to Denji and Pochita.
        // Inspired by Fujimoto's panel compositions: characters fill the frame,
        // the oppressive darkness of the shack surrounds them.
        Camera camera = new Camera(
            new Vector3D(-0.15, 0.92, 2.55),
            new Vector3D(0.12, 0.55, 0.0),
            new Vector3D(0, 1, 0),
            55.0,
            width, height
        );

        // Background: deep night sky — indigo from the Chainsaw Man manga
        Scene scene = new Scene(new Color(5, 8, 20));

        System.out.println("[Info] Generating procedural wood textures...");
        Texture woodDiff = ProceduralTexture.wood(512, 512, 72, 52, 32);
        Texture woodNorm = ProceduralTexture.woodNormal(512, 512);

        // -----------------------------------------------------------------------
        // LIGHTING — Chainsaw Man childhood arc palette
        //
        //   The bare bulb is the emotional centre: warm, focused, harsh.
        //   A SpotLight (not a PointLight) restricts it to a cone that only
        //   touches Denji and Pochita — the rest of the shack lives in moonlight.
        //
        //   The moon is dominant and cold: deep indigo-blue.
        //   Darkness is part of the character.
        // -----------------------------------------------------------------------

        // -----------------------------------------------------------------------
        // LIGHTING — Reference palette: Chainsaw Man S1 EP1 childhood flashback.
        //   Cold blue-indigo dominates EVERYTHING.
        //   The bare bulb is a barely-perceptible warm accent (dim, low cone).
        //   Darkness is not a bug — it IS the scene.
        // -----------------------------------------------------------------------

        // BARE OVERHEAD BULB — SpotLight at very low intensity.
        // In the anime reference, there is almost NO warm light visible.
        // This is just a subtle amber hint so the characters aren't purely blue.
        // BARE OVERHEAD BULB — warm amber SpotLight, brighter so Denji and
        // Pochita are clearly lit for the jury presentation.
        scene.addLight(new SpotLight(
            new Vector3D(0.0, 1.95, 0.10),
            new Vector3D(0.0, -1.0, 0.04),
            25.0, 45.0,
            new Color(255, 205, 130),    // warm amber
            4.0
        ));

        // MOON — dominant cold light, raised so the window AreaLight penumbra
        // is clearly visible on the walls and floor for the presentation.
        scene.addLight(new DirectionalLight(
            new Vector3D(1.0, -0.22, 0.18),
            new Color(130, 165, 240),
            0.80
        ));

        // AREA LIGHT: raised so the soft shadow penumbra from the window bars
        // is unmistakably visible on Denji's body — key challenge mode feature.
        scene.addLight(new AreaLight(
            new Vector3D(-2.44, 0.80, -1.35),
            new Vector3D(0.0, 0.0, 1.40),
            new Vector3D(0.0, 1.00, 0.0),
            new Color(130, 165, 240),
            0.70,
            7
        ));

        // Moonlight accent on puddle — stronger so the IOR=1.33 refraction
        // and IOR=0.55 reflection are clearly visible to the jury.
        scene.addLight(new PointLight(
            new Vector3D(-1.6, 1.9, 0.7),
            new Color(130, 168, 245),
            4.0
        ));

        // Deep indigo ambient fill — the manga darkness that fills every corner.
        scene.addLight(new DirectionalLight(
            new Vector3D(0.1, -1.0, 0.2),
            new Color(20, 28, 62),
            0.12
        ));

        String TEX = "../textures/";

        // --- MATERIALS ---
        // Dark, worn, weather-beaten surfaces — the shack has never been painted.
        // Lights define the colour; base colours are near-black so they don't fight the mood.

        Material woodFloor  = new Material(new Color(28, 25, 20), 0.06, 0.0, 1.0, 16)
                                  .withTexture(woodDiff)
                                  .withNormalMap(woodNorm);
        Material woodWall   = new Material(new Color(30, 27, 22), 0.0, 0.0, 1.0, 4)
                                  .withTexture(woodDiff)
                                  .withNormalMap(woodNorm);
        Material woodCeil   = new Material(new Color(16, 14, 10), 0.0, 0.0, 1.0, 4)
                                  .withTexture(woodDiff);
        // Grimy glass: dark blue, IOR=1.52 — the moon distorts as it passes through
        Material glassMat   = new Material(new Color(55, 75, 110), 0.08, 0.52, 1.52, 32)
                                  .withTexture(Texture.load(TEX + "mirror_BaseColor.1001.png"));
        // Window frame: very dark wood, deep indigo
        Material windowFrame= new Material(new Color(18, 15, 22), 0.0, 0.0, 1.0, 4);
        // Old mattress: worn fabric, dark blue-grey (as in the anime)
        Material mattress   = new Material(new Color(35, 38, 52), 0.0, 0.0, 1.0, 4);
        // Wooden crate: dark wood with cold tint
        Material woodCrate  = new Material(new Color(28, 25, 35), 0.0, 0.0, 1.0, 4);

        final double fy = 0.0;    // floor level
        final double cy = 2.55;   // ceiling level

        // Floor — wood planks tiled at 0.6 m
        quadUV(scene,
            new Vector3D(-2.5, fy, -3.0), new Vector3D( 2.5, fy, -3.0),
            new Vector3D( 2.5, fy,  4.5), new Vector3D(-2.5, fy,  4.5), woodFloor, 8.33, 12.5);

        // Ceiling — same wood tiling
        quadUV(scene,
            new Vector3D(-2.5, cy, -3.0), new Vector3D( 2.5, cy, -3.0),
            new Vector3D( 2.5, cy,  4.5), new Vector3D(-2.5, cy,  4.5), woodCeil, 8.33, 12.5);

        // Back wall
        quadUV(scene,
            new Vector3D(-2.5, fy, -3.0), new Vector3D( 2.5, fy, -3.0),
            new Vector3D( 2.5, cy, -3.0), new Vector3D(-2.5, cy, -3.0), woodWall, 8.33, 4.25);

        // Right wall
        quadUV(scene,
            new Vector3D(2.5, fy, -3.0), new Vector3D(2.5, fy, 4.5),
            new Vector3D(2.5, cy,  4.5), new Vector3D(2.5, cy, -3.0), woodWall, 12.5, 4.25);

        // Left wall (with window cutout) — tiled to match the rest
        // Section below window
        quadUV(scene,
            new Vector3D(-2.5, fy,   -3.0), new Vector3D(-2.5, fy,   4.5),
            new Vector3D(-2.5, 0.75,  4.5), new Vector3D(-2.5, 0.75, -3.0), woodWall, 12.5, 1.25);
        // Section above window
        quadUV(scene,
            new Vector3D(-2.5, 1.85, -3.0), new Vector3D(-2.5, 1.85,  4.5),
            new Vector3D(-2.5, cy,    4.5), new Vector3D(-2.5, cy,   -3.0), woodWall, 12.5, 1.17);
        // Section left of window
        quadUV(scene,
            new Vector3D(-2.5, 0.75, -3.0), new Vector3D(-2.5, 0.75, -1.4),
            new Vector3D(-2.5, 1.85, -1.4), new Vector3D(-2.5, 1.85, -3.0), woodWall, 2.67, 1.83);
        // Section right of window
        quadUV(scene,
            new Vector3D(-2.5, 0.75,  0.1), new Vector3D(-2.5, 0.75, 4.5),
            new Vector3D(-2.5, 1.85,  4.5), new Vector3D(-2.5, 1.85, 0.1), woodWall, 7.33, 1.83);

        // Window frame
        quad(scene,
            new Vector3D(-2.48, 0.72, -1.43), new Vector3D(-2.48, 0.72,  0.13),
            new Vector3D(-2.48, 1.88,  0.13), new Vector3D(-2.48, 1.88, -1.43), windowFrame);
        // Refractive glass — bends moonlight (IOR=1.52 → Snell's law)
        quad(scene,
            new Vector3D(-2.47, 0.78, -1.38), new Vector3D(-2.47, 0.78,  0.08),
            new Vector3D(-2.47, 1.82,  0.08), new Vector3D(-2.47, 1.82, -1.38), glassMat);

        // Old mattress (Denji's bed)
        quad(scene,
            new Vector3D(-2.2, fy, -2.8), new Vector3D(-0.4, fy, -2.8),
            new Vector3D(-0.4, fy, -1.2), new Vector3D(-2.2, fy, -1.2), mattress);
        // Mattress thickness
        quad(scene,
            new Vector3D(-2.2, 0.0, -2.8), new Vector3D(-0.4, 0.0, -2.8),
            new Vector3D(-0.4, 0.18,-2.8), new Vector3D(-2.2, 0.18,-2.8), mattress);
        quad(scene,
            new Vector3D(-2.2, 0.0, -1.2), new Vector3D(-0.4, 0.0, -1.2),
            new Vector3D(-0.4, 0.18,-1.2), new Vector3D(-2.2, 0.18,-1.2), mattress);

        // Wooden crate (improvised table)
        quad(scene,
            new Vector3D(1.0, 0.0, -0.8), new Vector3D(2.0, 0.0, -0.8),
            new Vector3D(2.0, 0.0,  0.3), new Vector3D(1.0, 0.0,  0.3), woodCrate);
        quad(scene,
            new Vector3D(1.0, 0.42, -0.8), new Vector3D(2.0, 0.42, -0.8),
            new Vector3D(2.0, 0.42,  0.3), new Vector3D(1.0, 0.42,  0.3), woodCrate);
        quad(scene,
            new Vector3D(1.0, 0.0, -0.8), new Vector3D(1.0, 0.42, -0.8),
            new Vector3D(1.0, 0.42,  0.3), new Vector3D(1.0, 0.0,  0.3), woodCrate);
        quad(scene,
            new Vector3D(2.0, 0.0, -0.8), new Vector3D(2.0, 0.42, -0.8),
            new Vector3D(2.0, 0.42,  0.3), new Vector3D(2.0, 0.0,  0.3), woodCrate);

        // DENJI (seated)
        ObjModel denji = ObjReader.parse("../models/Denji.obj");
        denji.printSummary();

        // Seated Denji: BB height ~0.9 m (floor to head in cross-legged pose)
        Vector3D[] bb      = denji.boundingBox();
        double     charH   = bb[1].y - bb[0].y;
        double     scale   = 0.9 / charH;
        Vector3D   bbCtr   = bb[0].add(bb[1]).multiply(0.5);
        Vector3D   offset  = new Vector3D(
            -bbCtr.x * scale,
            -(bb[0].y * scale),   // base on the floor
            -bbCtr.z * scale
        );

        System.out.printf("[Info] Denji scale: %.5f  BB height: %.2f m%n", scale, charH * scale);

        Map<String, Material> dejMats = new LinkedHashMap<>();
        // Organic Denji materials: shininess=6 → skin/cloth appearance, not hard plastic.
        dejMats.put("Denji_face.001",    new Material(new Color(220, 175, 145), 0.0, 0.0, 1.0, 6)
                                                 .withSpecular(0.01)
                                                 .withTexture(Texture.load(TEX + "Denji face texture.png")));
        dejMats.put("Denji_Body.001",    new Material(new Color(215, 170, 140), 0.0, 0.0, 1.0, 6)
                                                 .withSpecular(0.01)
                                                 .withTexture(Texture.load(TEX + "Denji_Body.png")));
        dejMats.put("Denji_Hair.001",    new Material(new Color(180, 115, 55),  0.0, 0.0, 1.0, 8)
                                                 .withSpecular(0.02)
                                                 .withTexture(Texture.load(TEX + "Denji_Hair.png")));
        dejMats.put("Denji_Clothes.001", new Material(new Color( 60,  55,  65), 0.0, 0.0, 1.0, 4)
                                                 .withSpecular(0.02)
                                                 .withTexture(Texture.load(TEX + "Denji_Clothes.png")));
        dejMats.put("Denji_shoes.001",   new Material(new Color( 55,  40,  35), 0.0, 0.0, 1.0, 4)
                                                 .withSpecular(0.02)
                                                 .withTexture(Texture.load(TEX + "denji_shoes.png")));
        dejMats.put("Outline.001",       new Material(new Color( 15,  12,  10), 0.0, 0.0, 1.0, 1)
                                                 .withSpecular(0.0));

        Material defaultMat = Material.diffuse(new Color(180, 150, 120)).withSpecular(0.01);
        denji.addToScene(scene, dejMats, defaultMat, offset, scale);

        // POCHITA (separate model)
        ObjModel pochita = ObjReader.parse("../models/pochita.obj");
        pochita.printSummary();

        // Pochita is ~0.4 m tall
        Vector3D[] pbb    = pochita.boundingBox();
        double     pCharH = pbb[1].y - pbb[0].y;
        double     pScale = 0.4 / pCharH;
        Vector3D   pBbCtr = pbb[0].add(pbb[1]).multiply(0.5);
        Vector3D   pOffset = new Vector3D(
            -pBbCtr.x * pScale + 0.55,   // to the right of Denji
            -(pbb[0].y * pScale),          // base on the floor
            -pBbCtr.z * pScale + 0.25     // slightly in front
        );

        System.out.printf("[Info] Pochita scale: %.5f  BB height: %.2f m%n", pScale, pCharH * pScale);

        Map<String, Material> pochMats = new LinkedHashMap<>();
        pochMats.put("Pochita_Texture", Material.diffuse(new Color(200, 100, 35))
                                                .withTexture(Texture.load(TEX + "Pochita_Texture.png")));
        pochMats.put("Outline",         Material.diffuse(new Color( 15,  12,  10)));

        Material pochDefaultMat = Material.diffuse(new Color(200, 100, 35));
        pochita.addToScene(scene, pochMats, pochDefaultMat, pOffset, pScale);

        // BUCKET (left corner below the window — the shack leaks)
        // Canonical detail: Denji lived in a ruined shack that flooded
        ObjModel bucket = ObjReader.parse("../models/bucket.obj");
        bucket.printSummary();

        Vector3D[] bkBB   = bucket.boundingBox();
        double bkH        = bkBB[1].y - bkBB[0].y;
        double bkScale    = 0.27 / bkH;
        Vector3D bkBbCtr  = bkBB[0].add(bkBB[1]).multiply(0.5);
        Vector3D bkOffset = new Vector3D(
            -bkBbCtr.x * bkScale + (-1.75),
            -(bkBB[0].y * bkScale),
            -bkBbCtr.z * bkScale + (-0.5)
        );

        Map<String, Material> bkMats = new LinkedHashMap<>();
        // Bucket: rusted metal — shininess=12 for a broad diffuse metallic highlight.
        bkMats.put("M_Bucket", new Material(new Color(110, 95, 75), 0.42, 0.0, 1.0, 12)
                                    .withTexture(Texture.load(TEX + "M_Bucket_BaseColor.png")));
        Material bkDefault = new Material(new Color(110, 95, 75), 0.42, 0.0, 1.0, 12);
        bucket.addToScene(scene, bkMats, bkDefault, bkOffset, bkScale);

        // LOAF OF BREAD (on the floor between Denji and Pochita)
        // The most intimate manga moment: sharing their only food on the shack floor.
        ObjModel loaf = ObjReader.parse("../models/BreadLoad-exported.obj");
        loaf.printSummary();

        Vector3D[] lfBB   = loaf.boundingBox();
        double lfH        = lfBB[1].y - lfBB[0].y;
        double lfScale    = 0.22 / lfH;               // ~22 cm long — realistic bread
        Vector3D lfBbCtr  = lfBB[0].add(lfBB[1]).multiply(0.5);
        Vector3D lfOffset = new Vector3D(
            -lfBbCtr.x * lfScale + 0.35,
            -(lfBB[0].y * lfScale),
            -lfBbCtr.z * lfScale + 1.05
        );

        Map<String, Material> lfMats = new LinkedHashMap<>();
        lfMats.put("Material_0", Material.diffuse(new Color(185, 138, 75))
                                         .withTexture(Texture.load(TEX + "BreadLoad-exported01.jpg")));
        lfMats.put("Material_1", Material.diffuse(new Color(165, 118, 60))
                                         .withTexture(Texture.load(TEX + "BreadLoad-exported02.jpg")));
        Material lfDefault = Material.diffuse(new Color(175, 128, 68));
        loaf.addToScene(scene, lfMats, lfDefault, lfOffset, lfScale);

        // BREAD SLICE (on the crate — Denji's only food)
        ObjModel bread = ObjReader.parse("../models/Slice of bread.obj");
        bread.printSummary();

        Vector3D[] brBB   = bread.boundingBox();
        double brW        = Math.max(brBB[1].x - brBB[0].x, brBB[1].z - brBB[0].z);
        double brScale    = 0.14 / brW;
        Vector3D brBbCtr  = brBB[0].add(brBB[1]).multiply(0.5);
        Vector3D brOffset = new Vector3D(
            -brBbCtr.x * brScale + 1.45,
            -(brBB[0].y * brScale) + 0.42,   // on top of the crate
            -brBbCtr.z * brScale + (-0.22)
        );

        Map<String, Material> brMats = new LinkedHashMap<>();
        brMats.put("Material.002", Material.diffuse(new Color(210, 165, 100))
                                           .withTexture(Texture.load(TEX + "E9506484-EE02-4D04-BA57-CB5ACA723925.jpeg")));
        Material brDefault = Material.diffuse(new Color(210, 165, 100));
        bread.addToScene(scene, brMats, brDefault, brOffset, brScale);

        // WATER PUDDLE — flat procedural disc at floor level
        // Y=0.001 (1 mm): prevents z-fighting with the floor.
        // Refracted ray (Snell IOR=1.33) passes through the disc and hits the floor.
        // Shadow rays skip the disc (isOpaque=false) → floor is correctly illuminated.
        Material puddleMat = new Material(new Color(18, 42, 85), 0.55, 0.45, 1.33, 12);
        {
            double cx   = -1.4, wy = 0.001, cz = 0.8;
            double rx   = 0.36, rz = 0.28;
            int    NS   = 40;
            Vector3D UP  = new Vector3D(0, 1, 0);
            Vector3D ctr = new Vector3D(cx, wy, cz);
            for (int i = 0; i < NS; i++) {
                double a0 = 2 * Math.PI * i       / NS;
                double a1 = 2 * Math.PI * (i + 1) / NS;
                Vector3D p0 = new Vector3D(cx + rx * Math.cos(a0), wy, cz + rz * Math.sin(a0));
                Vector3D p1 = new Vector3D(cx + rx * Math.cos(a1), wy, cz + rz * Math.sin(a1));
                scene.addObject(new Triangle(ctr, p1, p0, UP, UP, UP, puddleMat));
            }
        }

        // WOODEN WINDOW FRAME (sm_churchWindowFrame.obj)
        // Rotated -90° around Y to stand perpendicular to the left wall (x=-2.5).
        // Wooden bars cast grid-shaped soft-shadow penumbra from the AreaLight.
        ObjModel winFrame = ObjReader.parse("../models/sm_churchWindowFrame.obj");
        winFrame.printSummary();

        Vector3D[] wfBB    = winFrame.boundingBox();
        double wfScale     = 1.46 / (wfBB[1].x - wfBB[0].x);
        Vector3D wfOffset  = new Vector3D(
            -2.45,
            1.30,
            -0.65
        );

        Material wfMat = new Material(new Color(38, 24, 10), 0.0, 0.0, 1.0, 4)
                             .withTexture(Texture.load(TEX + "sm_churchWindowFrame_churchWindowFrame_Bas.png"))
                             .withNormalMap(Texture.load(TEX + "sm_churchWindowFrame_churchWindowFrame_Nor.png"));
        winFrame.addToScene(scene, Map.of("unwrapChekcerShader", wfMat), wfMat,
                            wfOffset, wfScale, -Math.PI / 2.0);

        // BROKEN MIRROR ON THE FLOOR (mirror.obj)
        // A shard Denji found in the trash — reflects the ceiling and bulb.
        // Reflectivity bumped to 0.90 for a cleaner, more dramatic reflection.
        ObjModel mirror = ObjReader.parse("../models/mirror.obj");
        mirror.printSummary();

        Vector3D[] mrBB   = mirror.boundingBox();
        double mrH        = mrBB[1].y - mrBB[0].y;
        double mrScale    = 0.30 / mrH;
        Vector3D mrBbCtr  = mrBB[0].add(mrBB[1]).multiply(0.5);
        Vector3D mrOffset = new Vector3D(
            -mrBbCtr.x * mrScale + (-1.1),
            -(mrBB[0].y * mrScale),
            -mrBbCtr.z * mrScale + (-1.5)
        );

        // Broken mirror: higher reflectivity (0.90) — captures the moon overhead vividly
        Material mirrorMat = new Material(new Color(80, 105, 145), 0.90, 0.0, 1.0, 256)
                                 .withTexture(Texture.load(TEX + "mirror_BaseColor.1001.png"));
        mirror.addToScene(scene, Map.of("None", mirrorMat), mirrorMat, mrOffset, mrScale);

        int       bounces = finalRender ? 5 : 3;
        Raytracer rt      = new Raytracer(scene, camera, bounces);

        System.out.println("[Info] Starting render...");
        long         t0  = System.currentTimeMillis();
        BufferedImage img = rt.render();
        double       secs = (System.currentTimeMillis() - t0) / 1000.0;

        String fname = finalRender
            ? "Scene3_Denji_Shack_FINAL_4096x2160.png"
            : "Scene3_Denji_Shack_1024x576.png";
        File outDir = new File("output");
        if (!outDir.exists()) outDir.mkdirs();
        File outFile = new File(outDir, fname);
        ImageIO.write(img, "PNG", outFile);

        System.out.println("\n+---------------------------------------------------------------+");
        System.out.printf("| Completed in %.1f s → %s%n", secs, outFile.getName());
        System.out.println("+---------------------------------------------------------------+");
    }

    private static void quad(Scene scene,
                              Vector3D a, Vector3D b,
                              Vector3D c, Vector3D d, Material mat) {
        scene.addObject(new Triangle(a, b, c, mat));
        scene.addObject(new Triangle(a, c, d, mat));
    }

    /** UV-mapped quad — su × sv controls texture tiling frequency. */
    private static void quadUV(Scene scene,
                                Vector3D a, Vector3D b, Vector3D c, Vector3D d,
                                Material mat, double su, double sv) {
        double[] uva = {0,  0 };
        double[] uvb = {su, 0 };
        double[] uvc = {su, sv};
        double[] uvd = {0,  sv};
        scene.addObject(new Triangle(a, b, c, null, null, null, uva, uvb, uvc, mat, false));
        scene.addObject(new Triangle(a, c, d, null, null, null, uva, uvc, uvd, mat, false));
    }
}
