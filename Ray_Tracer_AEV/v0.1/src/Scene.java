import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Scene {
    private final List<Object3D> objects;
    private final Vector3D lightPosition;
    private final Color backgroundColor;

    public Scene(Color backgroundColor, Vector3D lightPosition) {
        this.objects = new ArrayList<>();
        this.backgroundColor = backgroundColor;
        this.lightPosition = lightPosition;
    }

    public void addObject(Object3D object) {
        objects.add(object);
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Vector3D getLightPosition() {
        return lightPosition;
    }

    public Intersection intersect(Ray ray) {
        Intersection closest = Intersection.miss();
        for (Object3D object : objects) {
            Intersection hit = object.intersect(ray);
            if (hit.hit && hit.distance < closest.distance) {
                closest = hit;
            }
        }
        return closest;
    }

    public boolean hasLineOfSight(Vector3D point, Vector3D lightDirection, double maxDistance) {
        Ray shadowRay = new Ray(point.add(lightDirection.multiply(1e-6)), lightDirection);
        for (Object3D object : objects) {
            Intersection hit = object.intersect(shadowRay);
            if (hit.hit && hit.distance < maxDistance) {
                return false;
            }
        }
        return true;
    }
}
