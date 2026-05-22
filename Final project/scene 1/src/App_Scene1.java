import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * SCENE 1 – "CRYSTAL GEOMETRY"
 *
 * NARRATIVA:
 * Una composición artística que explora cómo la luz se comporta de manera diferente
 * según los materiales. En el centro, una gran pirámide de vidrio refracta la luz,
 * creando un foco visual hipnótico. Alrededor de ella, pequeñas esferas metálicas
 * reflejan la escena dramáticamente. El piso es un espejo perfecto que duplica la
 * complejidad visual. La iluminación viene de arriba, creando sombras profundas y
 * reflejos especulares que cuentan la historia de cómo la luz interactúa con la materia.
 *
 * ELEMENTOS VISUALES:
 * - Pirámide de vidrio (vidrio transparente con IOR=1.5)
 * - 5 esferas de acero pulido alrededor (espejos con brillo controlado)
 * - Piso espejo (reflexión especular perfecta)
 * - Fondo oscuro para contraste
 * - 3 fuentes de luz (clave, relleno, contorno)
 *
 * PROPÓSITO EDUCATIVO:
 * Demuestra las capacidades del ray tracer:
 * ✓ Refracción con Ley de Snell (pirámide de vidrio)
 * ✓ Reflexiones especulares (esferas metálicas)
 * ✓ Múltiples bounces (luz rebotando entre materiales)
 * ✓ Iluminación Blinn-Phong
 * ✓ Sombras duras correctas
 */
