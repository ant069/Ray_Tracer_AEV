import java.awt.Color;
import java.util.*;

/** Scene: holds 3D objects and lights. Supports shadow rays with BVH acceleration. */
public class Scene {
    private final List<Object3D> objects = new ArrayList<>();
    private final List<Light>    lights  = new ArrayList<>();
    private final Color          backgroundColor;
    private BVHNode              bvh     = null;
    private boolean              needsRebuildBVH = true;

    private static final double SHADOW_BIAS = 1e-4;

    public Scene(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void addObject(Object3D o) {
        objects.add(o);
        needsRebuildBVH = true;
    }

    public void addLight(Light l) { lights.add(l); }

    public Color       getBackgroundColor() { return backgroundColor; }
    public List<Light> getLights()          { return lights;          }

    private void buildBVH() {
        if (!needsRebuildBVH || bvh != null) return;
        if (objects.isEmpty()) return;

        System.out.println("[Scene] Building BVH for " + objects.size() + " objects...");
        bvh = new BVHNode(objects);
        needsRebuildBVH = false;
    }

    /** Closest intersection in [tNear, tFar]. */
    public Intersection intersect(Ray ray, double tNear, double tFar) {
        buildBVH();

        Intersection closest = Intersection.miss();

        if (bvh != null) {
            Intersection hit = bvh.intersect(ray);
            if (hit.hit && hit.distance >= tNear && hit.distance <= tFar) {
                closest = hit;
            }
        } else {
            for (Object3D obj : objects) {
                Intersection hit = obj.intersect(ray);
                if (hit.hit && hit.distance >= tNear && hit.distance <= tFar
                            && hit.distance < closest.distance) {
                    closest = hit;
                }
            }
        }
        return closest;
    }

    public Intersection intersect(Ray ray) {
        return intersect(ray, 0.0, Double.POSITIVE_INFINITY);
    }

    /**
     * Returns true if hitPoint is in shadow with respect to the given light.
     * Offset by SHADOW_BIAS along the normal to prevent self-intersection.
     */
    public boolean isInShadow(Vector3D hitPoint, Vector3D normal, Light light) {
        Vector3D L = light.getLightDirection(hitPoint);
        if (L == null) return true;

        // Ensure the bias goes in the correct direction (toward the light)
        double sign = normal.dot(L) >= 0 ? 1.0 : -1.0;
        Vector3D origin = hitPoint.add(normal.multiply(sign * SHADOW_BIAS));
        Ray shadowRay = new Ray(origin, L);

        double maxDist = (light instanceof PointLight)
            ? ((PointLight) light).getDistance(hitPoint) - SHADOW_BIAS
            : Double.POSITIVE_INFINITY;

        Intersection hit = intersect(shadowRay);
        return hit.hit && hit.distance > SHADOW_BIAS && hit.distance < maxDist
               && hit.object.getMaterial().getRefractivity() < 0.1;
    }
}
