import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Scene v0.6
 *
 * Stores scene objects and a list of lights (any mix of
 * DirectionalLight and PointLight).
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
}
