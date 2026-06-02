import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Fast preview of Scene 1 - McLaren Monaco Night (512x288, 1 bounce).
 * Uses the same scene as App_Scene1 but at low resolution to verify
 * composition, camera and materials before the final render.
 */
public class App_Scene1_Fast {
    public static void main(String[] args) throws Exception {
        int width  = 512;
        int height = 288;

        System.out.println("+----------------------------------------------------------+");
        System.out.println("|   SCENE 1 FAST: McLAREN MONACO NIGHT  512x288 preview   |");
        System.out.println("+----------------------------------------------------------+");

        // McLaren (scale=1.0): X≈[-1.1, 1.1], Y≈[-0.3, 0.95], Z≈[-2.87, 1.87]
        Camera camera = new Camera(
            new Vector3D(2.2, 0.75, 4.2),
            new Vector3D(0.0, 0.35, -0.5),
            new Vector3D(0, 1, 0),
            50.0, width, height, 0.1, 300.0
        );

        Scene scene = new Scene(new Color(8, 10, 18));

        scene.addLight(new DirectionalLight(
            new Vector3D(-0.15, -0.85, -0.45),
            new Color(255, 232, 180), 2.0));
        scene.addLight(new PointLight(
            new Vector3D(6.5, 4.0, 0.5),
            new Color(255, 205, 120), 150.0));
        scene.addLight(new PointLight(
            new Vector3D(-5.5, 3.0, 4.5),
            new Color(60, 100, 200), 50.0));

        Material carPaint     = Material.metallic(new Color(255, 88, 0),    0.50, 200);
        Material floorWet     = Material.metallic(new Color(11, 12, 17),    0.62, 100);
        Material glassBarrier = Material.glass   (new Color(200, 215, 228), 1.52, 0.68);
        Material concreteMat  = Material.diffuse (new Color(168, 163, 153));
        Material curbRed      = Material.diffuse (new Color(195, 32, 28));
        Material bgWall       = Material.diffuse (new Color(20, 24, 34));

        double fy = -0.30, fsz = 25.0;
        scene.addObject(new Triangle(new Vector3D(-fsz,fy, fsz), new Vector3D(fsz,fy, fsz), new Vector3D(fsz,fy,-fsz), floorWet));
        scene.addObject(new Triangle(new Vector3D(-fsz,fy, fsz), new Vector3D(fsz,fy,-fsz), new Vector3D(-fsz,fy,-fsz), floorWet));

        // Glass side barrier (left of car) → refraction visible
        double gx = -2.5;
        scene.addObject(new Triangle(new Vector3D(gx,fy, 4.5), new Vector3D(gx,fy,-4.5), new Vector3D(gx,2.5,-4.5), glassBarrier));
        scene.addObject(new Triangle(new Vector3D(gx,fy, 4.5), new Vector3D(gx,2.5,-4.5), new Vector3D(gx,2.5, 4.5), glassBarrier));

        double bz = -5.0;
        scene.addObject(new Triangle(new Vector3D(-12,fy,bz), new Vector3D(12,fy,bz), new Vector3D(12,6,bz), bgWall));
        scene.addObject(new Triangle(new Vector3D(-12,fy,bz), new Vector3D(12,6,bz), new Vector3D(-12,6,bz), bgWall));

        // Pit wall (right of car)
        scene.addObject(new Triangle(new Vector3D(2.5,fy, 4.5), new Vector3D(2.5,fy,-5.0), new Vector3D(2.5,2.0,-5.0), concreteMat));
        scene.addObject(new Triangle(new Vector3D(2.5,fy, 4.5), new Vector3D(2.5,2.0,-5.0), new Vector3D(2.5,2.0, 4.5), concreteMat));
        scene.addObject(new Triangle(new Vector3D(2.5,2.0, 4.5), new Vector3D(2.5,2.0,-5.0), new Vector3D(4.0,2.0,-5.0), concreteMat));
        scene.addObject(new Triangle(new Vector3D(2.5,2.0, 4.5), new Vector3D(4.0,2.0,-5.0), new Vector3D(4.0,2.0, 4.5), concreteMat));

        scene.addObject(new Triangle(new Vector3D(2.0,fy, 4.5), new Vector3D(2.5,fy, 4.5), new Vector3D(2.5,fy,-5.0), curbRed));
        scene.addObject(new Triangle(new Vector3D(2.0,fy, 4.5), new Vector3D(2.5,fy,-5.0), new Vector3D(2.0,fy,-5.0), curbRed));

        // Car model — try several candidate paths in order
        String[] candidates = {
            "../models/mclaren_senna.obj", "../models/mclaren_p1.obj", "../models/mclaren_f1.obj",
            "../models/koenigsegg.obj", "../models/koenigsegg_simplified.obj"
        };
        String objPath = null;
        for (String p : candidates) if (new File(p).exists()) { objPath = p; break; }

        if (objPath == null) {
            System.err.println("[ERROR] No OBJ model found."); return;
        }

        ObjModel car = ObjReader.parse(objPath);
        car.printSummary();

        // McLaren OBJ already in real-world scale (meters)
        double     scale    = 1.0;
        Vector3D[] bb       = car.boundingBox();
        Vector3D   centroid = car.centroid();
        Vector3D   offset   = new Vector3D(
            -centroid.x * scale,
            fy - bb[0].y * scale,
            -centroid.z * scale - 0.5
        );
        car.addToScene(scene, carPaint, offset, scale);
        System.out.printf("[Info] Car X:[%.2f,%.2f] Y:[%.2f,%.2f] Z:[%.2f,%.2f]%n",
            bb[0].x*scale+offset.x, bb[1].x*scale+offset.x,
            bb[0].y*scale+offset.y, bb[1].y*scale+offset.y,
            bb[0].z*scale+offset.z, bb[1].z*scale+offset.z);

        long t0 = System.currentTimeMillis();
        Raytracer rt = new Raytracer(scene, camera, 1);
        BufferedImage img = rt.render();
        double secs = (System.currentTimeMillis() - t0) / 1000.0;

        File outDir = new File("output");
        if (!outDir.exists()) outDir.mkdirs();
        File outFile = new File(outDir, "Scene1_FAST_512x288.png");
        ImageIO.write(img, "PNG", outFile);
        System.out.printf("[OK] %.1f s → %s%n", secs, outFile.getName());
    }
}
