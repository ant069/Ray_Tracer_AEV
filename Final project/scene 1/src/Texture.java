import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Diffuse texture — loads a PNG and samples it by UV coordinates. */
public class Texture {
    private final int[] pixels;
    private final int   width, height;

    /** Returns null (with a warning) if the file cannot be read. */
    public static Texture load(String path) {
        try {
            BufferedImage img = ImageIO.read(new File(path));
            if (img == null) {
                System.err.println("[Texture] Cannot read: " + path);
                return null;
            }
            return new Texture(img);
        } catch (Exception e) {
            System.err.println("[Texture] Failed: " + path + " — " + e.getMessage());
            return null;
        }
    }

    public static Texture fromImage(BufferedImage img) {
        return new Texture(img);
    }

    private Texture(BufferedImage img) {
        width  = img.getWidth();
        height = img.getHeight();
        pixels = new int[width * height];
        img.getRGB(0, 0, width, height, pixels, 0, width);
    }

    /**
     * Sample texture at (u,v). UVs are wrapped to [0,1].
     * V is flipped because OBJ origin is bottom-left but image origin is top-left.
     * Returns [r, g, b] in [0.0, 1.0].
     */
    public double[] sample(double u, double v) {
        u = u - Math.floor(u);
        v = 1.0 - (v - Math.floor(v));
        int x = Math.min((int)(u * width),  width  - 1);
        int y = Math.min((int)(v * height), height - 1);
        int rgb = pixels[y * width + x];
        return new double[]{
            ((rgb >> 16) & 0xFF) / 255.0,
            ((rgb >>  8) & 0xFF) / 255.0,
            ( rgb        & 0xFF) / 255.0
        };
    }

    public int getWidth()  { return width;  }
    public int getHeight() { return height; }
}
