import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * App – Raytracer v0.3
 *
 * New feature vs v0.2:
 *   OBJ Reader – loads any .obj file with the custom ObjReader (no external
 *   libraries), triangulates quads via fan method, and adds the resulting
 *   Triangle objects to the Scene.
 *
 * Scene: a cube loaded from models/cube.obj, plus two spheres for reference.
 */
public class App {
    public static void main(String[] args) {
        int width  = 800;
        int height = 600;

        // ── Camera ────────────────────────────────────────────────────────────
        Camera camera = new Camera(
            new Vector3D(3, 3, -6),     // position  – slightly to the side
            new Vector3D(0, 0.5,  0),   // look-at   – centre of the cube
            new Vector3D(0, 1,    0),   // up
            60.0,                       // fov
            width, height,
            0.1, 100.0                  // near / far clipping
        );

        // ── Scene ─────────────────────────────────────────────────────────────
        Scene scene = new Scene(new Color(15, 15, 30), new Vector3D(5, 10, -5));

        // ── OBJ model ─────────────────────────────────────────────────────────
        // Path is relative to the working directory from which you run javac/java.
        // Run from v0.3/  →  java -cp src App
        String objPath = "models/cube.obj";
        try {
            System.out.println("[App] Loading OBJ: " + objPath);
            ObjModel model = ObjReader.parse(objPath);
            model.printSummary();

            // Add the cube to the scene (centred at origin, scale 1)
            model.addToScene(scene, new Color(220, 160, 40));   // gold

            // A second copy shifted to the right, scaled up
            model.addToScene(scene, new Color(80, 180, 220),    // cyan
                             new Vector3D(1.5, 0, 1.5), 0.6);

        } catch (Exception e) {
            System.err.println("[App] Could not load OBJ: " + e.getMessage());
            System.err.println("[App] Continuing with sphere-only scene.");
        }

        // ── Reference spheres ─────────────────────────────────────────────────
        scene.addObject(new Sphere(new Vector3D(-1.2, 1.5, 0.0), 0.3, Color.RED));
        scene.addObject(new Sphere(new Vector3D( 1.0, 1.8, 0.5), 0.2, Color.BLUE));

        // ── Render ────────────────────────────────────────────────────────────
        Raytracer raytracer = new Raytracer(scene, camera);
        BufferedImage image = raytracer.render();

        // Save to file
        try {
            File output = new File("render.png");
            ImageIO.write(image, "png", output);
            System.out.println("[App] Render saved: " + output.getAbsolutePath());
        } catch (Exception ex) {
            System.err.println("[App] Error saving image: " + ex.getMessage());
        }

        showImage(image);
    }

    private static void showImage(BufferedImage image) {
        JFrame frame = new JFrame("Raytracer v0.3 – OBJ Reader");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
