import java.awt.Color;
import java.awt.image.BufferedImage;

public class Raytracer {
    private final Scene scene;
    private final Camera camera;

    public Raytracer(Scene scene, Camera camera) {
        this.scene = scene;
        this.camera = camera;
    }

    public BufferedImage render() {
        int width = camera.getWidth();
        int height = camera.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Ray ray = camera.getRay(x, y);
                Color color = traceRay(ray, 0);
                image.setRGB(x, y, color.getRGB());
            }
        }
        return image;
    }

    private Color traceRay(Ray ray, int depth) {
        Intersection hit = scene.intersect(ray);
        if (!hit.hit) {
            return scene.getBackgroundColor();
        }
        return hit.object.getColor();
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
