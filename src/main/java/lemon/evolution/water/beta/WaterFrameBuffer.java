package lemon.evolution.water.beta;

import lemon.engine.frameBuffer.FrameBuffer;

public class WaterFrameBuffer extends FrameBuffer {

    public enum Type {
        REFLECTION,
        REFRACTION
    }

    public static final int REFLECTION_WIDTH = 320;
    public static final int REFLECTION_HEIGHT = 180;
    public static final int REFRACTION_WIDTH = 1280;
    public static final int REFRACTION_HEIGHT = 720;

    private Type type;

    public WaterFrameBuffer(int width, int height, Type type) {
        super(width, height);
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }
}
