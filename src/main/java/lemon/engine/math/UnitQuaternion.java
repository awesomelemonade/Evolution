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
}
