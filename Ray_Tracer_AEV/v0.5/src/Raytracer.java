import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Raytracer v0.5
 *
 * Change from v0.4:
 *   Uses PhongShader to shade each hit point. The interpolated normal
 *   is already embedded in the Intersection record by Triangle.intersect(),
 *   so the Raytracer simply passes the hit to the shader – no extra work here.
 *
 *   The shader computes:
 *     Diffuse = LC × OC × LI × (N · L)
 *   where N is now the smooth interpolated normal (Phong, slide 8).
 */
public class Raytracer {
    private final Scene       scene;
    private final Camera      camera;
    private final PhongShader shader;

    public Raytracer(Scene scene, Camera camera) {
        this.scene  = scene;
        this.camera = camera;
        this.shader = new PhongShader(0.08); // ambient term
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
                Color color = traceRay(ray, tNear, tFar);
                image.setRGB(x, y, color.getRGB());
            }
        }
        return image;
    }

    private Color traceRay(Ray ray, double tNear, double tFar) {
        Intersection hit = scene.intersect(ray, tNear, tFar);
        if (!hit.hit) return scene.getBackgroundColor();

        // Phong shading: uses the interpolated normal stored in hit.normal
        return shader.shade(hit, scene.getLights());
    }
}
