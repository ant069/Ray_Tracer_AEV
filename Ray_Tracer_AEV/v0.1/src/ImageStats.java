import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

public class ImageStats {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("render.png"));
        int w = img.getWidth();
        int h = img.getHeight();
        List<int[]> red = new ArrayList<>();
        List<int[]> blue = new ArrayList<>();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (r == 255 && g == 0 && b == 0) {
                    red.add(new int[] {x, y});
                } else if (r == 0 && g == 0 && b == 255) {
                    blue.add(new int[] {x, y});
                }
            }
        }
        printStats("red", red);
        printStats("blue", blue);
    }

    private static void printStats(String name, List<int[]> pixels) {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int sumX = 0;
        int sumY = 0;
        for (int[] p : pixels) {
            int x = p[0];
            int y = p[1];
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
            sumX += x;
            sumY += y;
        }
        int n = pixels.size();
        System.out.println(name + " count=" + n + " x=[" + minX + "," + maxX + "] y=[" + minY + "," + maxY + "] cx=" + (sumX / (double)n) + " cy=" + (sumY / (double)n));
    }
}
