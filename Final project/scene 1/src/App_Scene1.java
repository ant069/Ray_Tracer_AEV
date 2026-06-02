import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 * SCENE 1 - "McLAREN SENNA: MONACO TUNNEL, NIGHT QUALIFYING"
 *
 * STORY:
 * Monaco, 02:47 AM. The Papaya Orange McLaren Senna returns to the pit lane after
 * its qualifying lap. The Monegasque tunnel encloses it: to the right, silver Armco
 * barriers. To the left, polycarbonate panels with blue LED strips illuminate the
 * concrete wall — and refract through it. Above the track, a row of white LED
 * fixtures marks the dark ceiling. The wet asphalt doubles the car in an almost
 * perfect mirror. This is the exact calm before everything explodes in tenths of a second.
 *
 * FEATURES:
 *   - McLaren Senna Papaya Orange (McLaren's iconic color)
 *   - Ground-level camera (Y=0.13), F1 TV style
 *   - Row of 5 white LEDs on the tunnel ceiling
 *   - 3 blue LED strips on the left wall (Monaco blue glow)
 *   - Wet asphalt with 0.65 reflectivity
 *   - Polycarbonate panel IOR=1.49 (refraction)
 *   - Silver metallic Armco barriers
 *   - Multi-threaded parallel rendering
 */
public class App_Scene1 {

    public static void main(String[] args) throws Exception {

        boolean finalRender = true;    // true → 4096×2160
        int width  = finalRender ? 4096 : 1024;
        int height = finalRender ? 2160 :  576;

        System.out.println("+---------------------------------------------------------------+");
        System.out.println("|    SCENE 1: McLAREN SENNA - MONACO TUNNEL QUALIFYING NIGHT    |");
        System.out.printf( "|                    Resolution: %dx%d%n", width, height);
        System.out.println("+---------------------------------------------------------------+");

        // Camera at ground level, centered on the left lane,
        // wide FOV to capture both tunnel walls (matches the reference photo).
        Camera camera = new Camera(
            new Vector3D(0.0, 0.12, 5.5),      // tunnel floor, lane center
            new Vector3D(0.0, 0.38, -1.5),     // aimed at the center of the McLaren
            new Vector3D(0, 1, 0),
            62.0,                               // FOV: balance between capturing walls and avoiding lateral distortion
            width, height,
            0.1, 300.0
        );

        Scene scene = new Scene(new Color(10, 7, 5));   // warm black tunnel background

        // Dynamic light collection — allows adding/removing lights without touching the render loop.
        ArrayList<Light> tunnelLights = new ArrayList<>();

        // Very faint warm ambient — tunnel darkness is the narrative
        tunnelLights.add(new DirectionalLight(
            new Vector3D(0.0, -1.0, 0.1),
            new Color(160, 125, 75),
            0.04
        ));
        // Cold blue fill from the LED panels — simulates bounce light from the left wall.
        // This is what gives the Monaco tunnel its signature bicolor (warm top + cold left) look.
        tunnelLights.add(new DirectionalLight(
            new Vector3D(1.0, -0.05, 0.1),
            new Color(50, 100, 230),
            0.22
        ));

        // Main overhead spot — creates the warm halo over the McLaren
        tunnelLights.add(new PointLight(new Vector3D(0.0, 4.2, 0.5), new Color(255, 238, 200), 70.0));

        // Row of white LEDs: 4 fixtures in perspective along the corridor.
        // Kq = 1/I → the more distant LEDs (lower I) attenuate faster,
        // creating the visual depth characteristic of the Monaco tunnel.
        tunnelLights.add(new PointLight(new Vector3D(0.0, 3.6,  3.2), new Color(250, 228, 182), 22.0));
        tunnelLights.add(new PointLight(new Vector3D(0.0, 3.6,  1.2), new Color(248, 222, 176), 18.0));
        tunnelLights.add(new PointLight(new Vector3D(0.0, 3.6, -1.2), new Color(246, 218, 170), 14.0));
        tunnelLights.add(new PointLight(new Vector3D(0.0, 3.6, -3.2), new Color(244, 212, 165), 10.0));
        // Extra depth light — extends the corridor perspective into the tunnel exit
        tunnelLights.add(new PointLight(new Vector3D(0.0, 3.6, -5.2), new Color(242, 208, 160),  7.0));

        // Blue LED panels — the visual signature of Monaco tunnel.
        // Placed at X=-1.6 (not X=-2.0): the N·L angle against the polycarbonate face
        // rises from 0.32 to 0.98, making the panel actually glow blue as it should.
        tunnelLights.add(new PointLight(new Vector3D(-1.6, 2.4,  2.5), new Color( 80, 140, 255), 80.0));
        tunnelLights.add(new PointLight(new Vector3D(-1.6, 2.4,  0.0), new Color( 70, 130, 252), 65.0));
        tunnelLights.add(new PointLight(new Vector3D(-1.6, 2.4, -2.5), new Color( 60, 118, 245), 50.0));

        // Soft front fill: illuminates the McLaren nose without erasing shadows
        tunnelLights.add(new PointLight(
            new Vector3D(0.0, 1.0, 5.2),
            new Color(235, 205, 150), 16.0));

        for (Light l : tunnelLights) {
            scene.addLight(l);
        }

        System.out.println("[Info] Generating procedural concrete textures...");
        Texture concreteDiff = ProceduralTexture.concrete(512, 512, 155, 124, 70);
        Texture concreteNorm = ProceduralTexture.concreteNormal(512, 512);

        Material carPaint  = Material.metallic(new Color(245, 120, 28), 0.42, 140);  // Papaya Orange fallback

        // Wet asphalt: low Kd (dark surface scatters little) but high Ks
        // (Blinn-Phong specular is very sharp) → simulates wet track surface.
        Material floorWet = new Material(new Color(4, 4, 5), 0.52, 0.0, 1.0, 200)
                                .withKd(0.08)   // wet asphalt barely scatters diffuse light
                                .withKs(0.92);  // almost mirror-like specular — Monaco wet track

        // Red kerb: high Kd, low Ks — matte paint, no visible specular.
        Material lineaRoja = new Material(new Color(195, 22, 18), 0.0, 0.0, 1.0, 28)
                                 .withKd(0.88)
                                 .withKs(0.12);

        // White lane line: slightly specular (worn old paint).
        Material lineaBlanca = new Material(new Color(215, 212, 205), 0.0, 0.0, 1.0, 48)
                                   .withKd(0.80)
                                   .withKs(0.20);

        // Armco: brushed steel — reduced Ks to avoid overexposing the barrier
        Material armco = Material.metallic(new Color(162, 165, 172), 0.10, 80)
                                 .withKd(0.65).withKs(0.35);
        // Lower wall: worn grey concrete — moisture and rubber marks below 2 m
        Material pitWallLo = Material.diffuse(new Color(105, 100, 92))
                                      .withKd(0.82).withKs(0.18)
                                      .withTexture(concreteDiff)
                                      .withNormalMap(concreteNorm);
        // Upper wall: clean grey-beige concrete — the Sainte-Dévote tunnel palette
        Material pitWallHi = Material.diffuse(new Color(148, 142, 130))
                                      .withKd(0.86).withKs(0.14)
                                      .withTexture(concreteDiff)
                                      .withNormalMap(concreteNorm);
        // Polycarbonate: frosted LED panel — nearly opaque so the blue direct illumination
        // dominates. Refractivity 0.10 means only 10% comes from the dark backing wall.
        Material polycarb = new Material(new Color(80, 140, 255), 0.18, 0.10, 1.49, 100)
                                .withKd(0.55).withKs(0.60);
        // Tunnel exit background: very dark neutral grey — depth illusion
        Material bgTunnel = Material.diffuse(new Color(25, 24, 22))
                                    .withKd(0.95).withKs(0.05);
        // Ceiling: dark neutral concrete — darkness is the atmosphere
        Material ceilMat = Material.diffuse(new Color(20, 19, 17))
                                   .withKd(0.95).withKs(0.05);
        // Low barrier: dark semi-reflective concrete
        Material lowBarrier = Material.metallic(new Color(72, 68, 60), 0.15, 40)
                                      .withKd(0.75).withKs(0.25);
        final double fy = -0.30;   // floor level

        // Floor: tunnel asphalt (moderately reflective)
        quad(scene,
            new Vector3D(-20, fy,  20), new Vector3D(20, fy,  20),
            new Vector3D( 20, fy, -20), new Vector3D(-20, fy, -20), floorWet);

        // Puddles: fan of triangles from a center point → irregular organic silhouette.
        // Nearly black like asphalt, high reflectivity, IOR=1.33 (water).
        double py = fy + 0.002;
        Material charcoMat = new Material(new Color(6, 6, 7), 0.88, 0.05, 1.33, 512)
                                 .withKd(0.02)
                                 .withKs(0.98);

        // Puddle 1 — left side under the spot (~30 cm, fan of 5 triangles)
        Vector3D c1 = new Vector3D(-0.55, py, 0.85);
        scene.addObject(new Triangle(c1, new Vector3D(-0.28, py, 0.68), new Vector3D(-0.22, py, 1.00), charcoMat));
        scene.addObject(new Triangle(c1, new Vector3D(-0.22, py, 1.00), new Vector3D(-0.50, py, 1.12), charcoMat));
        scene.addObject(new Triangle(c1, new Vector3D(-0.50, py, 1.12), new Vector3D(-0.82, py, 1.05), charcoMat));
        scene.addObject(new Triangle(c1, new Vector3D(-0.82, py, 1.05), new Vector3D(-0.85, py, 0.72), charcoMat));
        scene.addObject(new Triangle(c1, new Vector3D(-0.85, py, 0.72), new Vector3D(-0.28, py, 0.68), charcoMat));

        // Puddle 2 — in front of the car, small and elongated (~20 cm × 35 cm)
        Vector3D c2 = new Vector3D(0.10, py, 3.20);
        scene.addObject(new Triangle(c2, new Vector3D( 0.28, py, 3.05), new Vector3D( 0.32, py, 3.28), charcoMat));
        scene.addObject(new Triangle(c2, new Vector3D( 0.32, py, 3.28), new Vector3D( 0.18, py, 3.48), charcoMat));
        scene.addObject(new Triangle(c2, new Vector3D( 0.18, py, 3.48), new Vector3D(-0.15, py, 3.42), charcoMat));
        scene.addObject(new Triangle(c2, new Vector3D(-0.15, py, 3.42), new Vector3D(-0.12, py, 3.08), charcoMat));
        scene.addObject(new Triangle(c2, new Vector3D(-0.12, py, 3.08), new Vector3D( 0.28, py, 3.05), charcoMat));

        // Puddle 3 — against the left barrier, very small (~18 cm)
        Vector3D c3 = new Vector3D(-1.30, py, -0.60);
        scene.addObject(new Triangle(c3, new Vector3D(-1.14, py, -0.70), new Vector3D(-1.12, py, -0.50), charcoMat));
        scene.addObject(new Triangle(c3, new Vector3D(-1.12, py, -0.50), new Vector3D(-1.30, py, -0.42), charcoMat));
        scene.addObject(new Triangle(c3, new Vector3D(-1.30, py, -0.42), new Vector3D(-1.48, py, -0.52), charcoMat));
        scene.addObject(new Triangle(c3, new Vector3D(-1.48, py, -0.52), new Vector3D(-1.46, py, -0.72), charcoMat));
        scene.addObject(new Triangle(c3, new Vector3D(-1.46, py, -0.72), new Vector3D(-1.14, py, -0.70), charcoMat));

        // Tunnel ceiling
        double cy = 4.6;
        quad(scene,
            new Vector3D(-6.0, cy,  7.0), new Vector3D( 6.0, cy,  7.0),
            new Vector3D( 6.0, cy, -6.5), new Vector3D(-6.0, cy, -6.5), ceilMat);

        // Back wall: tunnel exit opening
        quad(scene,
            new Vector3D(-6.0, fy, -6.0), new Vector3D( 6.0, fy, -6.0),
            new Vector3D( 6.0, 5.0, -6.0), new Vector3D(-6.0, 5.0, -6.0), bgTunnel);

        // Left wall — split into three zones:
        // Below barrier: visible concrete (textured grey)
        quadUV(scene,
            new Vector3D(-2.6, fy,   7.0), new Vector3D(-2.6, fy,   -6.0),
            new Vector3D(-2.6, 0.55, -6.0), new Vector3D(-2.6, 0.55,  7.0), pitWallLo, 4.33, 0.38);
        // Behind polycarbonate: near-black so refracted rays return nothing warm.
        // This lets the blue direct illumination dominate over the refracted background.
        Material leftWallBack = Material.diffuse(new Color(12, 10, 8)).withKd(0.90).withKs(0.10);
        quad(scene,
            new Vector3D(-2.6, 0.55,  7.0), new Vector3D(-2.6, 0.55, -6.0),
            new Vector3D(-2.6, 4.40, -6.0), new Vector3D(-2.6, 4.40,  7.0), leftWallBack);
        // Small strip above polycarbonate: visible upper concrete
        quad(scene,
            new Vector3D(-2.6, 4.40,  7.0), new Vector3D(-2.6, 4.40, -6.0),
            new Vector3D(-2.6, 4.60, -6.0), new Vector3D(-2.6, 4.60,  7.0), pitWallHi);

        // Low left barrier (below the panels, Y: floor → 0.55 m).
        // In the reference photo there is a metal rail before the glass panels.
        quad(scene,
            new Vector3D(-2.1, fy,    7.0), new Vector3D(-2.1, fy,   -6.0),
            new Vector3D(-2.1, 0.55, -6.0), new Vector3D(-2.1, 0.55,  7.0), lowBarrier);

        // Blue LED panel — polycarbonate backlit.
        // Extends from Y=0.55 nearly to the ceiling (Y=4.40) so the camera
        // does not see exposed concrete behind it. IOR=1.49 produces lateral refraction.
        quad(scene,
            new Vector3D(-2.1, 0.55,  7.0), new Vector3D(-2.1, 0.55, -6.0),
            new Vector3D(-2.1, 4.40, -6.0), new Vector3D(-2.1, 4.40,  7.0), polycarb);

        // Right wall: concrete in two height bands (3 m tile size — less repetitive)
        quadUV(scene,
            new Vector3D(3.5, fy,  7.0), new Vector3D(3.5, fy, -6.0),
            new Vector3D(3.5, 2.0, -6.0), new Vector3D(3.5, 2.0,  7.0), pitWallLo, 4.33, 0.77);
        quadUV(scene,
            new Vector3D(3.5, 2.0,  7.0), new Vector3D(3.5, 2.0, -6.0),
            new Vector3D(3.5, 4.6, -6.0), new Vector3D(3.5, 4.6,  7.0), pitWallHi, 4.33, 0.87);

        // Right Armco barrier
        // Front face (facing the camera)
        quad(scene,
            new Vector3D(2.0, fy,  7.0), new Vector3D(2.0, fy, -6.0),
            new Vector3D(2.0, 0.52, -6.0), new Vector3D(2.0, 0.52,  7.0), armco);
        // Top face (barrier edge)
        quad(scene,
            new Vector3D(2.0, 0.52,  7.0), new Vector3D(2.0, 0.52, -6.0),
            new Vector3D(2.6, 0.52, -6.0), new Vector3D(2.6, 0.52,  7.0), armco);

        // Vertical concrete pillar ribs protruding 28 cm from the right wall.
        // Spaced every 2 m in Z — breaks the flat surface and casts directional shadows
        // from the overhead LEDs, giving the wall depth and rhythm.
        Material ribMat = Material.diffuse(new Color(92, 88, 80)).withKd(0.88).withKs(0.12);
        double ribInnerX = 3.22;
        double ribHalfW  = 0.13;
        for (double rz : new double[]{-5.0, -3.0, -1.0, 1.0, 3.0}) {
            // Inner face (visible from camera)
            quad(scene,
                new Vector3D(ribInnerX, fy,  rz - ribHalfW),
                new Vector3D(ribInnerX, fy,  rz + ribHalfW),
                new Vector3D(ribInnerX, 4.6, rz + ribHalfW),
                new Vector3D(ribInnerX, 4.6, rz - ribHalfW), ribMat);
            // Side face toward +Z
            quad(scene,
                new Vector3D(ribInnerX, fy,  rz + ribHalfW),
                new Vector3D(3.50,      fy,  rz + ribHalfW),
                new Vector3D(3.50,      4.6, rz + ribHalfW),
                new Vector3D(ribInnerX, 4.6, rz + ribHalfW), ribMat);
            // Side face toward -Z
            quad(scene,
                new Vector3D(3.50,      fy,  rz - ribHalfW),
                new Vector3D(ribInnerX, fy,  rz - ribHalfW),
                new Vector3D(ribInnerX, 4.6, rz - ribHalfW),
                new Vector3D(3.50,      4.6, rz - ribHalfW), ribMat);
        }

        // Horizontal dado ledge at Y=2.0: a shallow protrusion that marks
        // the boundary between the stained lower zone and the cleaner upper wall.
        Material ledgeMat = Material.diffuse(new Color(118, 112, 102)).withKd(0.86).withKs(0.14);
        quad(scene,   // front face (facing camera, at X=ribInnerX)
            new Vector3D(ribInnerX, 1.96,  7.0), new Vector3D(ribInnerX, 1.96, -6.0),
            new Vector3D(ribInnerX, 2.04, -6.0), new Vector3D(ribInnerX, 2.04,  7.0), ledgeMat);
        quad(scene,   // top face (facing ceiling)
            new Vector3D(ribInnerX, 2.04, -6.0), new Vector3D(ribInnerX, 2.04,  7.0),
            new Vector3D(3.50,      2.04,  7.0), new Vector3D(3.50,      2.04, -6.0), ledgeMat);

        // Aluminum mullion frames between the polycarbonate panels.
        // Placed 1 cm in front of the polycarbonate (X=-2.09) to avoid z-fighting.
        Material mullion = Material.metallic(new Color(85, 88, 93), 0.22, 100)
                                    .withKd(0.50).withKs(0.50);
        for (double mz : new double[]{-5.0, -2.5, 0.0, 2.5, 5.0}) {
            quad(scene,
                new Vector3D(-2.09, 0.55, mz - 0.085),
                new Vector3D(-2.09, 0.55, mz + 0.085),
                new Vector3D(-2.09, 4.40, mz + 0.085),
                new Vector3D(-2.09, 4.40, mz - 0.085), mullion);
        }

        // LED strip housing panels on the ceiling, 2 cm below the ceiling surface.
        // Not directly visible from the ground-level camera, but they appear as
        // bright white rectangles in the wet-asphalt floor reflection.
        Material ledHousing = Material.diffuse(new Color(242, 238, 224)).withKd(0.55).withKs(0.80);
        for (double lz : new double[]{3.2, 1.2, -1.2, -3.2}) {
            quad(scene,
                new Vector3D(-0.30, 4.58, lz - 0.40),
                new Vector3D( 0.30, 4.58, lz - 0.40),
                new Vector3D( 0.30, 4.58, lz + 0.40),
                new Vector3D(-0.30, 4.58, lz + 0.40), ledHousing);
        }

        // Red-white kerbs
        // Red (Monaco outer kerb)
        scene.addObject(new Triangle(
            new Vector3D(1.40, fy,  7.0), new Vector3D(1.70, fy,  7.0),
            new Vector3D(1.70, fy, -6.0), lineaRoja));
        scene.addObject(new Triangle(
            new Vector3D(1.40, fy,  7.0), new Vector3D(1.70, fy, -6.0),
            new Vector3D(1.40, fy, -6.0), lineaRoja));
        // White (kerb before the Armco)
        scene.addObject(new Triangle(
            new Vector3D(1.70, fy,  7.0), new Vector3D(2.00, fy,  7.0),
            new Vector3D(2.00, fy, -6.0), lineaBlanca));
        scene.addObject(new Triangle(
            new Vector3D(1.70, fy,  7.0), new Vector3D(2.00, fy, -6.0),
            new Vector3D(1.70, fy, -6.0), lineaBlanca));
        // Central lane line
        scene.addObject(new Triangle(
            new Vector3D(-0.06, fy,  7.0), new Vector3D(0.06, fy,  7.0),
            new Vector3D( 0.06, fy, -6.0), lineaBlanca));
        scene.addObject(new Triangle(
            new Vector3D(-0.06, fy,  7.0), new Vector3D(0.06, fy, -6.0),
            new Vector3D(-0.06, fy, -6.0), lineaBlanca));

        // Car model — try several candidate paths in order
        String[] candidatos = {
            "../models/mclaren_senna.obj",
            "../models/mclaren_p1.obj",
            "../models/mclaren_f1.obj",
            "../models/koenigsegg.obj",
            "../models/koenigsegg_simplified.obj"
        };
        String objPath = null;
        for (String p : candidatos) {
            if (new File(p).exists()) { objPath = p; break; }
        }
        if (objPath == null) {
            System.err.println("[ERROR] No OBJ model found.");
            return;
        }

        ObjModel car = ObjReader.parse(objPath);
        car.printSummary();

        // Texture directories — relative to the src/ working directory
        String TEX     = "../textures/";
        String TEX_SRC = "../textures/source/";

        // Material map keyed by OBJ group name (usemtl)
        java.util.Map<String, Material> carMats = new java.util.HashMap<>();

        // Main body: McLaren Papaya Orange — the iconic factory colour
        Material mPaint   = Material.metallic(new Color(245, 120, 28), 0.42, 140);

        // Coloured panels: same Papaya Orange (roof + large body panels)
        Material mColour  = Material.metallic(new Color(245, 120, 28), 0.42, 140);

        // Carbon fiber with diffuse texture + diagonal weave normal map
        Material mCarbon  = Material.metallic(new Color( 18,  18,  20), 0.22, 350)
                            .withTexture(Texture.load(TEX + "common_carbon05_black_diff.png"))
                            .withNormalMap(Texture.load(TEX + "common_carbon05_norm.png"));

        // Wheels/rims: texture + normal map for rim detail
        Material mWheel   = Material.metallic(new Color(188, 190, 195), 0.45, 200)
                            .withTexture(Texture.load(TEX + "McLaren_Senna_2018_Wheel1B_DiffuseAOSO.png"))
                            .withNormalMap(Texture.load(TEX + "McLaren_Senna_2018_Wheel1A_Normal.png"));

        // Brake calipers: McLaren orange (no texture)
        Material mCaliper = Material.metallic(new Color(255, 108,   0), 0.25, 150);

        // Tinted glass (refractive — no texture)
        Material mGlass   = Material.glass   (new Color(160, 180, 200), 1.52, 0.55);

        // Headlights with internal optic texture
        Material mLight   = Material.metallic(new Color(255, 225, 170), 0.15, 100)
                            .withTexture(Texture.load(TEX + "McLaren_Senna_2018_LightA_Diffuse.png"));

        // Rear turn signals (translucent red — no texture)
        Material mRedGlass= Material.glass   (new Color(220,  18,  12), 1.48, 0.45);

        // Interior with leather/alcantara texture
        Material mInterior= Material.diffuse (new Color( 42,  38,  35))
                            .withTexture(Texture.load(TEX + "McLaren_Senna_2018_InteriorB_DiffuseAOSO.png"))
                            .withNormalMap(Texture.load(TEX + "McLaren_Senna_2018_InteriorA_Normal.png"));

        // Engine and mechanical parts with texture + normal map
        Material mEngine  = Material.metallic(new Color( 60,  60,  65), 0.25, 120)
                            .withTexture(Texture.load(TEX + "McLaren_Senna_2018_EngineA_DiffuseAOSO.png"))
                            .withNormalMap(Texture.load(TEX + "McLaren_Senna_2018_EngineA_Normal.png"));

        // Base / structure (matte black — no texture)
        Material mBase    = Material.diffuse (new Color( 28,  28,  32));

        // Badge A — uses the correct source texture (BadgeA, not BadgeB)
        Material mBadgeA  = Material.metallic(new Color(218, 215, 210), 0.55, 280)
                            .withTexture(Texture.load(TEX_SRC + "McLaren_Senna_2018_BadgeA_DiffuseAOSO.png"))
                            .withNormalMap(Texture.load(TEX + "McLaren_Senna_2018_BadgeA_Normal.png"));

        // Manufacturer plate with its own texture
        Material mManuPlt = Material.metallic(new Color(218, 215, 210), 0.55, 280)
                            .withTexture(Texture.load(TEX + "McLaren_Senna_2018_ManufacturerPlateA_Diffuse.png"));

        // Textured parts (rubber, plastic)
        Material mTextured= Material.diffuse (new Color( 20,  20,  22))
                            .withTexture(Texture.load(TEX + "McLaren_Senna_2018_TexturedB_Diffuse.png"));

        carMats.put("m:McLaren_SennaLE_2018Paint_Material1",                    mPaint);
        carMats.put("m:McLaren_SennaLE_2018Coloured_Material1",                 mColour);
        carMats.put("m:McLaren_SennaLE_2018Carbon1M_Material1",                 mCarbon);
        carMats.put("m:McLaren_SennaLE_2018_Wheel1A_3D_3DWheel1B_Material1",    mWheel);
        carMats.put("m:McLaren_SennaLE_2018_CallipersCalliperGloss_Material1",  mCaliper);
        // Caliper badge: its texture is only available in source/
        Material mCaliperBadge = Material.metallic(new Color(218, 215, 210), 0.40, 200)
                            .withTexture(Texture.load(TEX_SRC + "McLaren_Senna_2018_CalliperBadgeA_Diffuse.png"));
        carMats.put("m:McLaren_SennaLE_2018_CallipersCalliperBadgeA_Material1", mCaliperBadge);
        carMats.put("m:McLaren_SennaLE_2018Window_Material1",                   mGlass);
        carMats.put("m:McLaren_SennaLE_2018LightA_Material1",                   mLight);
        carMats.put("red_glass",                                                 mRedGlass);
        carMats.put("m:McLaren_SennaLE_2018InteriorB_Material1",                mInterior);
        // Grilles 1-7: diffuse texture (TEX) + normal map (TEX_SRC has all of them, including Grille3A)
        for (int n = 1; n <= 7; n++) {
            Texture gt = Texture.load(TEX + "McLaren_Senna_2018_Grille" + n + "A_DiffuseAOSO.png");
            Texture gn = Texture.load(TEX_SRC + "McLaren_Senna_2018_Grille" + n + "A_Normal.png");
            carMats.put("m:McLaren_SennaLE_2018Grille" + n + "A_Material1",
                        Material.diffuse(new Color(10, 10, 12)).withTexture(gt).withNormalMap(gn));
        }
        carMats.put("m:McLaren_SennaLE_2018EngineA_Material1",                  mEngine);
        carMats.put("m:McLaren_SennaLE_2018Base_Material1",                     mBase);
        carMats.put("m:McLaren_SennaLE_2018BadgeA_Material1",                   mBadgeA);
        carMats.put("m:McLaren_SennaLE_2018TexturedB_Material1",                mTextured);
        carMats.put("m:McLaren_SennaLE_2018ManufacturerPlateA_Material1",       mManuPlt);
        carMats.put("cal_2",                                                     mCaliper);

        // OBJ in real-world scale (meters) → scale=1.0, center by centroid
        double     scale    = 1.0;
        Vector3D[] bb       = car.boundingBox();
        Vector3D   centroid = car.centroid();
        Vector3D   offset   = new Vector3D(
            -centroid.x * scale,
            fy - bb[0].y * scale,
            -centroid.z * scale - 0.5
        );
        car.addToScene(scene, carMats, carPaint, offset, scale);

        System.out.printf("[Info] Model: %s%n", objPath);
        System.out.printf("[Info] Car X:[%.2f,%.2f] Y:[%.2f,%.2f] Z:[%.2f,%.2f]%n",
            bb[0].x*scale+offset.x, bb[1].x*scale+offset.x,
            bb[0].y*scale+offset.y, bb[1].y*scale+offset.y,
            bb[0].z*scale+offset.z, bb[1].z*scale+offset.z);

        int bounces = finalRender ? 5 : 3;
        Raytracer rt = new Raytracer(scene, camera, bounces);

        System.out.println("[Info] Starting render...");
        long t0 = System.currentTimeMillis();
        BufferedImage img = rt.render();
        double secs = (System.currentTimeMillis() - t0) / 1000.0;

        String fname = finalRender
            ? "Scene1_McLaren_Monaco_FINAL_4096x2160.png"
            : "Scene1_McLaren_Monaco_1024x576.png";
        File outDir  = new File("output");
        if (!outDir.exists()) outDir.mkdirs();
        File outFile = new File(outDir, fname);
        ImageIO.write(img, "PNG", outFile);

        System.out.println("\n+---------------------------------------------------------------+");
        System.out.printf("| Completed in %.1f s → %s%n", secs, outFile.getName());
        System.out.println("+---------------------------------------------------------------+");
    }

    /** Quad = two triangles (a-b-c and a-c-d), counter-clockwise from front. */
    private static void quad(Scene scene,
                              Vector3D a, Vector3D b,
                              Vector3D c, Vector3D d, Material mat) {
        scene.addObject(new Triangle(a, b, c, mat));
        scene.addObject(new Triangle(a, c, d, mat));
    }

    /**
     * UV-mapped quad. su × sv UV scale controls how many times the texture tiles
     * across the surface (e.g. su=8.67 tiles the texture every 1.5 m over a 13 m wall).
     */
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
