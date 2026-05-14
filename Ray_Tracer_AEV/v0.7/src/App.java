import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * App – Raytracer v0.7
 *
 * New features vs v0.6:
 *   1. Light Falloff  – PointLight intensity decreases with distance squared:
 *                       LI = intensity / d²  (Session 24, slide 5)
 *   2. Shadows        – shadow rays are cast from each hit point toward every
 *                       light; occluded points receive only ambient light
 *                       (Session 24, slides 7-13)
 *
 * Scene: a white floor plane, a cube, and spheres casting visible shadows.
 * The point light is placed close to objects so falloff is clearly visible.
 */
public class App {
    public static void main(String[] args) {
        int width  = 800;
        int height = 600;

        // ── Camera ────────────────────────────────────────────────────────────
        Camera camera = new Camera(
            new Vector3D(3, 4, -7),
            new Vector3D(0, 0.3,  0),
            new Vector3D(0, 1,    0),
            55.0,
            width, height,
            0.1, 100.0
        );

        // ── Scene ─────────────────────────────────────────────────────────────
        Scene scene = new Scene(new Color(0, 0, 0));

        // ── Lights ────────────────────────────────────────────────────────────
        // Directional key light – white, slightly from above-left
        // No falloff (infinite distance), so it lights the whole scene evenly
        scene.addLight(new DirectionalLight(
            new Vector3D(-1.0, -1.5, 1.0),
            new Color(255, 255, 255),
            0.8
        ));

        // Point light – placed close to objects so falloff is clearly visible
        // Nearby surfaces are much brighter than distant ones (slide 4 render)
        scene.addLight(new PointLight(
            new Vector3D(1.5, 3.0, -1.0),   // position near the cube
            new Color(255, 240, 200),         // warm yellow
            12.0                              // high base intensity (falloff reduces it)
        ));

        // Second point light from the other side – creates secondary shadows
        scene.addLight(new PointLight(
            new Vector3D(-2.0, 2.5,  0.5),
            new Color(180, 210, 255),          // cool blue
            8.0
        ));

        // ── Floor plane (large flat quad = 2 triangles) ───────────────────────
        // White floor so shadows are clearly visible (slide 4 shows floor shadows)
        Vector3D fl0 = new Vector3D(-6, 0,  6);
        Vector3D fl1 = new Vector3D(-6, 0, -6);
        Vector3D fl2 = new Vector3D( 6, 0,  6);
        Vector3D fl3 = new Vector3D( 6, 0, -6);
        scene.addObject(new Triangle(fl0, fl1, fl2, new Color(200, 200, 200)));
        scene.addObject(new Triangle(fl1, fl3, fl2, new Color(200, 200, 200)));

        // ── OBJ model ─────────────────────────────────────────────────────────
        String objPath = "models/cube.obj";
        try {
            System.out.println("[App] Loading OBJ: " + objPath);
            ObjModel model = ObjReader.parse(objPath);
            model.printSummary();

            // Main cube – white so both light colours are visible
            model.addToScene(scene, new Color(230, 230, 230));

            // Second smaller cube offset – will cast shadow onto the floor
            model.addToScene(scene, new Color(180, 100, 60),
                             new Vector3D(-1.2, 0, 1.0), 0.5);

        } catch (Exception e) {
            System.err.println("[App] OBJ load failed: " + e.getMessage());
        }

        // ── Spheres ───────────────────────────────────────────────────────────
        // Red sphere – close to the point light so falloff is visible on its far side
        scene.addObject(new Sphere(new Vector3D(-0.6, 0.4,  0.2), 0.40, Color.RED));

        // Blue sphere – further from the light, visibly dimmer due to falloff
        scene.addObject(new Sphere(new Vector3D( 0.6, 0.35, -0.4), 0.30,
                                   new Color(60, 80, 220)));

        // Small green sphere – sits on the floor, casts a visible shadow
        scene.addObject(new Sphere(new Vector3D(-0.1, 0.18,  0.8), 0.18,
                                   new Color(50, 200, 80)));

        // ── Render ────────────────────────────────────────────────────────────
        System.out.println("[App] Rendering v0.7 (falloff + shadows)…");
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
        JFrame frame = new JFrame("Raytracer v0.7 – Falloff + Shadows");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