public class App_Scene1 {
    public static void main(String[] args) throws Exception {
        // Resolución FINAL: 4096 x 2160 (8K)
        // Para desarrollo/prueba: 1024 x 576 (escala 4:1)
        // NOTA: Cambiar estos valores para renderizar final
        int width  = 1024;
        int height = 576;
        
        boolean isFinalRender = false;  // Cambiar a true para 4096x2160
        if (isFinalRender) {
            width = 4096;
            height = 2160;
        }

        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║          SCENE 1: CRYSTAL GEOMETRY - Ray Tracer            ║");
        System.out.println("║                  Resolución: " + width + "x" + height);
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        // ── CÁMARA ─────────────────────────────────────────────────────────────
        // Posicionada para ver la pirámide de frente, ligeramente arriba
        Camera camera = new Camera(
            new Vector3D(0, 4, -12),     // posición de cámara
            new Vector3D(0, 1.5, 0),     // mira al centro de la escena
            new Vector3D(0, 1, 0),       // vector up
            50.0,                        // FOV (más ancho para ver la composición)
            width, height,
            0.1, 300.0
        );

        // ── ESCENA ──────────────────────────────────────────────────────────────
        // Fondo azul oscuro (noche, espacio)
        Scene scene = new Scene(new Color(15, 15, 35));

        // ── ILUMINACIÓN ─────────────────────────────────────────────────────────
        
        // 1. Luz clave: Blanca fría desde arriba-izquierda (simulando luna)
        scene.addLight(new DirectionalLight(
            new Vector3D(-0.6, -1.8, 0.4),  // dirección
            new Color(220, 230, 255),       // azul frío
            1.4                             // intensidad
        ));

        // 2. Luz relleno: Dorada cálida desde la derecha (complementaria)
        scene.addLight(new PointLight(
            new Vector3D(8.0, 3.0, -2.0),
            new Color(255, 200, 100),       // dorado cálido
            25.0
        ));

        // 3. Luz de contorno: Azul profundo desde atrás (rim light)
        scene.addLight(new PointLight(
            new Vector3D(-5.0, 5.0, 8.0),
            new Color(100, 120, 200),       // azul profundo
            18.0
        ));

        // ── OBJETOS – MATERIALIZACIÓN DE LA NARRATIVA ───────────────────────────

        // ·· PISO (ESPEJO PERFECTO) ··
        // Crea reflexiones secundarias que multiplican la complejidad
        Material floorMirror = Material.mirror(new Color(60, 60, 80), 0.95);
        double floorY = 0.0;
        double floorSize = 30.0;
        scene.addObject(new Triangle(
            new Vector3D(-floorSize, floorY, floorSize),
            new Vector3D(floorSize, floorY, floorSize),
            new Vector3D(floorSize, floorY, -floorSize),
            floorMirror
        ));
        scene.addObject(new Triangle(
            new Vector3D(-floorSize, floorY, floorSize),
            new Vector3D(floorSize, floorY, -floorSize),
            new Vector3D(-floorSize, floorY, -floorSize),
            floorMirror
        ));

        // ·· PIRÁMIDE DE VIDRIO (ELEMENTO CENTRAL) ··
        // Material refractivo: vidrio con IOR realista (1.5)
        // La pirámide es el protagonista visual de la composición
        Material glassBlue = Material.glass(
            new Color(150, 180, 220),  // tinte azul pálido
            1.5,                       // IOR vidrio
            0.92                       // alta transparencia
        );

        // Construir pirámide con 4 triángulos (base + 4 caras)
        Vector3D pyrBase_A = new Vector3D(-2.0, 0.5, 2.0);
        Vector3D pyrBase_B = new Vector3D(2.0, 0.5, 2.0);
        Vector3D pyrBase_C = new Vector3D(2.0, 0.5, -2.0);
        Vector3D pyrBase_D = new Vector3D(-2.0, 0.5, -2.0);
        Vector3D pyrApex = new Vector3D(0, 4.5, 0);  // Punta de la pirámide

        // Base inferior
        scene.addObject(new Triangle(pyrBase_A, pyrBase_C, pyrBase_B, glassBlue));
        scene.addObject(new Triangle(pyrBase_A, pyrBase_D, pyrBase_C, glassBlue));

        // Caras laterales
        scene.addObject(new Triangle(pyrBase_A, pyrApex, pyrBase_B, glassBlue));  // frente
        scene.addObject(new Triangle(pyrBase_B, pyrApex, pyrBase_C, glassBlue));  // derecha
        scene.addObject(new Triangle(pyrBase_C, pyrApex, pyrBase_D, glassBlue));  // atrás
        scene.addObject(new Triangle(pyrBase_D, pyrApex, pyrBase_A, glassBlue));  // izquierda

        // ·· ESFERAS METÁLICAS (SATÉLITES VISUALES) ··
        // Pequeñas esferas reflectivas alrededor de la pirámide
        // Cada una tiene un tinte metálico diferente
        Material steelMetal = Material.metallic(new Color(180, 190, 200), 0.85, 96);
        Material copperMetal = Material.metallic(new Color(220, 140, 60), 0.80, 80);
        Material silverMetal = Material.metallic(new Color(210, 210, 220), 0.90, 120);

        // Esfera 1 (acero, izquierda-adelante)
        scene.addObject(new Sphere(new Vector3D(-4.0, 1.2, -1.5), 0.7, steelMetal));

        // Esfera 2 (cobre, derecha-adelante)
        scene.addObject(new Sphere(new Vector3D(4.0, 1.2, -1.5), 0.7, copperMetal));

        // Esfera 3 (plata, izquierda-atrás)
        scene.addObject(new Sphere(new Vector3D(-3.5, 0.9, 3.0), 0.6, silverMetal));

        // Esfera 4 (acero, derecha-atrás)
        scene.addObject(new Sphere(new Vector3D(3.5, 0.9, 3.0), 0.6, steelMetal));

        // Esfera 5 (cobre, arriba-centro, pequeña)
        scene.addObject(new Sphere(new Vector3D(0, 3.0, -0.5), 0.5, copperMetal));

        // ·· BLOQUE REFLECTIVO (OPCIONAL - para más complejidad) ··
        // Un cubo de espejo oscuro detrás de la pirámide
        Material darkMirror = Material.mirror(new Color(40, 40, 50), 0.70);
        double cubeSizeX = 3.0;
        double cubeSizeY = 3.0;
        double cubeSizeZ = 0.5;
        double cubeZ = 5.5;
        
        // Frente del cubo (1 triángulo visible)
        scene.addObject(new Triangle(
            new Vector3D(-cubeSizeX, 0.5, cubeZ),
            new Vector3D(cubeSizeX, 3.5 + cubeSizeY, cubeZ),
            new Vector3D(cubeSizeX, 0.5, cubeZ),
            darkMirror
        ));
        scene.addObject(new Triangle(
            new Vector3D(-cubeSizeX, 0.5, cubeZ),
            new Vector3D(-cubeSizeX, 3.5 + cubeSizeY, cubeZ),
            new Vector3D(cubeSizeX, 3.5 + cubeSizeY, cubeZ),
            darkMirror
        ));

        // ── RENDERIZADO ─────────────────────────────────────────────────────────
        System.out.println("[Info] Iniciando renderizado con hasta 5 bounces...");
        long startTime = System.currentTimeMillis();

        Raytracer raytracer = new Raytracer(scene, camera, 5);
        BufferedImage img = raytracer.render();

        long elapsed = System.currentTimeMillis() - startTime;
        double seconds = elapsed / 1000.0;

        // ── GUARDAR IMAGEN ──────────────────────────────────────────────────────
        String filename = (isFinalRender)
            ? "Scene_1_FINAL_4096x2160.png"
            : "Scene_1_Preview_1024x576.png";
        
        File outputDir = new File("output");
        if (!outputDir.exists()) outputDir.mkdirs();
        
        File outputFile = new File(outputDir, filename);
        ImageIO.write(img, "PNG", outputFile);

        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                    RENDERIZADO COMPLETADO                 ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.printf("║ Tiempo total: %.2f segundos\n", seconds);
        System.out.printf("║ Resolución: %dx%d\n", width, height);
        System.out.printf("║ Archivo: %s\n", outputFile.getName());
        System.out.printf("║ Ruta: %s\n", outputFile.getAbsolutePath());
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }
}
