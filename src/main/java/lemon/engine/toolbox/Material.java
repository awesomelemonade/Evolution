package lemon.engine.toolbox;

public record Material(String name, Color ambient, Color diffuse, Color specular, Color emission,
                       float refraction, float dissolve, float specularExponent) {
    // https://www.loc.gov/preservation/digital/formats/fdd/fdd000508.shtml
    public Material(String name, Color color) {
        this(name, color, color, color, color, 1f, 1f, 1f);
    }
}