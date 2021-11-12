package lemon.engine.toolbox;

import java.util.Optional;

public class MaterialBuilder {
    private String name;
    private Color ambient;
    private Color diffuse;
    private Color specular;

    public void reset() {
        this.name = null;
        this.ambient = null;
        this.diffuse = null;
        this.specular = null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAmbient(Color ambient) {
        this.ambient = ambient;
    }

    public void setDiffuse(Color diffuse) {
        this.diffuse = diffuse;
    }

    public void setSpecular(Color specular) {
        this.specular = specular;
    }

    public Optional<Material> build() {
        if (name == null) {
            return Optional.empty();
        }
        if (diffuse == null) {
            throw new IllegalStateException("Missing Diffuse");
        }
        if (ambient == null) {
            ambient = Color.WHITE;
        }
        if (specular == null) {
            specular = Color.WHITE;
        }
        return Optional.of(new Material(name, ambient, diffuse, specular, Color.BLACK, 1f, 1f, 1f));
    }
}
