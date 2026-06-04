import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

/**
 * SCENE 2: BATMOBILE — BATCAVE BAY
 *
 * Story: After foiling Two-Face's heist in downtown Gotham, Batman descends
 * into the Batcave. The tactical lighting system activates as the Batmobile
 * glides into its bay. Cave water drips from stalactites and pools on the
 * wet stone floor, reflecting the machine's black armor back at the darkness.
 */
public class App_Scene2 {

    public static void main(String[] args) throws Exception {

        boolean finalRender = true;     // true → 4096×2160
        int width  = finalRender ? 4096 : 1024;
        int height = finalRender ? 2160 :  576;

        System.out.println("+---------------------------------------------------------------+");
        System.out.println("|         SCENE 2: BATMOBILE - BATCAVE BAY                     |");
        System.out.printf( "|                    Resolution: %dx%d%n", width, height);
        System.out.println("+---------------------------------------------------------------+");

        // Low-profile lateral-front view — eye level near the car's roofline,
        // so the Batmobile reads as imposing and massive in the cave.
        Camera camera = new Camera(
            new Vector3D(-1.2, 1.9, 6.5),
            new Vector3D( 0.0, 0.8, 0.0),
            new Vector3D(0, 1, 0),
            60.0,
            width, height
        );

        Scene scene = new Scene(new Color(4, 6, 10));   // cave black background

        // Main overhead spot: above the center of the car
        scene.addLight(new SpotLight(
            new Vector3D(-1.0, 5.0, 0.0),
            new Vector3D(-1.0, 0.6, 0.0),
            new Color(255, 248, 230),
            500.0, 16.0, 35.0
        ));

        // Side spot: illuminates the left/front side (the face visible to the camera)
        scene.addLight(new SpotLight(
            new Vector3D(-5.0, 3.5, 1.0),
            new Vector3D(-1.0, 0.6, 0.0),
            new Color(210, 235, 255),
            400.0, 14.0, 30.0
        ));

        // Rear spot: amber accent on the opposite side
        scene.addLight(new SpotLight(
            new Vector3D( 2.5, 4.0, -1.5),
            new Vector3D( 0.5, 0.6, 0.0),
            new Color(255, 210, 130),
            300.0, 14.0, 28.0
        ));

        // Front fill (from camera angle): softens hard shadows
        scene.addLight(new SpotLight(
            new Vector3D(-4.5, 2.0, 4.0),
            new Vector3D(-1.0, 0.6, 0.0),
            new Color(170, 210, 255),
            200.0, 20.0, 40.0
        ));

        // Cave ambient: faint stalactite blue
        scene.addLight(new DirectionalLight(
            new Vector3D(0.5, -1, 0.3),
            new Color(50, 70, 100),
            0.08
        ));

        // Floor — reflective wet rock (cave puddle effect)
        Material floorMat = Material.metallic(new Color(22, 20, 18), 0.65, 80);
        quad(scene,
            new Vector3D(-15, 0, -15), new Vector3D( 15, 0, -15),
            new Vector3D( 15, 0,  15), new Vector3D(-15, 0,  15), floorMat);

        // Water puddle in front-left of the car — refractive surface (water IOR = 1.33)
        Material waterMat = new Material(new Color(14, 24, 42), 0.05, 0.80, 1.33, 32);
        quad(scene,
            new Vector3D(-2.5, 0.005,  2.0), new Vector3D( 2.5, 0.005,  2.0),
            new Vector3D( 2.5, 0.005,  6.5), new Vector3D(-2.5, 0.005,  6.5), waterMat);

        // Back wall (extended for corridor depth)
        Material rockMat = Material.diffuse(new Color(36, 32, 27));
        quad(scene,
            new Vector3D(-12, 0, -14), new Vector3D(12, 0, -14),
            new Vector3D( 12, 8, -14), new Vector3D(-12, 8, -14), rockMat);

        // Left wall
        quad(scene,
            new Vector3D(-10, 0,  12), new Vector3D(-10, 0, -14),
            new Vector3D(-10, 8, -14), new Vector3D(-10, 8,  12), rockMat);

        // Right wall
        quad(scene,
            new Vector3D(10, 0, -14), new Vector3D(10, 0,  12),
            new Vector3D(10, 8,  12), new Vector3D(10, 8, -14), rockMat);

        // Cave ceiling
        Material ceilMat = Material.diffuse(new Color(26, 23, 20));
        quad(scene,
            new Vector3D(-12, 8, -14), new Vector3D(12, 8, -14),
            new Vector3D( 12, 8,  12), new Vector3D(-12, 8,  12), ceilMat);

        // COMPUTER SCREENS (Batcave command center)
        // The glowing screens are visible through the refractive windscreen
        Material scrGreen = Material.diffuse(new Color( 20, 210,  90));   // green terminal
        Material scrBlue  = Material.diffuse(new Color( 25, 140, 255));   // blue screen
        Material scrAmber = Material.diffuse(new Color(255, 140,  20));   // amber alert
        Material scrFrame = Material.metallic(new Color( 28,  28,  32), 0.2, 60);

        // Left frame
        quad(scene, new Vector3D(-4.1, 1.3, -9.5), new Vector3D(-1.3, 1.3, -9.5),
                    new Vector3D(-1.3, 3.1, -9.5), new Vector3D(-4.1, 3.1, -9.5), scrFrame);
        // Left screen (green)
        quad(scene, new Vector3D(-3.9, 1.5, -9.5), new Vector3D(-1.5, 1.5, -9.5),
                    new Vector3D(-1.5, 2.9, -9.5), new Vector3D(-3.9, 2.9, -9.5), scrGreen);

        // Center frame
        quad(scene, new Vector3D(-1.1, 1.6, -9.5), new Vector3D( 1.1, 1.6, -9.5),
                    new Vector3D( 1.1, 3.7, -9.5), new Vector3D(-1.1, 3.7, -9.5), scrFrame);
        // Center screen (blue — Gotham tactical map)
        quad(scene, new Vector3D(-0.9, 1.8, -9.5), new Vector3D( 0.9, 1.8, -9.5),
                    new Vector3D( 0.9, 3.5, -9.5), new Vector3D(-0.9, 3.5, -9.5), scrBlue);

        // Right frame
        quad(scene, new Vector3D( 1.3, 1.3, -9.5), new Vector3D( 4.1, 1.3, -9.5),
                    new Vector3D( 4.1, 3.1, -9.5), new Vector3D( 1.3, 3.1, -9.5), scrFrame);
        // Right screen (amber — alert)
        quad(scene, new Vector3D( 1.5, 1.5, -9.5), new Vector3D( 3.9, 1.5, -9.5),
                    new Vector3D( 3.9, 2.9, -9.5), new Vector3D( 1.5, 2.9, -9.5), scrAmber);

        // Point lights in front of each screen — boosted so they paint the cave
        // walls with saturated coloured light (green/blue/amber spill).
        scene.addLight(new PointLight(new Vector3D(-2.7, 2.2, -8.5), new Color( 20, 210,  90), 28));
        scene.addLight(new PointLight(new Vector3D( 0.0, 2.7, -8.5), new Color( 25, 140, 255), 42));
        scene.addLight(new PointLight(new Vector3D( 2.7, 2.2, -8.5), new Color(255, 140,  20), 28));

        // CIRCULAR PLATFORM — bat symbol on the floor.
        // The Batmobile parks on this rotating turntable.
        Material ringMat = Material.metallic(new Color(28, 38, 52), 0.55, 100);
        ring(scene, ringMat, 0.0, 0.06, -1.0, 3.0, 4.2, 24);
        // Blue glow from the rotating platform — brighter to cast a visible
        // blue halo on the floor and on the underside of the Batmobile.
        scene.addLight(new PointLight(new Vector3D( 0.0, 0.3, -1.0), new Color(30, 120, 255), 28));
        scene.addLight(new PointLight(new Vector3D( 3.5, 0.3, -1.0), new Color(20,  80, 200), 16));
        scene.addLight(new PointLight(new Vector3D(-3.5, 0.3, -1.0), new Color(20,  80, 200), 16));

        // BATMAN LOGO OBJ (platform floor)
        // Rotated 90° so it lies flat — model XY plane → world XZ plane
        ObjModel batLogo = ObjReader.parse("../models/batmanlogo.obj");
        Vector3D[] lb    = batLogo.boundingBox();
        double logoScale = 2.6 / (lb[1].x - lb[0].x);              // 2.6 m wide
        double logoCtrX  = (lb[0].x + lb[1].x) * 0.5;
        double logoCtrY  = (lb[0].y + lb[1].y) * 0.5;              // model Y centroid
        Vector3D logoOffset = new Vector3D(
            -logoCtrX * logoScale,                                   // centered on X
             0.012,                                                   // height above floor
             3.8 + logoCtrY * logoScale                              // in front of the car, visible from camera
        );
        // Bright blue — emblem visible on the Batcave floor
        Material logoBatMat = Material.diffuse(new Color(45, 140, 255));
        batLogo.addToSceneFloor(scene, logoBatMat, logoOffset, logoScale);
        // Blue light above the logo — stronger so the emblem glows on the wet floor
        scene.addLight(new PointLight(new Vector3D(0, 1.5,  3.8), new Color(40, 120, 255), 26));

        // METALLIC PILLARS (Batcave corridor)
        Material pillarMat = Material.metallic(new Color(38, 44, 52), 0.45, 80);
        // 3 pairs of pillars along the corridor
        for (double pz : new double[]{1.5, -2.5, -6.5}) {
            pillar(scene, pillarMat, -5.5, pz, 0.32, 8.0);
            pillar(scene, pillarMat,  5.5, pz, 0.32, 8.0);
            // Teal blue accent between each pair of pillars — brighter corridor atmosphere
            scene.addLight(new PointLight(new Vector3D(0, 2.0, pz), new Color(20, 70, 160), 14));
        }

        // STALACTITES — denser canopy for a more convincing cave ceiling
        Material stalMat = Material.diffuse(new Color(40, 36, 30));
        stalactite(scene, stalMat, -2.0, 8.0,  1.5, 5.2, 0.30);
        stalactite(scene, stalMat,  1.5, 8.0,  2.0, 4.8, 0.25);
        stalactite(scene, stalMat,  0.0, 8.0,  3.5, 5.5, 0.20);
        stalactite(scene, stalMat, -3.0, 8.0, -3.5, 4.2, 0.28);
        stalactite(scene, stalMat,  2.5, 8.0, -4.5, 5.0, 0.24);
        stalactite(scene, stalMat, -5.0, 8.0, -7.0, 4.5, 0.20);
        stalactite(scene, stalMat,  4.5, 8.0, -5.5, 5.2, 0.26);
        // Additional stalactites to fill the cave ceiling
        stalactite(scene, stalMat,  3.2, 8.0,  0.5, 5.8, 0.18);
        stalactite(scene, stalMat, -4.0, 8.0,  4.0, 4.6, 0.22);
        stalactite(scene, stalMat,  0.8, 8.0, -1.5, 6.2, 0.15);
        stalactite(scene, stalMat, -1.5, 8.0, -6.0, 5.0, 0.23);
        stalactite(scene, stalMat,  5.5, 8.0, -2.0, 4.8, 0.17);
        stalactite(scene, stalMat, -6.0, 8.0,  0.0, 5.5, 0.19);

        // BATMOBILE
        String objPath = "../models/Batmobile for Sketchfab.obj";
        ObjModel batmobile = ObjReader.parse(objPath);
        batmobile.printSummary();

        // Scale to real dimensions: target height 1.4 m
        Vector3D[] bb    = batmobile.boundingBox();
        double     carH  = bb[1].y - bb[0].y;
        double     scale = 1.4 / carH;
        // Center using bounding box center (more reliable than vertex centroid)
        Vector3D   bbCtr = bb[0].add(bb[1]).multiply(0.5);
        Vector3D   offset = new Vector3D(
            -bbCtr.x * scale,
            -(bb[0].y * scale),    // car base at Y = 0
            -bbCtr.z * scale
        );

        System.out.printf("[Info] Scale: %.5f  Height: %.2f m%n", scale, carH * scale);
        System.out.printf("[Info] Batmobile X:[%.2f,%.2f] Y:[%.2f,%.2f] Z:[%.2f,%.2f]%n",
            bb[0].x*scale+offset.x, bb[1].x*scale+offset.x,
            bb[0].y*scale+offset.y, bb[1].y*scale+offset.y,
            bb[0].z*scale+offset.z, bb[1].z*scale+offset.z);

        // Materials by usemtl group name
        Map<String, Material> batMats = new LinkedHashMap<>();

        Material mBlackBody  = Material.metallic(new Color( 62,  62,  75), 0.55,  22);
        Material mChrome     = Material.metallic(new Color(210, 210, 218), 0.88,  60);
        Material mWindscreen = new Material    (new Color( 28,  42,  70), 0.05, 0.78, 1.52, 64);
        Material mWheelRim   = Material.metallic(new Color(162, 162, 168), 0.50, 160);
        Material mWheelRub   = Material.diffuse (new Color( 18,  18,  20));
        Material mWheelTread = Material.diffuse (new Color( 22,  22,  24));
        Material mDiscBrake  = Material.metallic(new Color( 70,  70,  76), 0.28,  80);
        Material mNewMetal   = Material.metallic(new Color(140, 140, 148), 0.45, 130);
        Material mOldMetal   = Material.metallic(new Color( 90,  86,  80), 0.20,  40);
        Material mBlue       = Material.metallic(new Color( 22,  52,  98), 0.40, 110);
        Material mMellowBlue = Material.metallic(new Color( 42,  72, 132), 0.35,  85);
        Material mSpotGlass  = new Material    (new Color(255, 242, 200), 0.08, 0.68, 1.52, 64);
        Material mSpotLights = Material.metallic(new Color(255, 245, 190), 0.12,  40);
        Material mGrillVent  = Material.diffuse (new Color(  8,   8,  10));
        Material mWhiteBody  = Material.metallic(new Color(218, 218, 222), 0.35, 160);
        Material mWhitePaint = Material.metallic(new Color(222, 222, 226), 0.30, 130);
        Material mMat001     = Material.metallic(new Color( 78,  78,  82), 0.28,  80);
        Material mMat002     = Material.metallic(new Color( 58,  58,  62), 0.20,  60);
        Material mDefault    = Material.diffuse (new Color( 52,  52,  56));
        Material mCopper     = Material.metallic(new Color(172,  92,  32), 0.55, 280);
        Material mDial       = Material.metallic(new Color( 52,  52,  58), 0.18,  40);
        Material mDialGlass  = new Material    (new Color(185, 210, 255), 0.08, 0.65, 1.52,  64);
        Material mTexture    = Material.diffuse (new Color( 58,  52,  46));
        Material mTexture001 = Material.diffuse (new Color( 48,  42,  38));

        batMats.put("Black Body",        mBlackBody);
        batMats.put("Chrome",            mChrome);
        batMats.put("Windscreen",        mWindscreen);
        batMats.put("Wheel Rim",         mWheelRim);
        batMats.put("Wheel Rubber",      mWheelRub);
        batMats.put("Wheel Tread",       mWheelTread);
        batMats.put("Disc Brakes",       mDiscBrake);
        batMats.put("New Metal",         mNewMetal);
        batMats.put("Old Metal",         mOldMetal);
        batMats.put("Blue",              mBlue);
        batMats.put("Mellow Blue",       mMellowBlue);
        batMats.put("Spot Light Glass",  mSpotGlass);
        batMats.put("Spot Lights",       mSpotLights);
        batMats.put("Grill Vent",        mGrillVent);
        batMats.put("White Body",        mWhiteBody);
        batMats.put("White Paint",       mWhitePaint);
        batMats.put("Material.001",      mMat001);
        batMats.put("Material.002",      mMat002);
        batMats.put("Default",           mDefault);
        batMats.put("Copper Wheel Nuts", mCopper);
        batMats.put("Pressure Dial 1",   mDial);
        batMats.put("Dial Glass",        mDialGlass);
        batMats.put("Texture",           mTexture);
        batMats.put("Texture.001",       mTexture001);

        batmobile.addToScene(scene, batMats, mBlackBody, offset, scale);

        int      bounces = finalRender ? 5 : 3;
        Raytracer rt     = new Raytracer(scene, camera, bounces);

        System.out.println("[Info] Starting render...");
        long         t0  = System.currentTimeMillis();
        BufferedImage img = rt.render();
        double       secs = (System.currentTimeMillis() - t0) / 1000.0;

        String fname = finalRender
            ? "Scene2_Batmobile_Batcave_FINAL_4096x2160.png"
            : "Scene2_Batmobile_Batcave_1024x576.png";
        File outDir = new File("output");
        if (!outDir.exists()) outDir.mkdirs();
        File outFile = new File(outDir, fname);
        ImageIO.write(img, "PNG", outFile);

        System.out.println("\n+---------------------------------------------------------------+");
        System.out.printf("| Completed in %.1f s → %s%n", secs, outFile.getName());
        System.out.println("+---------------------------------------------------------------+");
    }

