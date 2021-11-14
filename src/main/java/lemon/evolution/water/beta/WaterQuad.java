package lemon.evolution.water.beta;

import lemon.engine.math.FloatData;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Color;

public class WaterQuad {

    public static final FloatData[] QUAD_VERTICES = {
            v(-1f, 0f, -1f),
            v(-1f, 0f, 1f),
            v( 1f, 0f, 1f),
            v(-1f, 0f, -1f),
            v( 1f, 0f, -1f),
            v( 1f, 0f,  1f),
    };

    public static final FloatData[] QUAD_COLORS = {
            c(0f, 0f, 1f, 0f),
            c(0f, 0f, 1f, 0f),
            c(0f, 0f, 1f, 0f),
            c(0f, 0f, 1f, 0f),
            c(0f, 0f, 1f, 0f),
            c(0f, 0f, 1f, 0f),
    };

    private static Vector3D v(float x, float y, float z) {
        return Vector3D.of(x, y, z);
    }

    private static Color c(float r, float g, float b, float a) {
        return new Color(r, g, b, a);
    }

}
