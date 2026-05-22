import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Raytracer v0.7
 *
 * Change vs v0.6:
 *   shader.shade() now receives the Scene reference so FlatShader can
 *   cast shadow rays via Scene.isInShadow() for every light.
 */
public class Raytracer {
    private final Scene      scene;
    private final Camera     camera;
    private final FlatShader shader;

    public Raytracer(Scene scene, Camera camera) {
        this.scene  = scene;
        this.camera = camera;
        this.shader = new FlatShader(0.05);
    }

    public BufferedImage render() {
        int width  = camera.getWidth();
        int height = camera.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        double tNear = camera.getTNear();
        double tFar  = camera.getTFar();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Ray ray = camera.getRay(x, y);
                if (ray == null) {
                    image.setRGB(x, y, scene.getBackgroundColor().getRGB());
                    continue;
                }
                image.setRGB(x, y, traceRay(ray, tNear, tFar).getRGB());
            }
        }
        return image;
    }

    private Color traceRay(Ray ray, double tNear, double tFar) {
        Intersection hit = scene.intersect(ray, tNear, tFar);
        if (!hit.hit) return scene.getBackgroundColor();

        // Pass scene so FlatShader can cast shadow rays and apply falloff
        return shader.shade(hit, scene.getLights(), scene);
    }
}
