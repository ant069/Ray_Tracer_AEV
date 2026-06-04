import java.awt.image.BufferedImage;

/**
 * Procedural textures using multi-octave value noise.
 * Generates wood grain and concrete surfaces without external image files.
 */
public class ProceduralTexture {

    /**
     * Dark wood-grain texture with anisotropic FBM noise.
     * Grain runs along the X (U) axis — looks like horizontal siding on walls
     * and like floor boards running across the width on the floor.
     * No explicit plank seam lines: the grain variation and normal map
     * carry all the surface detail, avoiding the "garage door" stripe effect.
     */
    public static Texture wood(int width, int height, int baseR, int baseG, int baseB) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double nx = x / (double) width;
                double ny = y / (double) height;

                // Anisotropic grain: high frequency along X, low along Y.
                // This reads as horizontal wood grain on walls and floor planks.
                double grain = fbm(nx * 11.0, ny * 1.8, 5, 777);
                // Secondary cross-grain adds knot/ring variation without
                // introducing the repeating stripe that plankFreq caused.
                double cross = fbm(nx * 2.5, ny * 7.0, 3, 4321) * 0.35;
                double t = (grain - 0.5) * 0.18 + (cross - 0.5) * 0.06;

                int r = clamp((int)(baseR * (1.0 + t)));
                int g = clamp((int)(baseG * (1.0 + t)));
                int b = clamp((int)(baseB * (1.0 + t)));
                img.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return Texture.fromImage(img);
    }

    /**
     * Tangent-space normal map matched to the grain pattern in wood().
     * Uses pure FBM height field — no sinusoidal waves (those caused the
     * "wavy wallpaper" look in earlier versions). Scale 3.2 gives crisp
     * relief under raking moonlight without excessive specular noise.
     */
    public static Texture woodNormal(int width, int height) {
        double[][] hf = new double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double nx = x / (double) width;
                double ny = y / (double) height;
                // Match same anisotropic pattern as wood() diffuse
                double grain = fbm(nx * 11.0, ny * 1.8, 4, 777);
                double cross = fbm(nx * 2.5,  ny * 7.0, 2, 4321) * 0.35;
                hf[y][x] = grain * 0.70 + cross * 0.30;
            }
        }
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        double scale = 3.2;
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
