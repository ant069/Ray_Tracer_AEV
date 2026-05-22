public class Ray {
    public final Vector3D origin;
    public final Vector3D direction;

    public Ray(Vector3D origin, Vector3D direction) {
        this.origin = origin;
        this.direction = direction.normalize();
    }

    public Vector3D pointAt(double t) {
        return origin.add(direction.multiply(t));
    }
}