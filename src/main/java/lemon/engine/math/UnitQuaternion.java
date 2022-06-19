package lemon.engine.math;

public interface UnitQuaternion extends Quaternion {
    @Override
    public default Quaternion inverse() {
        return conjugate();
    }

    @Override
    public default float lengthSquared() {
        return 1.0f;
    }

    @Override
    public default float length() {
        return 1.0f;
    }

    @Override
    public default void toRotationMatrix(Matrix result) {
        var s = a();
        var x = b();
        var y = c();
        var z = d();
        var xx = 2f * x * x;
        var yy = 2f * y * y;
        var zz = 2f * z * z;
        var xy = 2f * x * y;
        var xz = 2f * x * z;
        var yz = 2f * y * z;
        var sx = 2f * s * x;
        var sy = 2f * s * y;
        var sz = 2f * s * z;
        // zeros
        result.set(0, 3, 0f);
        result.set(1, 3, 0f);
        result.set(2, 3, 0f);
        result.set(3, 0, 0f);
        result.set(3, 1, 0f);
        result.set(3, 2, 0f);
        // diagonal
        result.set(0, 0, 1f - yy - zz);
        result.set(1, 1, 1f - xx - zz);
        result.set(2, 2, 1f - xx - yy);
        result.set(3, 3, 1f);
        // others
        result.set(0, 1, xy - sz);
        result.set(1, 0, xy + sz);
        result.set(0, 2, xz + sy);
        result.set(2, 0, xz - sy);
        result.set(1, 2, yz - sx);
        result.set(2, 1, yz + sx);
    }

    @Override
    public default float angleBetween(Quaternion q) {
        return (float) Math.acos(dotProduct(q));
    }

    public static boolean isEqual(UnitQuaternion a, UnitQuaternion b, float tolerance) {
        return Math.abs(a.dotProduct(b) - 1.0f) <= tolerance;
    }
}
