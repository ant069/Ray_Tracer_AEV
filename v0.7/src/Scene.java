import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Scene v0.7
 *
 * New in v0.7:
 *   isInShadow() – fires a shadow ray from the hit point toward a light
 *   and checks for occluding geometry (Session 24, slides 7-13).
 *
 * Shadow algorithm (slides 8-13):
 *   1. Camera ray hits a surface at point P.
 *   2. Cast a shadow ray from P in direction L (toward the light).
 *   3. Offset the origin by a small epsilon along the normal to avoid
 *      self-intersection ("do not collide with yourself", slide 13).
 *   4. If the shadow ray hits any object before reaching the light → P is in shadow.
 *   5. For PointLight the maximum travel distance is the light-to-point distance.
 *      For DirectionalLight the light is at infinity so maxDist = +∞.
 */
public class Scene {
    private final List<Object3D> objects;
    private final List<Light>    lights;
    private final Color          backgroundColor;

    /** Small offset to prevent shadow rays from hitting their own surface. */
    private static final double SHADOW_BIAS = 1e-4;

    public Scene(Color backgroundColor) {
        this.objects         = new ArrayList<>();
        this.lights          = new ArrayList<>();
        this.backgroundColor = backgroundColor;
    }

    public void addObject(Object3D object) { objects.add(object); }
    public void addLight(Light light)      { lights.add(light);   }

    public Color       getBackgroundColor() { return backgroundColor; }
    public List<Light> getLights()          { return lights;          }

    /** Closest intersection within [tNear, tFar]. */
    public Intersection intersect(Ray ray, double tNear, double tFar) {
        Intersection closest = Intersection.miss();
        for (Object3D obj : objects) {
            Intersection hit = obj.intersect(ray);
            if (hit.hit && hit.distance >= tNear && hit.distance <= tFar
                        && hit.distance < closest.distance) {
                closest = hit;
            }
        }
        return closest;
    }

    public Intersection intersect(Ray ray) {
        return intersect(ray, 0.0, Double.POSITIVE_INFINITY);
    }

    /**
     * Tests whether hitPoint is in shadow relative to the given light.
     *
     * Steps (slides 10-13):
     *   - Compute L = direction from hitPoint toward the light.
     *   - Offset origin by SHADOW_BIAS along the surface normal to avoid
     *     self-intersection (slide 13: "do not collide with yourself").
     *   - For PointLight: maxDist = distance to the light (stop before reaching it).
     *   - For DirectionalLight: maxDist = infinity (light is infinitely far).
     *   - Fire the shadow ray; if any object is hit before maxDist → in shadow.
     *
     * @param hitPoint  surface point to test
     * @param normal    surface normal at hitPoint (used for bias offset)
     * @param light     the light to test visibility toward
     * @return          true if hitPoint is occluded from the light
     */
    public boolean isInShadow(Vector3D hitPoint, Vector3D normal, Light light) {
        // L – direction toward the light
        Vector3D L = light.getLightDirection(hitPoint);
        if (L == null) return true; // outside spot cone → treated as shadowed

        // Offset origin along the normal to avoid self-intersection (slide 13)
        Vector3D shadowOrigin = hitPoint.add(normal.multiply(SHADOW_BIAS));
        Ray shadowRay = new Ray(shadowOrigin, L);

        // Maximum distance the shadow ray should travel
        double maxDist;
        if (light instanceof PointLight) {
            // Stop just before the light position (slide 8: "point light ray")
            maxDist = ((PointLight) light).getDistance(hitPoint) - SHADOW_BIAS;
        } else {
            // Directional light is at infinity
            maxDist = Double.POSITIVE_INFINITY;
        }

        // Check for occluders (slides 11-12: "obvious collisions" / "are there collisions?")
        for (Object3D obj : objects) {
            Intersection hit = obj.intersect(shadowRay);
            if (hit.hit && hit.distance > SHADOW_BIAS && hit.distance < maxDist) {
                return true; // occluded → in shadow
            }
        }
        return false; // no occluder → lit
    }
}
