import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Raytracer v0.6
 *
 * BUG FIX vs v0.4/v0.5:
 *   traceRay() now calls shader.shade(hit, lights) instead of returning
 *   hit.object.getColor() directly. That was the root cause of lights having
 *   no visible effect — the FlatShader existed but was never invoked.
 *
 * New in v0.6:
 *   PointLight support (Session 23 slide 5). The shader handles both
 *   DirectionalLight and PointLight polymorphically through Light.getLightDirection().
 */
public class Raytracer {
    private final Scene      scene;
    private final Camera     camera;
    private final FlatShader shader;

    public Raytracer(Scene scene, Camera camera) {
        this.scene  = scene;
        this.camera = camera;
        this.shader = new FlatShader(0.05); // small ambient so unlit faces are visible
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

        // ── THE FIX ──────────────────────────────────────────────────────────
        // Previously this line was:  return hit.object.getColor();
        // That bypassed the shader completely, making all lights invisible.
        // Now we pass the intersection to FlatShader which applies:
        //   Diffuse = LC × OC × LI × (N · L)   for every light in the scene.
        return shader.shade(hit, scene.getLights());
    }
}
