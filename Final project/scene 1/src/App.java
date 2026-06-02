import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Raytracer v0.9 – Test scene with reflection and refraction.
 *
 * Scene:
 *   - Red specular sphere (mirror reflection)
 *   - Blue refractive sphere (glass)
 *   - White diffuse test sphere
 *   - Flat grey floor
 *   - Multiple light sources
 *
 * Note: This is a TEST scene. For the final deliverable,
 * use OBJ models such as the Koenigsegg.
 */
public class App {
    public static void main(String[] args) throws Exception {
        int width  = 1024;
        int height = 768;

        Camera camera = new Camera(
            new Vector3D(0, 2, -8),      // position
            new Vector3D(0, 1, 0),       // look-at target
            new Vector3D(0, 1, 0),       // up vector
            45.0,                        // FOV
            width, height,
            0.1, 200.0
        );

        Scene scene = new Scene(new Color(20, 20, 40)); // dark blue background

        // Key light: white directional
        scene.addLight(new DirectionalLight(
            new Vector3D(-1.0, -2.0, 0.5),
            new Color(255, 255, 255),
            1.2
        ));

        // Warm point light from upper-right
        scene.addLight(new PointLight(
            new Vector3D(4.0, 4.0, 2.0),
            new Color(255, 220, 180),
            20.0
        ));

        // Blue fill light from the left
        scene.addLight(new PointLight(
            new Vector3D(-4.0, 2.0, 0.0),
            new Color(100, 150, 255),
            15.0
        ));

        // 1. Red mirror sphere
        Material mirrorRed = Material.mirror(new Color(220, 50, 50), 0.8);
        scene.addObject(new Sphere(new Vector3D(-2, 1.2, 0), 0.8, mirrorRed));

        // 2. Blue glass sphere
        Material glassBlue = Material.glass(new Color(100, 150, 255), 1.5, 0.9);
        scene.addObject(new Sphere(new Vector3D(2, 1.2, 0), 0.8, glassBlue));

        // 3. White diffuse test sphere
        Material diffuseWhite = Material.diffuse(new Color(200, 200, 200));
        scene.addObject(new Sphere(new Vector3D(0, 1.2, 2.5), 0.5, diffuseWhite));

        // 4. Floor (two triangles)
        Material floorMat = Material.diffuse(new Color(100, 100, 100));
        double floorY = 0.0;
        double floorSize = 20.0;

        scene.addObject(new Triangle(
            new Vector3D(-floorSize, floorY, -floorSize),
            new Vector3D(floorSize, floorY, -floorSize),
            new Vector3D(floorSize, floorY, floorSize),
            floorMat
        ));

        scene.addObject(new Triangle(
            new Vector3D(-floorSize, floorY, -floorSize),
            new Vector3D(floorSize, floorY, floorSize),
            new Vector3D(-floorSize, floorY, floorSize),
            floorMat
        ));

        Raytracer raytracer = new Raytracer(scene, camera, 4);
        BufferedImage img = raytracer.render();

        File outputFile = new File("output_v0.9_reflection_refraction.png");
        ImageIO.write(img, "PNG", outputFile);
        System.out.printf("✓ Image saved: %s%n", outputFile.getAbsolutePath());
    }
}
