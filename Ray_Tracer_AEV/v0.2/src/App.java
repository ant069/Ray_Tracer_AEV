import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * App – Raytracer v0.2 demo
 *
 * Demonstrates both new features from Session 19:
 *   1. Clipping  – camera is constructed with explicit tNear / tFar values.
 *   2. Triangles – two triangles form a coloured quad in the scene.
 */
public class App {
    public static void main(String[] args) {
        int width  = 800;
        int height = 600;

        // ── Camera (perspective) with explicit near/far clipping ──────────────
        // tNear = 0.1  → nothing closer than 0.1 units is rendered
        // tFar  = 50.0 → nothing farther than 50 units is rendered
        Camera camera = new Camera(
            new Vector3D(0, 0, -10),   // position
            new Vector3D(0, 0,   0),   // target
            new Vector3D(0, 1,   0),   // up
            60.0,                      // fov degrees
            width, height,
            0.1, 50.0                  // tNear, tFar  ← NEW in v0.2
        );

        Scene scene = new Scene(new Color(15, 15, 30), new Vector3D(5, 10, -5));

        // ── Spheres (from v0.1) ───────────────────────────────────────────────
        scene.addObject(new Sphere(new Vector3D(-1.5, 1.0, 0.0), 0.6, Color.RED));
        scene.addObject(new Sphere(new Vector3D( 1.5, 1.0, 0.0), 0.4, Color.BLUE));

        // ── Triangles (NEW in v0.2) ───────────────────────────────────────────
        // Two triangles forming a flat quad (floor-like surface)
        //      v0 ──────── v2
        //       |  \       |
        //       |    \     |
        //      v1 ────── v3
        Vector3D v0 = new Vector3D(-2.0, -0.5,  1.0);
        Vector3D v1 = new Vector3D(-2.0, -0.5, -1.0);
        Vector3D v2 = new Vector3D( 2.0, -0.5,  1.0);
        Vector3D v3 = new Vector3D( 2.0, -0.5, -1.0);

        scene.addObject(new Triangle(v0, v1, v2, new Color(0, 180, 120)));   // teal
        scene.addObject(new Triangle(v1, v3, v2, new Color(0, 140,  90)));   // darker teal

        // A single decorative triangle above the spheres
        Vector3D ta = new Vector3D( 0.0,  2.5, -0.5);
        Vector3D tb = new Vector3D(-0.8,  1.0, -0.5);
        Vector3D tc = new Vector3D( 0.8,  1.0, -0.5);
        scene.addObject(new Triangle(ta, tb, tc, new Color(255, 200, 0)));   // gold

        // ── Render ────────────────────────────────────────────────────────────
        Raytracer raytracer = new Raytracer(scene, camera);
        BufferedImage image = raytracer.render();

        // Save
        try {
            File output = new File("render.png");
            ImageIO.write(image, "png", output);
            System.out.println("Render complete: " + output.getAbsolutePath());
        } catch (Exception ex) {
            System.err.println("Error saving image: " + ex.getMessage());
        }

        showImage(image);
    }

    private static void showImage(BufferedImage image) {
        JFrame frame = new JFrame("Raytracer v0.2");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}