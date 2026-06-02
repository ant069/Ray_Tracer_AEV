import java.awt.Color;

/**
 * Material – Defines optical properties of an object.
 *
 * Properties:
 *   - color:        base color of the material
 *   - reflectivity: specular reflection intensity (0.0 = matte, 1.0 = mirror)
 *   - refractivity: refraction intensity (0.0 = opaque, 1.0 = pure glass)
 *   - ior:          index of refraction (air=1.0, glass~1.5, diamond~2.42)
 *   - shininess:    Phong shininess exponent (controls highlight sharpness)
 */
public class Material {
    private final Color   color;
    private final double  reflectivity;    // 0.0 to 1.0
    private final double  refractivity;    // 0.0 to 1.0
    private final double  ior;             // index of refraction
    private final int     shininess;       // Phong exponent
    private double        specularCoeff;   // per-material Ks (default 1.0)
    private Texture       diffuseTexture;  // optional UV texture

    /** Full constructor. */
    public Material(Color color, double reflectivity, double refractivity, double ior, int shininess) {
        this.color         = color;
        this.reflectivity  = clamp(reflectivity);
        this.refractivity  = clamp(refractivity);
        this.ior           = Math.max(0.1, ior);
        this.shininess     = Math.max(1, shininess);
        this.specularCoeff = 1.0;
    }

    public Material withSpecular(double ks) { this.specularCoeff = Math.max(0.0, ks); return this; }

    /** Diffuse material (matte, no reflection or refraction). */
    public static Material diffuse(Color color) {
        return new Material(color, 0.0, 0.0, 1.0, 32);
    }

    /** Specular material (mirror). */
    public static Material mirror(Color color, double reflectivity) {
        return new Material(color, reflectivity, 0.0, 1.0, 128);
    }

    /** Refractive material (glass, water, etc.). */
    public static Material glass(Color color, double ior, double transparency) {
        return new Material(color, 0.1, transparency, ior, 64);
    }

    /** Metallic material (glossy reflection). */
    public static Material metallic(Color color, double reflectivity, int shininess) {
        return new Material(color, reflectivity, 0.0, 1.0, shininess);
    }

    private Texture normalMap;

    public Material withTexture(Texture t)   { this.diffuseTexture = t; return this; }
    public boolean  hasTexture()             { return diffuseTexture != null; }
    public Texture  getTexture()             { return diffuseTexture; }

    public Material withNormalMap(Texture t) { this.normalMap = t; return this; }
    public boolean  hasNormalMap()           { return normalMap != null; }
    public Texture  getNormalMap()           { return normalMap; }

    public Color getColor()            { return color; }
    public double getReflectivity()    { return reflectivity; }
    public double getRefractivity()    { return refractivity; }
    public double getIOR()             { return ior; }
    public int getShininess()          { return shininess; }
    public double getSpecularCoeff()   { return specularCoeff; }

    /** Returns true if the material is primarily refractive (transparent). */
    public boolean isRefractive() {
        return refractivity > reflectivity && refractivity > 0.1;
    }

    /** Returns true if the material is primarily reflective (specular). */
    public boolean isReflective() {
        return reflectivity > refractivity && reflectivity > 0.1;
    }

    /** Returns true if the material is fully opaque. */
    public boolean isOpaque() {
        return reflectivity < 0.01 && refractivity < 0.01;
    }

    private static double clamp(double v) {
        return Math.min(1.0, Math.max(0.0, v));
    }
}
