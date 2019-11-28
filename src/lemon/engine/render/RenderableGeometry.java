package lemon.engine.render;

import lemon.engine.math.VectorArray;

public interface RenderableGeometry<T> extends Renderable {
    public VectorArray getVectorArray();
    public T[] getBindedValues();
    public ShaderProgram getShaderProgram();
    public default void render() {

    }
}
