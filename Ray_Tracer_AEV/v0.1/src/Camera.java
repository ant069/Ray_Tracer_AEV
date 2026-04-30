public class Camera {
    private final Vector3D position;
    private final Vector3D forward;
    private final Vector3D right;
    private final Vector3D up;
    private final double fovRadians;
    private final double orthoScale;
    private final boolean orthographic;
    private final double aspectRatio;
    private final int width;
    private final int height;

    public Camera(Vector3D position, Vector3D target, Vector3D upVector, double fovDegrees, int width, int height) {
        this.position = position;
        this.fovRadians = Math.toRadians(fovDegrees);
        this.orthoScale = 0.0;
        this.orthographic = false;
        this.width = width;
        this.height = height;
        this.aspectRatio = (double) width / height;

        this.forward = target.subtract(position).normalize();
        this.right = upVector.cross(forward).normalize();
        this.up = this.forward.cross(this.right).normalize();
    }

    public Camera(Vector3D position, Vector3D target, Vector3D upVector, int width, int height, double orthoScale) {
        this.position = position;
        this.fovRadians = 0.0;
        this.orthoScale = orthoScale;
        this.orthographic = true;
        this.width = width;
        this.height = height;
        this.aspectRatio = (double) width / height;

        this.forward = target.subtract(position).normalize();
        this.right = upVector.cross(forward).normalize();
        this.up = this.forward.cross(this.right).normalize();
    }

    public Ray getRay(double pixelX, double pixelY) {
        double ndcX = (pixelX + 0.5) / width;
        double ndcY = (pixelY + 0.5) / height;

        if (orthographic) {
            double screenX = (2.0 * ndcX - 1.0) * aspectRatio * orthoScale;
            double screenY = (1.0 - 2.0 * ndcY) * orthoScale;
            Vector3D origin = position.add(right.multiply(screenX)).add(up.multiply(screenY));
            return new Ray(origin, forward);
        }

        double screenX = (2.0 * ndcX - 1.0) * aspectRatio * Math.tan(fovRadians / 2.0);
        double screenY = (1.0 - 2.0 * ndcY) * Math.tan(fovRadians / 2.0);

        Vector3D direction = forward.add(right.multiply(screenX)).add(up.multiply(screenY)).normalize();
        return new Ray(position, direction);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
