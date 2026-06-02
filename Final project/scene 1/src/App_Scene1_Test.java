import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * SCENE 1 TEST - Simple version without OBJ to verify raytracer functionality.
 */
public class App_Scene1_Test {
    public static void main(String[] args) throws Exception {
        int width  = 640;
        int height = 360;

        System.out.println("+---------------------------------------------------------------+");
        System.out.println("|        SCENE 1 TEST: Raytracer Testing (NO OBJ)             |");
        System.out.println("|                  Resolution: " + width + "x" + height + "                  |");
        System.out.println("+---------------------------------------------------------------+");

        Camera camera = new Camera(
            new Vector3D(2.0, 1.5, -5.0),
            new Vector3D(0, 0.5, 0),
            new Vector3D(0, 1, 0),
            60.0,
            width, height,
            0.1, 300.0
        );

        Scene scene = new Scene(new Color(20, 20, 30));

        scene.addLight(new DirectionalLight(
            new Vector3D(-0.3, -0.8, -0.5),
            new Color(240, 240, 255),
            1.5
        ));

        scene.addLight(new PointLight(
            new Vector3D(2.5, 2.0, -2.0),
            new Color(255, 180, 120),
            80.0
        ));

        Material floorMat = Material.metallic(new Color(30, 30, 40), 0.25, 64);
        double floorSize = 20.0;
        scene.addObject(new Triangle(
            new Vector3D(-floorSize, -0.3, floorSize),
            new Vector3D(floorSize, -0.3, floorSize),
            new Vector3D(floorSize, -0.3, -floorSize),
            floorMat
        ));
        scene.addObject(new Triangle(
            new Vector3D(-floorSize, -0.3, floorSize),
            new Vector3D(floorSize, -0.3, -floorSize),
            new Vector3D(-floorSize, -0.3, -floorSize),
            floorMat
        ));

        Material redMat = Material.metallic(new Color(200, 40, 40), 0.6, 120);
        scene.addObject(new Sphere(new Vector3D(0, 0.5, 0), 0.6, redMat));

        Material blueMat = Material.metallic(new Color(40, 100, 200), 0.5, 100);
        scene.addObject(new Sphere(new Vector3D(-1.5, 0.8, 1.0), 0.5, blueMat));

        System.out.println("[Info] Scene ready: 2 spheres + floor");
        System.out.println("[Info] Starting render...");
        long startTime = System.currentTimeMillis();

        Raytracer raytracer = new Raytracer(scene, camera, 2);
        BufferedImage img = raytracer.render();

        long elapsed = System.currentTimeMillis() - startTime;
        double seconds = elapsed / 1000.0;

        File outputDir = new File("output");
        if (!outputDir.exists()) outputDir.mkdirs();

        File outputFile = new File(outputDir, "Scene_1_TEST_640x360.png");
        ImageIO.write(img, "PNG", outputFile);

        System.out.println("\n+---------------------------------------------------------------+");
        System.out.printf("| Time: %.2f seconds | File: %s\n", seconds, outputFile.getName());
        System.out.println("+---------------------------------------------------------------+");
    }
}
