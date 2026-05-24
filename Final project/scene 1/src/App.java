import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Raytracer v0.9 – Escena de prueba con reflexión y refracción
 *
 * Escena:
 *   - Esfera especular (espejo) roja
 *   - Esfera refractiva (vidrio) azul
 *   - Esfera difusa blanca de prueba
 *   - Suelo plano gris
 *   - Múltiples fuentes de luz
 *
 * Nota: Esta es una escena de PRUEBA. Para el entregable final, 
 * usar modelos OBJ como el Koenigsegg.
 */
public class App {
    public static void main(String[] args) throws Exception {
        int width  = 1024;
        int height = 768;

        // ── Cámara ────────────────────────────────────────────────────────────
        Camera camera = new Camera(
            new Vector3D(0, 2, -8),      // posición
            new Vector3D(0, 1, 0),       // mira aquí
            new Vector3D(0, 1, 0),       // up
            45.0,                        // FOV
            width, height,
            0.1, 200.0
        );

        // ── Escena ────────────────────────────────────────────────────────────
        Scene scene = new Scene(new Color(20, 20, 40)); // fondo azul oscuro

        // ── Luces ─────────────────────────────────────────────────────────────
        // Luz clave: direccional blanca
        scene.addLight(new DirectionalLight(
            new Vector3D(-1.0, -2.0, 0.5),
            new Color(255, 255, 255),
            1.2
        ));

        // Luz puntual cálida desde arriba-derecha
        scene.addLight(new PointLight(
            new Vector3D(4.0, 4.0, 2.0),
            new Color(255, 220, 180),
            20.0
        ));

        // Luz azul desde la izquierda (relleno)
        scene.addLight(new PointLight(
            new Vector3D(-4.0, 2.0, 0.0),
            new Color(100, 150, 255),
            15.0
        ));

        // ── Objetos ───────────────────────────────────────────────────────────

        // 1. Esfera especular roja (espejo con reflexión)
        Material mirrorRed = Material.mirror(new Color(220, 50, 50), 0.8);
        scene.addObject(new Sphere(new Vector3D(-2, 1.2, 0), 0.8, mirrorRed));

        // 2. Esfera refractiva azul (vidrio transparente)
        Material glassBlue = Material.glass(new Color(100, 150, 255), 1.5, 0.9);
        scene.addObject(new Sphere(new Vector3D(2, 1.2, 0), 0.8, glassBlue));

        // 3. Esfera difusa blanca (de prueba, para ver reflejos)
        Material diffuseWhite = Material.diffuse(new Color(200, 200, 200));
        scene.addObject(new Sphere(new Vector3D(0, 1.2, 2.5), 0.5, diffuseWhite));

        // 4. Suelo plano (triángulos)
        Material floorMat = Material.diffuse(new Color(100, 100, 100));
        double floorY = 0.0;
        double floorSize = 20.0;

        // Triángulo 1 del suelo
        scene.addObject(new Triangle(
            new Vector3D(-floorSize, floorY, -floorSize),
            new Vector3D(floorSize, floorY, -floorSize),
            new Vector3D(floorSize, floorY, floorSize),
            floorMat
        ));

        // Triángulo 2 del suelo
        scene.addObject(new Triangle(
            new Vector3D(-floorSize, floorY, -floorSize),
            new Vector3D(floorSize, floorY, floorSize),
            new Vector3D(-floorSize, floorY, floorSize),
            floorMat
        ));

        // ── Renderizado ───────────────────────────────────────────────────────
        Raytracer raytracer = new Raytracer(scene, camera, 4);
        BufferedImage img = raytracer.render();

        // Guardar imagen
        File outputFile = new File("output_v0.9_reflection_refraction.png");
        ImageIO.write(img, "PNG", outputFile);
        System.out.printf("✓ Imagen guardada: %s%n", outputFile.getAbsolutePath());
    }
}
