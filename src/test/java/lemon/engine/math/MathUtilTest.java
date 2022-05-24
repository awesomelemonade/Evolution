package lemon.engine.math;

import org.junit.jupiter.api.Test;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class MathUtilTest {
    private static final float TOLERANCE = 0.0001f;

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
        assertTrue(Vector.isEqual(vector, matrix.multiply(forwardVector), TOLERANCE));
    }

    @Test
    public void testRandomSameDirection() {
        var matrix = new Matrix(4);
        for (int i = 0; i < 1000; i++) {
            var vector = UnitVector3D.ofRandom();
            MathUtil.lookAt(matrix, vector, vector);
            var forwardVector = Vector3D.of(0f, 0f, -1f);
            assertTrue(Vector.isEqual(vector, matrix.multiply(forwardVector), TOLERANCE));
        }
    }

    @Test
    public void testRotationXAgreement() {
        var a = MathUtil.getRotationX(MathUtil.toRadians(30f));
        var b = MathUtil.getAxisAngle(UnitVector3D.X_AXIS, MathUtil.toRadians(30f));
        assertTrue(Matrix.isEqual(a, b, TOLERANCE));
        assertAgreement(1000, MathUtil::randomAngle,
                angle -> MathUtil.getRotationX(a, angle),
                angle -> MathUtil.getAxisAngle(b, UnitVector3D.X_AXIS, angle),
                () -> Matrix.isEqual(a, b, TOLERANCE));
    }

    @Test
    public void testRotationYAgreement() {
        var a = MathUtil.getRotationY(MathUtil.toRadians(30f));
        var b = MathUtil.getAxisAngle(UnitVector3D.Y_AXIS, MathUtil.toRadians(30f));
        assertTrue(Matrix.isEqual(a, b, TOLERANCE));
        assertAgreement(1000, MathUtil::randomAngle,
                angle -> MathUtil.getRotationY(a, angle),
                angle -> MathUtil.getAxisAngle(b, UnitVector3D.Y_AXIS, angle),
                () -> Matrix.isEqual(a, b, TOLERANCE));
    }

    @Test
    public void testRotationZAgreement() {
        var a = MathUtil.getRotationZ(MathUtil.toRadians(30f));
        var b = MathUtil.getAxisAngle(UnitVector3D.Z_AXIS, MathUtil.toRadians(30f));
        assertTrue(Matrix.isEqual(a, b, TOLERANCE));
        assertAgreement(1000, MathUtil::randomAngle,
                angle -> MathUtil.getRotationZ(a, angle),
                angle -> MathUtil.getAxisAngle(b, UnitVector3D.Z_AXIS, angle),
                () -> Matrix.isEqual(a, b, TOLERANCE));
    }

    private static <T> void assertAgreement(int iterations, Supplier<T> randomSupplier, Consumer<T> a, Consumer<T> b, BooleanSupplier assertion) {
        for (int i = 0; i < iterations; i++) {
            T random = randomSupplier.get();
            a.accept(random);
            b.accept(random);
            assertTrue(assertion.getAsBoolean());
        }
    }
}