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
        assertThrows(IllegalArgumentException.class, () -> MathUtil.lookAt(matrix, Vector3D.ONE, Vector3D.ZERO));
    }

    @Test
    public void testSameDirection() {
        var vector = Vector3D.ONE.normalize();
        var matrix = new Matrix(4);
        MathUtil.lookAt(matrix, Vector3D.ONE, Vector3D.ONE);
        var forwardVector = Vector3D.of(0f, 0f, -1f);
        assertEquals(vector, matrix.multiply(forwardVector));
    }

    @Test
    public void testRandomSameDirection() {
        var matrix = new Matrix(4);
        for (int i = 0; i < 1000; i++) {
            var vector = Vector3D.ofRandomUnitVector();
            MathUtil.lookAt(matrix, vector, vector);
            var forwardVector = Vector3D.of(0f, 0f, -1f);
            assertTrue(Vector.isEqual(vector, matrix.multiply(forwardVector), 0.0001f));
        }
    }
}