    /** Quad = two triangles (a-b-c and a-c-d). */
    private static void quad(Scene scene,
                              Vector3D a, Vector3D b,
                              Vector3D c, Vector3D d, Material mat) {
        scene.addObject(new Triangle(a, b, c, mat));
        scene.addObject(new Triangle(a, c, d, mat));
    }

    /**
     * Bat symbol — built with triangles in the XY plane at depth Z=cz.
     * @param cx,cy  symbol center
     * @param s      scale (at s=1 the symbol is ~5 units wide)
     */
    private static void batSymbol(Scene scene, Material mat,
                                   double cx, double cy, double cz, double s) {
        // Key vertices (local XY space, scaled and translated)
        Vector3D bt  = bv(cx, cy, cz,  0.00,  0.80, s); // body top
        Vector3D bl  = bv(cx, cy, cz, -0.50,  0.30, s); // body left
        Vector3D br  = bv(cx, cy, cz,  0.50,  0.30, s); // body right
        Vector3D bbc = bv(cx, cy, cz,  0.00, -0.70, s); // tail center
        Vector3D bbl = bv(cx, cy, cz, -0.28, -0.55, s); // tail left
        Vector3D bbr = bv(cx, cy, cz,  0.28, -0.55, s); // tail right
        Vector3D el  = bv(cx, cy, cz, -0.38,  1.12, s); // left ear
        Vector3D er  = bv(cx, cy, cz,  0.38,  1.12, s); // right ear
        // Left wing
        Vector3D lmt = bv(cx, cy, cz, -1.80,  0.72, s);
        Vector3D lt  = bv(cx, cy, cz, -2.50,  0.18, s);
        Vector3D lmb = bv(cx, cy, cz, -2.00, -0.20, s);
        Vector3D lib = bv(cx, cy, cz, -1.00, -0.45, s);
        // Right wing (mirror)
        Vector3D rmt = bv(cx, cy, cz,  1.80,  0.72, s);
        Vector3D rt  = bv(cx, cy, cz,  2.50,  0.18, s);
        Vector3D rmb = bv(cx, cy, cz,  2.00, -0.20, s);
        Vector3D rib = bv(cx, cy, cz,  1.00, -0.45, s);

        // Central body
        btri(scene, mat, bt,  bl,  br );
        btri(scene, mat, bl,  br,  bbc);
        btri(scene, mat, bl,  bbc, bbl);
        btri(scene, mat, br,  bbr, bbc);
        // Ears
        btri(scene, mat, bt,  el,  bl );
        btri(scene, mat, bt,  br,  er );
        // Left wing
        btri(scene, mat, bl,  lmt, lt );
        btri(scene, mat, bl,  lt,  lmb);
        btri(scene, mat, bl,  lmb, lib);
        btri(scene, mat, bbl, bl,  lib);
        // Right wing
        btri(scene, mat, br,  rt,  rmt);
        btri(scene, mat, br,  rmb, rt );
        btri(scene, mat, br,  rib, rmb);
        btri(scene, mat, bbr, rib, br );
    }

