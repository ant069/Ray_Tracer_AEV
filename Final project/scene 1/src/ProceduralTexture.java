import java.awt.image.BufferedImage;

/**
 * Generates procedural concrete textures using multi-octave value noise.
 * Produces a diffuse color map and a matching tangent-space normal map
 * without requiring any external image files.
 */
public class ProceduralTexture {

    /**
     * Concrete diffuse texture with FBM color variation around a base tint.
     *
     * @param width  texture width in pixels
     * @param height texture height in pixels
     * @param baseR  base red channel (0–255)
     * @param baseG  base green channel (0–255)
     * @param baseB  base blue channel (0–255)
     */
    public static Texture concrete(int width, int height, int baseR, int baseG, int baseB) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double nx = x / (double) width;
                double ny = y / (double) height;
                double n  = fbm(nx * 4.0, ny * 4.0, 5, 1337);
                double t  = (n - 0.5) * 0.16;   // ±8% color variation — subtle, not blotchy
                int r = clamp((int)(baseR * (1.0 + t)));
                int g = clamp((int)(baseG * (1.0 + t)));
                int b = clamp((int)(baseB * (1.0 + t)));
                img.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return Texture.fromImage(img);
    }

    /**
     * Tangent-space normal map derived from a height field that matches the
     * concrete diffuse above, so surface color variation correlates with relief.
     *
     * @param width  texture width in pixels
     * @param height texture height in pixels
     */
    public static Texture concreteNormal(int width, int height) {
        double[][] hf = new double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double nx = x / (double) width;
                double ny = y / (double) height;
                hf[y][x] = fbm(nx * 8.0, ny * 8.0, 4, 42);
            }
        }
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        double scale = 2.2;   // bump strength — concrete is rough but not extreme
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int xp = (x + 1) % width,  xm = (x - 1 + width)  % width;
                int yp = (y + 1) % height,  ym = (y - 1 + height) % height;
                double dhdx = (hf[y][xp] - hf[y][xm]) * scale;
                double dhdy = (hf[yp][x] - hf[ym][x]) * scale;
                double len  = Math.sqrt(dhdx * dhdx + dhdy * dhdy + 1.0);
                double nr   = (-dhdx / len) * 0.5 + 0.5;
                double ng   = (-dhdy / len) * 0.5 + 0.5;
                double nb   = (1.0   / len) * 0.5 + 0.5;
                img.setRGB(x, y,
                    (clamp((int)(nr * 255)) << 16) |
                    (clamp((int)(ng * 255)) <<  8) |
                     clamp((int)(nb * 255)));
            }
        }
        return Texture.fromImage(img);
    }

    // Multi-octave fractional Brownian motion
    private static double fbm(double x, double y, int octaves, int seed) {
        double value = 0, amplitude = 0.5, frequency = 1.0, max = 0;
        for (int i = 0; i < octaves; i++) {
            value     += amplitude * valueNoise(x * frequency + seed, y * frequency + seed);
            max       += amplitude;
            amplitude *= 0.5;
            frequency *= 2.0;
        }
        return value / max;
    }

    private static double valueNoise(double x, double y) {
        int    ix = (int) Math.floor(x), iy = (int) Math.floor(y);
        double fx = x - ix,              fy = y - iy;
        double ux = smoothstep(fx),      uy = smoothstep(fy);
        double v00 = hash(ix,   iy),    v10 = hash(ix + 1, iy);
        double v01 = hash(ix,   iy + 1), v11 = hash(ix + 1, iy + 1);
        return lerp(lerp(v00, v10, ux), lerp(v01, v11, ux), uy);
    }

    private static double hash(int x, int y) {
        int n = x * 1619 + y * 31337;
        n = (n << 13) ^ n;
        return ((n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) / 2147483648.0;
    }

    private static double smoothstep(double t) { return t * t * (3.0 - 2.0 * t); }
    private static double lerp(double a, double b, double t) { return a + t * (b - a); }
    private static int    clamp(int v) { return Math.min(255, Math.max(0, v)); }
}
