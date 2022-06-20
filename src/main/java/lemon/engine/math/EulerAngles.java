package lemon.engine.math;

import com.google.errorprone.annotations.CheckReturnValue;
import org.junit.jupiter.api.Assertions;

public record EulerAngles(Vector3D vector, EulerAnglesConvention convention) {
    public float pitch() {
        return vector.x();
    }

    public float yaw() {
        return vector.y();
    }

    public float roll() {
        return vector.z();
    }

    public EulerAngles withPitch(float pitch) {
        return new EulerAngles(vector.withX(pitch), convention);
    }

    public EulerAngles withZeroPitch() {
        return new EulerAngles(vector.withZeroX(), convention);
    }

    public EulerAngles withOnlyPitch() {
        return new EulerAngles(vector.withOnlyX(), convention);
    }

    public EulerAngles withYaw(float yaw) {
        return new EulerAngles(vector.withY(yaw), convention);
    }

    public EulerAngles withZeroYaw() {
        return new EulerAngles(vector.withZeroY(), convention);
    }

    public EulerAngles withOnlyYaw() {
        return new EulerAngles(vector.withOnlyY(), convention);
    }

    public EulerAngles withRoll(float roll) {
        return new EulerAngles(vector.withZ(roll), convention);
    }

    public EulerAngles withZeroRoll() {
        return new EulerAngles(vector.withZeroZ(), convention);
    }

    public EulerAngles withOnlyRoll() {
        return new EulerAngles(vector.withOnlyZ(), convention);
    }

    @CheckReturnValue
    public static boolean isEqual(EulerAngles a, EulerAngles b, float tolerance) {
        return MathUtil.angleBetween(a.pitch(), b.pitch()) <= tolerance &&
                MathUtil.angleBetween(a.yaw(), b.yaw()) <= tolerance &&
                MathUtil.angleBetween(a.roll(), b.roll()) <= tolerance &&
                a.convention() == b.convention();
    }

    public static void assertEquals(EulerAngles a, EulerAngles b, float delta) {
        Assertions.assertTrue(isEqual(a, b, delta), () -> String.format("%s =/= %s", a, b));
    }
}