    private static Vector3D bv(double cx, double cy, double cz,
                                double lx, double ly, double s) {
        return new Vector3D(cx + lx * s, cy + ly * s, cz);
    }

    /** Bat symbol on the horizontal plane (floor) — lx maps to X, ly maps to Z. */
    private static void batSymbolFloor(Scene scene, Material mat,
                                        double cx, double cy, double cz, double s) {
        Vector3D bt  = bh(cx, cy, cz,  0.00,  0.80, s);
        Vector3D bl  = bh(cx, cy, cz, -0.50,  0.30, s);
        Vector3D br  = bh(cx, cy, cz,  0.50,  0.30, s);
        Vector3D bbc = bh(cx, cy, cz,  0.00, -0.70, s);
        Vector3D bbl = bh(cx, cy, cz, -0.28, -0.55, s);
        Vector3D bbr = bh(cx, cy, cz,  0.28, -0.55, s);
        Vector3D el  = bh(cx, cy, cz, -0.38,  1.12, s);
        Vector3D er  = bh(cx, cy, cz,  0.38,  1.12, s);
        Vector3D lmt = bh(cx, cy, cz, -1.80,  0.72, s);
        Vector3D lt  = bh(cx, cy, cz, -2.50,  0.18, s);
        Vector3D lmb = bh(cx, cy, cz, -2.00, -0.20, s);
        Vector3D lib = bh(cx, cy, cz, -1.00, -0.45, s);
        Vector3D rmt = bh(cx, cy, cz,  1.80,  0.72, s);
        Vector3D rt  = bh(cx, cy, cz,  2.50,  0.18, s);
        Vector3D rmb = bh(cx, cy, cz,  2.00, -0.20, s);
        Vector3D rib = bh(cx, cy, cz,  1.00, -0.45, s);

        btri(scene, mat, bt,  bl,  br ); btri(scene, mat, bl,  br,  bbc);
        btri(scene, mat, bl,  bbc, bbl); btri(scene, mat, br,  bbr, bbc);
        btri(scene, mat, bt,  el,  bl ); btri(scene, mat, bt,  br,  er );
        btri(scene, mat, bl,  lmt, lt ); btri(scene, mat, bl,  lt,  lmb);
        btri(scene, mat, bl,  lmb, lib); btri(scene, mat, bbl, bl,  lib);
        btri(scene, mat, br,  rt,  rmt); btri(scene, mat, br,  rmb, rt );
        btri(scene, mat, br,  rib, rmb); btri(scene, mat, bbr, rib, br );
    }

