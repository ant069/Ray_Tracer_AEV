import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * App – Raytracer v0.5
 *
 * NEW feature vs v0.4:
 *   Phong (smooth) shading – per-vertex normals are read from the OBJ file
 *   and interpolated across each triangle using barycentric coordinates.
 *   The interpolated normal is used in the Lambertian diffuse formula so
 *   the shading transitions smoothly across polygon edges (slide 8, 11).
 *
 * Scene:
 *   • A cube loaded from cube_smooth.obj (vertex normals via vn + smoothing groups).
 *   • Two reference spheres (their normals are always smooth – computed analytically).
 *   • Three lights: one directional (key), one point (fill), one spot (rim).
 */
public class App {
    public static void main(String[] args) {
        int width  = 800;
        int height = 600;

        // ── Camera ────────────────────────────────────────────────────────────
        Camera camera = new Camera(
            new Vector3D(2.5, 2.5, -5),  // position
            new Vector3D(0,   0.5,  0),  // look-at
            new Vector3D(0,   1,    0),  // up
            60.0,
            width, height,
            0.1, 100.0
        );

        // ── Scene ─────────────────────────────────────────────────────────────
        Scene scene = new Scene(new Color(10, 10, 20));

        // ── Lights ────────────────────────────────────────────────────────────
        // Key light – directional (sun-like), warm white, from upper-left
        scene.addLight(new DirectionalLight(
            new Vector3D(-1, -2, 1),      // direction light travels
            new Color(255, 245, 220),     // warm white
            1.2                           // intensity
        ));

        // Fill light – point light, cool blue, from the right
        scene.addLight(new PointLight(
            new Vector3D(4, 3, -2),       // position
            new Color(180, 200, 255),     // cool blue
            0.6
        ));

        // Rim light – spot from behind, to show silhouette
        scene.addLight(new SpotLight(
            new Vector3D(-2, 4, 3),       // position
            new Vector3D(1, -1, -1),      // aim direction
            20.0, 35.0,                   // inner / outer cone degrees
            Color.WHITE,
            1.0
        ));

        // ── OBJ model ─────────────────────────────────────────────────────────
        String objPath = "models/cube_smooth.obj";
        try {
            System.out.println("[App] Loading OBJ: " + objPath);
            ObjModel model = ObjReader.parse(objPath);
            model.printSummary();

            // Gold cube – centered at origin
            model.addToScene(scene, new Color(220, 160, 40));

            // Cyan cube – offset, smaller, also phong-shaded
            model.addToScene(scene, new Color(60, 200, 210),
                             new Vector3D(1.4, 0, 1.2), 0.55);

        } catch (Exception e) {
            System.err.println("[App] OBJ load failed: " + e.getMessage());
            System.err.println("[App] Continuing without OBJ.");
        }

        // ── Reference spheres ─────────────────────────────────────────────────
        // Red sphere – on the left (normals always smooth on spheres)
        scene.addObject(new Sphere(new Vector3D(-1.0, 1.2, 0.3), 0.35, Color.RED));

        // Blue sphere – upper right
        scene.addObject(new Sphere(new Vector3D( 0.8, 1.8, 0.4), 0.22,
                                   new Color(60, 80, 220)));

        // Small yellow sphere
        scene.addObject(new Sphere(new Vector3D( 0.05, 1.1, -0.3), 0.14,
                                   new Color(255, 220, 50)));

        // ── Render ────────────────────────────────────────────────────────────
        Raytracer raytracer = new Raytracer(scene, camera);
        BufferedImage image = raytracer.render();

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
        JFrame frame = new JFrame("Raytracer v0.5 – Phong Shading");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
