import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Area Light — rectangular light source that produces soft shadows (penumbra).
 *
 * Instead of a single point, light originates from an entire surface.
 * The shader takes samples² stratified samples over the area and averages
 * visibility — partially occluded zones receive blurred shadows.
 *
 * Use case: simulating Denji's shack window projecting soft moonlight,
 * with the wooden frame casting grid-shaped shadows.
 *
 *   corner ──────────────── corner + edgeU
 *     │                           │
 *     │       light area          │
 *     │                           │
 *   corner + edgeV ─── corner + edgeU + edgeV
 */
public class AreaLight extends Light {

    private final Vector3D corner;
    private final Vector3D edgeU;
    private final Vector3D edgeV;
    private final int      samples;
    // Fixed seed: deterministic renders without a visible grid pattern
    private static final Random RNG = new Random(137L);

    /**
     * @param corner    corner of the light rectangle
     * @param edgeU     horizontal edge vector (width)
     * @param edgeV     vertical edge vector (height)
     * @param color     light color
     * @param intensity base intensity
     * @param samples   number of samples per side (2=4 total, 3=9, 4=16)
     */
    public AreaLight(Vector3D corner, Vector3D edgeU, Vector3D edgeV,
                     Color color, double intensity, int samples) {
        super(color, intensity);
        this.corner  = corner;
        this.edgeU   = edgeU;
        this.edgeV   = edgeV;
        this.samples = Math.max(1, samples);
    }

    /** Geometric center of the area — used for the standard getLightDirection. */
    public Vector3D getCenter() {
        return corner
            .add(edgeU.multiply(0.5))
            .add(edgeV.multiply(0.5));
    }

    /**
     * Generates samples² stratified jittered points over the area.
     * Each sample falls at a random position within its grid cell,
     * eliminating the regular grid pattern that causes banding and discrete shadow ghosts.
     */
    public List<Vector3D> getSamplePoints() {
        List<Vector3D> pts = new ArrayList<>(samples * samples);
        for (int i = 0; i < samples; i++) {
            for (int j = 0; j < samples; j++) {
                double u = (i + RNG.nextDouble()) / samples;  // jitter within cell i
                double v = (j + RNG.nextDouble()) / samples;  // jitter within cell j
                pts.add(corner
                    .add(edgeU.multiply(u))
                    .add(edgeV.multiply(v)));
            }
        }
        return pts;
    }

    /** Total sample count = samples². */
    public int getTotalSamples() { return samples * samples; }

    @Override
    public Vector3D getLightDirection(Vector3D hitPoint) {
        return getCenter().subtract(hitPoint).normalize();
    }
}