    private static Vector3D bh(double cx, double cy, double cz,
                                double lx, double lz, double s) {
        return new Vector3D(cx + lx * s, cy, cz + lz * s);
    }

    /** Cylindrical pillar with n faces between Y=0 and Y=h. */
    private static void pillar(Scene scene, Material mat,
                                double cx, double cz, double r, double h) {
        int n = 8;
        for (int i = 0; i < n; i++) {
            double a0 = 2 * Math.PI * i / n;
            double a1 = 2 * Math.PI * (i + 1) / n;
            Vector3D b0 = new Vector3D(cx + r*Math.cos(a0), 0, cz + r*Math.sin(a0));
            Vector3D b1 = new Vector3D(cx + r*Math.cos(a1), 0, cz + r*Math.sin(a1));
            Vector3D t0 = new Vector3D(cx + r*Math.cos(a0), h, cz + r*Math.sin(a0));
            Vector3D t1 = new Vector3D(cx + r*Math.cos(a1), h, cz + r*Math.sin(a1));
            scene.addObject(new Triangle(b0, b1, t0, mat));
            scene.addObject(new Triangle(b1, t1, t0, mat));
        }
    }

    /** Flat ring (circular platform) at Y=y, between radii innerR and outerR. */
    private static void ring(Scene scene, Material mat,
                              double cx, double y, double cz,
                              double innerR, double outerR, int n) {
        for (int i = 0; i < n; i++) {
            double a0 = 2 * Math.PI * i / n;
            double a1 = 2 * Math.PI * (i + 1) / n;
            Vector3D i0 = new Vector3D(cx + innerR*Math.cos(a0), y, cz + innerR*Math.sin(a0));
            Vector3D i1 = new Vector3D(cx + innerR*Math.cos(a1), y, cz + innerR*Math.sin(a1));
            Vector3D o0 = new Vector3D(cx + outerR*Math.cos(a0), y, cz + outerR*Math.sin(a0));
            Vector3D o1 = new Vector3D(cx + outerR*Math.cos(a1), y, cz + outerR*Math.sin(a1));
            scene.addObject(new Triangle(i0, o0, i1, mat));
            scene.addObject(new Triangle(i1, o0, o1, mat));
        }
    }

