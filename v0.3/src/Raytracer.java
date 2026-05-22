import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Raytracer v0.2
 *
 * New features vs v0.1:
 *   - Near & Far clipping: every intersection is tested against [tNear, tFar].
 *   - Frustum clipping:    getRay() returns null for out-of-frustum pixels;
 *                          those pixels receive the background colour directly,
 *                          avoiding all intersection math (slide 3: "Avoid extra operations").
 *   - Triangle support:    Scene can contain Triangle objects rendered via
 *                          Möller-Trumbore intersection.
 */
public class Raytracer {
    private final Scene  scene;
    private final Camera camera;

    public Raytracer(Scene scene, Camera camera) {
        this.scene  = scene;
        this.camera = camera;
    }

    public BufferedImage render() {
        int width  = camera.getWidth();
        int height = camera.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        double tNear = camera.getTNear();
        double tFar  = camera.getTFar();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                // Frustum clipping: getRay returns null for out-of-frustum pixels.
                // Avoids all intersection work for those pixels (slide 3).
                Ray ray = camera.getRay(x, y);
                if (ray == null) {
                    image.setRGB(x, y, scene.getBackgroundColor().getRGB());
                    continue;
                }

                Color color = traceRay(ray, tNear, tFar);
                image.setRGB(x, y, color.getRGB());
            }
        }
        return image;
    }

    private Color traceRay(Ray ray, double tNear, double tFar) {
        // Near & Far clipping applied inside Scene.intersect
        Intersection hit = scene.intersect(ray, tNear, tFar);
        if (!hit.hit) return scene.getBackgroundColor();
        return hit.object.getColor();
    }
}