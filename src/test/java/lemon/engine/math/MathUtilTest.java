package lemon.engine.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MathUtilTest {
    @Test
    public void testLookAtDirectionZero() {
        var matrix = new Matrix(4);
        MathUtil.lookAt(matrix, Vector3D.ZERO, Vector3D.ONE);
        assertEquals(Matrix.IDENTITY_4, matrix);
    }

    @Test
    public void testLookAtUpZero() {
        var matrix = new Matrix(4);
        MathUtil.lookAt(matrix, Vector3D.ONE, Vector3D.ZERO);
        assertEquals(Matrix.IDENTITY_4, matrix);
    }
}