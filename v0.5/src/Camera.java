/**
 * Camera v0.2 – adds Near & Far clipping planes and frustum clipping.
 *
 * Clipping rules (slide 3):
 *   Near & Far  → rays with t outside [tNear, tFar] are discarded.
 *   Frustum     → rays whose screen-space coords exceed [-1,1] are clipped.
 *   Avoid extra operations → clipping checks are done BEFORE expensive
 *                            intersection math by letting Scene/Raytracer
 *                            test t against [tNear, tFar].
 */
public class Camera {

    // ── geometry ─────────────────────────────────────────────────────────────
    private final Vector3D position;
    private final Vector3D forward;
    private final Vector3D right;
    private final Vector3D up;

    // ── projection ───────────────────────────────────────────────────────────
    private final double fovRadians;
    private final double orthoScale;
    private final boolean orthographic;
    private final double aspectRatio;
    private final int width;
    private final int height;

    // ── clipping planes (NEW in v0.2) ─────────────────────────────────────────
    private final double tNear;   // near clipping distance
    private final double tFar;    // far  clipping distance

    // ── default clip values ───────────────────────────────────────────────────
    private static final double DEFAULT_NEAR = 0.001;
    private static final double DEFAULT_FAR  = 1000.0;

    // ── perspective constructors ──────────────────────────────────────────────

    /** Perspective camera with explicit clipping planes. */
    public Camera(Vector3D position, Vector3D target, Vector3D upVector,
                  double fovDegrees, int width, int height,
                  double tNear, double tFar) {
        this.position      = position;
        this.fovRadians    = Math.toRadians(fovDegrees);
        this.orthoScale    = 0.0;
        this.orthographic  = false;
        this.width         = width;
        this.height        = height;
        this.aspectRatio   = (double) width / height;
        this.tNear         = tNear;
        this.tFar          = tFar;

        this.forward = target.subtract(position).normalize();
        this.right   = upVector.cross(forward).normalize();
        this.up      = this.forward.cross(this.right).normalize();
    }

    /** Perspective camera with default clipping planes. */
    public Camera(Vector3D position, Vector3D target, Vector3D upVector,
                  double fovDegrees, int width, int height) {
        this(position, target, upVector, fovDegrees, width, height,
             DEFAULT_NEAR, DEFAULT_FAR);
    }

    // ── orthographic constructors ─────────────────────────────────────────────

    /** Orthographic camera with explicit clipping planes. */
    public Camera(Vector3D position, Vector3D target, Vector3D upVector,
                  int width, int height, double orthoScale,
                  double tNear, double tFar) {
        this.position     = position;
        this.fovRadians   = 0.0;
        this.orthoScale   = orthoScale;
        this.orthographic = true;
        this.width        = width;
        this.height       = height;
        this.aspectRatio  = (double) width / height;
        this.tNear        = tNear;
        this.tFar         = tFar;

        this.forward = target.subtract(position).normalize();
        this.right   = upVector.cross(forward).normalize();
        this.up      = this.forward.cross(this.right).normalize();
    }

    /** Orthographic camera with default clipping planes (keeps v0.1 signature). */
    public Camera(Vector3D position, Vector3D target, Vector3D upVector,
                  int width, int height, double orthoScale) {
        this(position, target, upVector, width, height, orthoScale,
             DEFAULT_NEAR, DEFAULT_FAR);
    }

    // ── ray generation ────────────────────────────────────────────────────────

    /**
     * Returns a Ray for pixel (pixelX, pixelY), or null if the pixel lies
     * outside the view frustum (frustum clipping).
     *
     * Frustum clipping: screen-space x and y must be in [-1,1].
     * Rays that pass the frustum test carry the camera's [tNear, tFar] range
     * so the Raytracer can skip intersections outside that range.
     */
    public Ray getRay(double pixelX, double pixelY) {
        // Convert pixel coordinates to normalized device coordinates (NDC)
        double ndcX = (2.0 * pixelX / width) - 1.0;
        double ndcY = 1.0 - (2.0 * pixelY / height);

        // Frustum clipping: discard pixels outside [-1,1] in NDC
        if (ndcX < -1.0 || ndcX > 1.0 || ndcY < -1.0 || ndcY > 1.0) {
            return null;  // out of frustum
        }

        if (orthographic) {
            // Orthographic projection
            double worldX = ndcX * orthoScale * aspectRatio;
            double worldY = ndcY * orthoScale;
            double worldZ = 0.0;

            Vector3D worldPoint = position.add(
                right.multiply(worldX).add(up.multiply(worldY)).add(forward.multiply(worldZ))
            );
            return new Ray(worldPoint, forward);
        } else {
            // Perspective projection
            double tanHalfFov = Math.tan(fovRadians / 2.0);
            double worldX = ndcX * tanHalfFov * aspectRatio;
            double worldY = ndcY * tanHalfFov;
            double worldZ = 1.0;

            Vector3D rayDirection = right.multiply(worldX)
                                .add(up.multiply(worldY))
                                .add(forward.multiply(worldZ))
                                .normalize();
            return new Ray(position, rayDirection);
        }
    }

    // ── clipping accessors (used by Raytracer) ────────────────────────────────

    /** Near clipping distance. Intersections with t < tNear are discarded. */
    public double getTNear() { return tNear; }

    /** Far clipping distance. Intersections with t > tFar are discarded. */
    public double getTFar()  { return tFar;  }

    public int     getWidth()  { return width;  }
    public int     getHeight() { return height; }
    public Vector3D getPosition() { return position; }
}