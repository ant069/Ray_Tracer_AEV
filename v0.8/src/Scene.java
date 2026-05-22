import java.awt.Color;
import java.util.*;

/** Escena: contiene objetos 3D y luces. Soporta shadow rays. */
public class Scene {
    private final List<Object3D> objects = new ArrayList<>();
    private final List<Light>    lights  = new ArrayList<>();
    private final Color          backgroundColor;

    private static final double SHADOW_BIAS = 1e-4;

    public Scene(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void addObject(Object3D o) { objects.add(o); }
    public void addLight(Light l)     { lights.add(l);  }

    public Color       getBackgroundColor() { return backgroundColor; }
    public List<Light> getLights()          { return lights;          }

    /** Intersección más cercana en [tNear, tFar]. */
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
     * Devuelve true si hitPoint está en sombra respecto a la luz dada.
     * Offset por SHADOW_BIAS a lo largo de la normal para evitar auto-intersección.
     */
    public boolean isInShadow(Vector3D hitPoint, Vector3D normal, Light light) {
        Vector3D L = light.getLightDirection(hitPoint);
        if (L == null) return true;

        // Asegurar que el bias va en la dirección correcta (hacia la luz)
        double sign = normal.dot(L) >= 0 ? 1.0 : -1.0;
        Vector3D origin = hitPoint.add(normal.multiply(sign * SHADOW_BIAS));
        Ray shadowRay = new Ray(origin, L);

        double maxDist = (light instanceof PointLight)
            ? ((PointLight) light).getDistance(hitPoint) - SHADOW_BIAS
            : Double.POSITIVE_INFINITY;

        for (Object3D obj : objects) {
            Intersection hit = obj.intersect(shadowRay);
            if (hit.hit && hit.distance > SHADOW_BIAS && hit.distance < maxDist) {
                return true;
            }
        }
        return false;
    }
}