    private static void btri(Scene scene, Material mat,
                              Vector3D a, Vector3D b, Vector3D c) {
        scene.addObject(new Triangle(a, b, c, mat));
    }

    /**
     * Stalactite — triangular cone hanging from the ceiling.
     * @param baseY   Y of the ceiling (cone base)
     * @param tipY    Y of the tip (lower than baseY)
     * @param radius  base radius
     */
    private static void stalactite(Scene scene, Material mat,
                                    double cx, double baseY, double cz,
                                    double tipY, double radius) {
        Vector3D tip = new Vector3D(cx, tipY, cz);
        double angle = Math.PI / 6.0;
        Vector3D b0 = new Vector3D(cx + radius * Math.cos(angle),              baseY, cz + radius * Math.sin(angle));
        Vector3D b1 = new Vector3D(cx + radius * Math.cos(angle + 2*Math.PI/3), baseY, cz + radius * Math.sin(angle + 2*Math.PI/3));
        Vector3D b2 = new Vector3D(cx + radius * Math.cos(angle + 4*Math.PI/3), baseY, cz + radius * Math.sin(angle + 4*Math.PI/3));
        scene.addObject(new Triangle(tip, b1, b0, mat));
        scene.addObject(new Triangle(tip, b2, b1, mat));
        scene.addObject(new Triangle(tip, b0, b2, mat));
        scene.addObject(new Triangle(b0, b1, b2, mat));
    }
}
