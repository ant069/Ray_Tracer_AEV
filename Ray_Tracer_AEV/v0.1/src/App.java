import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class App {
    public static void main(String[] args) {
        int width = 600;
        int height = 600;

        Camera camera = new Camera(
            new Vector3D(0, 0, -10),
            new Vector3D(0, 0, 0),
            new Vector3D(0, 1, 0),
            width,
            height,
            0.7
        );

        Scene scene = new Scene(Color.WHITE, new Vector3D(0, 0, 0));
        scene.addObject(new Sphere(new Vector3D(-0.05, 0.38, 0.0), 0.10, Color.RED));
        scene.addObject(new Sphere(new Vector3D(0.35, 0.38, 0.0), 0.06, Color.BLUE));

        Raytracer raytracer = new Raytracer(scene, camera);
        BufferedImage image = raytracer.render();

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
        JFrame frame = new JFrame("Raytracer v0.1");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
