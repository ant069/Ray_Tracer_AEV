import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * App – Raytracer v0.6
 *
 * New feature vs v0.5:
 *   PointLight – emits light in all directions from a world-space position.
 *   L = normalize(lightPosition - hitPoint), so each surface point receives
 *   light from a different angle depending on its position.
 *
 * Bug fixed vs v0.4/v0.5:
 *   Raytracer.traceRay() now calls shader.shade() so lighting is actually
 *   applied to the rendered pixels.
 *
 * Scene matches Session 23 slides 3-7:
 *   - 1 Directional light  (slide 3: white, intensity 1.1, direction (0,0,1))
 *   - 1 Directional light  (slide 4: red,   intensity 1.1, direction (0,-1,0))
 *   - 1 Point light        (slide 5: white, intensity 0.9, position (0,1,0))
 *   (Slides 6-7 show how combining lights blends their contributions)
 */
public class App {
    public static void main(String[] args) {
        int width  = 800;
        int height = 600;

        // ── Camera ────────────────────────────────────────────────────────────
        Camera camera = new Camera(
            new Vector3D(3, 3, -6),
            new Vector3D(0, 0.5,  0),
            new Vector3D(0, 1,    0),
            60.0,
            width, height,
            0.1, 100.0
        );

        // ── Scene ─────────────────────────────────────────────────────────────
        Scene scene = new Scene(new Color(0, 0, 0));

        // ── Lights (Session 23 slides 3-7) ────────────────────────────────────
        // Directional light 1 – white, direction (0,0,1) (slide 3)
        scene.addLight(new DirectionalLight(
            new Vector3D(0.0, 0.0, 1.0),
            new Color(255, 255, 255),
            1.1
        ));

        // Directional light 2 – red, direction (0,-1,0) (slide 4)
        scene.addLight(new DirectionalLight(
            new Vector3D(0.0, -1.0, 0.0),
            new Color(255, 0, 0),
            1.1
        ));

        // Point light – white, position (0,1,0) (slide 5) ← NEW in v0.6
        scene.addLight(new PointLight(
            new Vector3D(0.0, 1.0, 0.0),
            new Color(255, 255, 255),
            0.9
        ));

        // ── OBJ model ─────────────────────────────────────────────────────────
        String objPath = "models/cube.obj";
        try {
            System.out.println("[App] Loading OBJ: " + objPath);
            ObjModel model = ObjReader.parse(objPath);
            model.printSummary();

            // White cube – shading visible on all sides
            model.addToScene(scene, new Color(255, 255, 255));

            // Second cyan cube – offset
            model.addToScene(scene, new Color(80, 200, 220),
                             new Vector3D(1.5, 0, 1.5), 0.6);

        } catch (Exception e) {
            System.err.println("[App] OBJ load failed: " + e.getMessage());
        }

        // ── Reference spheres ─────────────────────────────────────────────────
        scene.addObject(new Sphere(new Vector3D(-1.0, 1.2,  0.3), 0.35, Color.RED));
        scene.addObject(new Sphere(new Vector3D( 0.8, 1.8,  0.4), 0.22, Color.BLUE));
        scene.addObject(new Sphere(new Vector3D( 0.05, 1.1,-0.3), 0.14, new Color(50, 220, 50)));

        // ── Render ────────────────────────────────────────────────────────────
        Raytracer raytracer = new Raytracer(scene, camera);
        BufferedImage image = raytracer.render();

        try {
            File output = new File("render.png");
            ImageIO.write(image, "png", output);
            System.out.println("[App] Render saved: " + output.getAbsolutePath());
        } catch (Exception ex) {
            System.err.println("[App] Error saving: " + ex.getMessage());
        }

        showImage(image);
    }

    private static void showImage(BufferedImage image) {
        JFrame frame = new JFrame("Raytracer v0.6 – Point Light + Flat Shading");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
