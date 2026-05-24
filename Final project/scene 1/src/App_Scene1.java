import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * SCENE 1 - "KOENIGSEGG SHOWROOM"
 *
 * NARRATIVA:
 * Un render elegante que presenta un Koenigsegg superdeportivo en un showroom nocturno.
 * El auto es el protagonista absoluto: su carroceria pulida refleja la iluminacion direccional,
 * mientras que el piso oscuro muestra sombras suaves y realza la sensacion de poder.
 * La historia es la de la quietud previa a la aceleracion, con una atmosfera de lujo y tension.
 *
 * ELEMENTOS VISUALES:
 * - Auto Koenigsegg cargado desde un archivo OBJ
 * - Material metalizado y brillante con reflejos especulares
 * - Piso oscuro y ligeramente reflectante
 * - 3 luces para dar volumen y contorno al modelo
 *
 * PROPOSITO EDUCATIVO:
 * Demuestra las capacidades del ray tracer:
 * - OBJ importado correctamente
 * - Material Blinn-Phong con especular y brillo
 * - Iluminacion con multiples fuentes
 * - Sombras y reflexiones suaves
 */
public class App_Scene1 {
    public static void main(String[] args) throws Exception {
        int width  = 1280;
        int height = 720;

        boolean isFinalRender = false;  // Cambiar a true para render final 4096x2160
        if (isFinalRender) {
            width = 4096;
            height = 2160;
        }

        System.out.println("+---------------------------------------------------------------+");
        System.out.println("|          SCENE 1: KOENIGSEGG SHOWROOM - Ray Tracer           |");
        System.out.println("|                  Resolution: " + width + "x" + height + "                 |");
        System.out.println("+---------------------------------------------------------------+");

        Camera camera = new Camera(
            new Vector3D(0, 3.5, -14),
            new Vector3D(0, 1.5, 0),
            new Vector3D(0, 1, 0),
            45.0,
            width, height,
            0.1, 300.0
        );

        Scene scene = new Scene(new Color(12, 12, 18));

        scene.addLight(new DirectionalLight(
            new Vector3D(-0.5, -1.0, -0.3),
            new Color(220, 220, 240),
            2.0
        ));

        scene.addLight(new PointLight(
            new Vector3D(5.0, 5.5, -3.0),
            new Color(255, 210, 180),
            120.0
        ));

        scene.addLight(new PointLight(
            new Vector3D(-4.0, 3.5, 6.0),
            new Color(120, 140, 255),
            90.0
        ));

        Material carPaint = Material.metallic(new Color(220, 50, 50), 0.55, 110);
        Material floorMat = Material.metallic(new Color(20, 20, 30), 0.24, 48);

        double floorY = 0.0;
        double floorSize = 20.0;
        scene.addObject(new Triangle(
            new Vector3D(-floorSize, floorY, floorSize),
            new Vector3D(floorSize, floorY, floorSize),
            new Vector3D(floorSize, floorY, -floorSize),
            floorMat
        ));
        scene.addObject(new Triangle(
            new Vector3D(-floorSize, floorY, floorSize),
            new Vector3D(floorSize, floorY, -floorSize),
            new Vector3D(-floorSize, floorY, -floorSize),
            floorMat
        ));

        ObjModel koenigsegg = ObjReader.parse("../models/koenigsegg.obj");
        koenigsegg.printSummary();
        koenigsegg.addToScene(scene, carPaint, new Vector3D(0, -4.0, -16.0), 1.0);

        System.out.println("[Info] Starting fast preview render with up to 1 bounce...");
        long startTime = System.currentTimeMillis();

        // Use minimal bounces for a very fast preview
        Raytracer raytracer = new Raytracer(scene, camera, 1);
        BufferedImage img = raytracer.render();

        long elapsed = System.currentTimeMillis() - startTime;
        double seconds = elapsed / 1000.0;

        String filename = (isFinalRender)
            ? "Scene_1_Koenigsegg_FINAL_4096x2160.png"
            : "Scene_1_Koenigsegg_Preview_1280x720.png";

        File outputDir = new File("output");
        if (!outputDir.exists()) outputDir.mkdirs();

        File outputFile = new File(outputDir, filename);
        ImageIO.write(img, "PNG", outputFile);

        System.out.println("\n+---------------------------------------------------------------+");
        System.out.println("|                    RENDERIZADO COMPLETADO                    |");
        System.out.println("+---------------------------------------------------------------+");
        System.out.printf("| Tiempo total: %.2f segundos\n", seconds);
        System.out.printf("| Resolucion: %dx%d\n", width, height);
        System.out.printf("| Archivo: %s\n", outputFile.getName());
        System.out.printf("| Ruta: %s\n", outputFile.getAbsolutePath());
        System.out.println("+---------------------------------------------------------------+");
    }
}
