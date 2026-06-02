/** Perspective camera with near/far clipping planes. */
public class Camera {
    private final Vector3D position, forward, right, up;
    private final double   fovRadians, aspectRatio;
    private final int      width, height;
    private final double   tNear, tFar;

    private static final double DEFAULT_NEAR = 0.001;
    private static final double DEFAULT_FAR  = 1000.0;

    public Camera(Vector3D position, Vector3D target, Vector3D upVector,
                  double fovDegrees, int width, int height,
                  double tNear, double tFar) {
        this.position   = position;
        this.fovRadians = Math.toRadians(fovDegrees);
        this.width      = width;
        this.height     = height;
        this.aspectRatio = (double) width / height;
        this.tNear      = tNear;
        this.tFar       = tFar;

        this.forward = target.subtract(position).normalize();
        this.right   = upVector.cross(forward).normalize();
        this.up      = this.forward.cross(this.right).normalize();
    }

    public Camera(Vector3D position, Vector3D target, Vector3D upVector,
                  double fovDegrees, int width, int height) {
        this(position, target, upVector, fovDegrees, width, height,
             DEFAULT_NEAR, DEFAULT_FAR);
    }

    public Ray getRay(double px, double py) {
        double ndcX = (2.0 * px / width)  - 1.0;
        double ndcY = 1.0 - (2.0 * py / height);

        double tanH = Math.tan(fovRadians / 2.0);
        double wx   = ndcX * tanH * aspectRatio;
        double wy   = ndcY * tanH;

        Vector3D dir = forward
            .add(right.multiply(wx))
            .add(up.multiply(wy))
            .normalize();
        return new Ray(position, dir);
    }

    public int      getWidth()    { return width;    }
    public int      getHeight()   { return height;   }
    public double   getTNear()    { return tNear;    }
    public double   getTFar()     { return tFar;     }
    public Vector3D getPosition() { return position; }
}
