package lemon.engine.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuaternionTest {
    private static final float TOLERANCE = 0.001f;

    @Test
    public void testFromToEulerAnglesX() {
        var angle = MathUtil.toRadians(90f);
        var a = Vector3D.of(angle, 0f, 0f);
        Quaternion q = Quaternion.fromEulerAngles(a);
        var b = q.toEulerAngles();
        assertTrue(Vector.isEqual(a, b, TOLERANCE));
    }

    @Test
    public void testFromToEulerAnglesY() {
        var angle = MathUtil.toRadians(90f);
        var a = Vector3D.of(0f, angle, 0f);
        Quaternion q = Quaternion.fromEulerAngles(a);
        var b = q.toEulerAngles();
        assertTrue(Vector.isEqual(a, b, TOLERANCE));
    }

    @Test
    public void testFromToEulerAnglesZ() {
        var angle = MathUtil.toRadians(90f);
        var a = Vector3D.of(0f, 0f, angle);
        Quaternion q = Quaternion.fromEulerAngles(a);
        var b = q.toEulerAngles();
        assertTrue(Vector.isEqual(a, b, TOLERANCE));
    }

    @Test
    public void testFromToEulerAnglesRandom() {
        MathUtilTest.assertAgreement(1000, Vector3D::ofRandomUnitVector,
                v -> Quaternion.fromEulerAngles(v).toEulerAngles());
    }

    @Test
    public void testRotationMatrixX() {
        var angle = MathUtil.toRadians(90f);
        Quaternion q = Quaternion.fromEulerAngles(Vector3D.of(angle, 0f, 0f));
        Matrix a = q.toRotationMatrix();
        Matrix b = MathUtil.getRotationX(angle);
        assertTrue(Matrix.isEqual(a, b, TOLERANCE));
    }

    @Test
    public void testRotationMatrixY() {
        var angle = MathUtil.toRadians(90f);
        Quaternion q = Quaternion.fromEulerAngles(Vector3D.of(0f, angle, 0f));
        Matrix a = q.toRotationMatrix();
        Matrix b = MathUtil.getRotationY(angle);
        assertTrue(Matrix.isEqual(a, b, TOLERANCE));
    }

    @Test
    public void testRotationMatrixZ() {
        var angle = MathUtil.toRadians(90f);
        Quaternion q = Quaternion.fromEulerAngles(Vector3D.of(0f, 0f, angle));
        Matrix a = q.toRotationMatrix();
        Matrix b = MathUtil.getRotationZ(angle);
        assertTrue(Matrix.isEqual(a, b, TOLERANCE));
    }
}