public class Vector3D {
    public final double x, y, z;

    public Vector3D(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
    }

    public Vector3D add(Vector3D o)      { return new Vector3D(x+o.x, y+o.y, z+o.z); }
    public Vector3D subtract(Vector3D o) { return new Vector3D(x-o.x, y-o.y, z-o.z); }
    public Vector3D multiply(double s)   { return new Vector3D(x*s, y*s, z*s); }
    public Vector3D divide(double s)     { return new Vector3D(x/s, y/s, z/s); }
    public Vector3D negate()             { return new Vector3D(-x, -y, -z); }

    public double dot(Vector3D o)  { return x*o.x + y*o.y + z*o.z; }
    public double length()         { return Math.sqrt(dot(this)); }
    public double distanceTo(Vector3D o) { return subtract(o).length(); }

    public Vector3D cross(Vector3D o) {
        return new Vector3D(
            y*o.z - z*o.y,
            z*o.x - x*o.z,
            x*o.y - y*o.x
        );
    }

    public Vector3D normalize() {
        double len = length();
        return len < 1e-12 ? new Vector3D(0,1,0) : divide(len);
    }

    @Override
    public String toString() {
        return String.format("(%.3f, %.3f, %.3f)", x, y, z);
    }
}
