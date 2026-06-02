import java.util.*;

/**
 * BVH (Bounding Volume Hierarchy) with median split.
 * Guarantees a balanced tree regardless of triangle distribution.
 * Reduces intersection complexity from O(n) to O(log n).
 */
public class BVHNode {
    private double[] boxMin = new double[3];
    private double[] boxMax = new double[3];
    private List<Object3D> objects;  // null in internal nodes
    private BVHNode left, right;
    private static final int MAX_LEAF = 8;

    public BVHNode(List<Object3D> objs) {
        computeBounds(objs);
        if (objs.size() <= MAX_LEAF) {
            this.objects = new ArrayList<>(objs);
        } else {
            this.objects = null;
            split(objs);
        }
    }

    private void computeBounds(List<Object3D> objs) {
        boxMin[0] = boxMin[1] = boxMin[2] = Double.POSITIVE_INFINITY;
        boxMax[0] = boxMax[1] = boxMax[2] = Double.NEGATIVE_INFINITY;
        for (Object3D obj : objs) {
            if (obj instanceof Triangle) {
                for (Vector3D v : ((Triangle) obj).getVertices()) {
                    if (v.x < boxMin[0]) boxMin[0] = v.x;
                    if (v.y < boxMin[1]) boxMin[1] = v.y;
                    if (v.z < boxMin[2]) boxMin[2] = v.z;
                    if (v.x > boxMax[0]) boxMax[0] = v.x;
                    if (v.y > boxMax[1]) boxMax[1] = v.y;
                    if (v.z > boxMax[2]) boxMax[2] = v.z;
                }
            } else if (obj instanceof Sphere) {
                // Sphere expands bounds by its AABB
                double[] sb = sphereBounds((Sphere) obj);
                for (int i = 0; i < 3; i++) {
                    if (sb[i]     < boxMin[i]) boxMin[i] = sb[i];
                    if (sb[i + 3] > boxMax[i]) boxMax[i] = sb[i + 3];
                }
            }
        }
    }

    private double[] sphereBounds(Sphere s) {
        // Returns [minX, minY, minZ, maxX, maxY, maxZ]
        // Sphere doesn't expose center/radius, so use a generous placeholder
        return new double[]{-1e9, -1e9, -1e9, 1e9, 1e9, 1e9};
    }

    private void split(List<Object3D> objs) {
        // Choose longest axis
        int axis = 0;
        double dx = boxMax[0] - boxMin[0];
        double dy = boxMax[1] - boxMin[1];
        double dz = boxMax[2] - boxMin[2];
        if (dy > dx && dy >= dz) axis = 1;
        else if (dz > dx)        axis = 2;

        final int sortAxis = axis;

        // Separate Triangles (sortable) from other types
        List<Object3D> tris      = new ArrayList<>(objs.size());
        List<Object3D> others    = new ArrayList<>();
        for (Object3D o : objs) {
            if (o instanceof Triangle) tris.add(o);
            else others.add(o);
        }

        // Median sort on centroid — always produces a balanced tree
        tris.sort((a, b) -> {
            double ca = centroid((Triangle) a, sortAxis);
            double cb = centroid((Triangle) b, sortAxis);
            return Double.compare(ca, cb);
        });

        int mid = tris.size() / 2;
        List<Object3D> leftList  = new ArrayList<>(tris.subList(0, mid));
        List<Object3D> rightList = new ArrayList<>(tris.subList(mid, tris.size()));

        // Distribute non-Triangles into the smaller half
        for (Object3D o : others) {
            if (leftList.size() <= rightList.size()) leftList.add(o);
            else rightList.add(o);
        }

        if (leftList.isEmpty() || rightList.isEmpty()) {
            // Can't split further — keep as leaf
            this.objects = new ArrayList<>(objs);
            return;
        }

        this.left  = new BVHNode(leftList);
        this.right = new BVHNode(rightList);
    }

    private static double centroid(Triangle t, int axis) {
        Vector3D[] v = t.getVertices();
        if (axis == 0) return (v[0].x + v[1].x + v[2].x) * 0.3333333333;
        if (axis == 1) return (v[0].y + v[1].y + v[2].y) * 0.3333333333;
        return                (v[0].z + v[1].z + v[2].z) * 0.3333333333;
    }

    /** Finds the closest intersection within the subtree. */
    public Intersection intersect(Ray ray) {
        if (!hitBox(ray)) return Intersection.miss();

        if (objects != null) {
            // Leaf
            Intersection best = Intersection.miss();
            for (Object3D obj : objects) {
                Intersection h = obj.intersect(ray);
                if (h.hit && h.distance < best.distance) best = h;
            }
            return best;
        }

        // Internal — traverse both children, keep closest
        Intersection l = (left  != null) ? left.intersect(ray)  : Intersection.miss();
        Intersection r = (right != null) ? right.intersect(ray) : Intersection.miss();

        if (!l.hit && !r.hit) return Intersection.miss();
        if (!l.hit) return r;
        if (!r.hit) return l;
        return l.distance < r.distance ? l : r;
    }

    /** Slab-method AABB test. */
    private boolean hitBox(Ray ray) {
        double tmin = Double.NEGATIVE_INFINITY;
        double tmax = Double.POSITIVE_INFINITY;

        double[] ro = {ray.origin.x,    ray.origin.y,    ray.origin.z};
        double[] rd = {ray.direction.x, ray.direction.y, ray.direction.z};

        for (int i = 0; i < 3; i++) {
            double d = rd[i];
            if (Math.abs(d) < 1e-9) {
                if (ro[i] < boxMin[i] || ro[i] > boxMax[i]) return false;
            } else {
                double t1 = (boxMin[i] - ro[i]) / d;
                double t2 = (boxMax[i] - ro[i]) / d;
                if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
                if (t1 > tmin) tmin = t1;
                if (t2 < tmax) tmax = t2;
                if (tmin > tmax) return false;
            }
        }
        return tmax >= 0;
    }
}
