package lemon.engine.draw;

import lemon.engine.math.FloatData;

import java.nio.FloatBuffer;

public record DrawableData(int[] indices, FloatData[][] vertices, int stride, FloatBuffer floatBuffer) {
    public DrawableData(int[] indices, FloatData[][] vertices) {
        this(indices, vertices, Drawable.getStride(vertices));
    }

    public DrawableData(int[] indices, FloatData[][] vertices, int stride) {
        this(indices, vertices, stride, Drawable.getFloatBuffer(vertices, stride));
    }
}
