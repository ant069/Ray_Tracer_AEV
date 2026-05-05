import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Scene v0.4 – supports a list of Light objects instead of a single light position.
 *
 * Changes from v0.3:
 *   - Replaced single lightPosition with a List&lt;Light&gt;.
 *   - addLight() method to register lights.
 *   - getLights() for the Raytracer to iterate over.
 */
public class Scene {
    private final List<Object3D> objects;
    private final List<Light>    lights;
    private final Color          backgroundColor;

    public Scene(Color backgroundColor) {
        this.objects         = new ArrayList<>();
        this.lights          = new ArrayList<>();
        this.backgroundColor = backgroundColor;
    }

    public void addObject(Object3D object) { objects.add(object); }
    public void addLight(Light light)      { lights.add(light);   }

    public Color       getBackgroundColor() { return backgroundColor; }
    public List<Light> getLights()          { return lights;          }

    /**
     * Closest intersection in [tNear, tFar].
     */
    public Intersection intersect(Ray ray, double tNear, double tFar) {
        Intersection closest = Intersection.miss();
        for (Object3D object : objects) {
            Intersection hit = object.intersect(ray);
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
}