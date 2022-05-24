package lemon.engine.math;

public interface UnitVector3D extends Vector3D {
    public static final UnitVector3D X_AXIS = UnitVector3D.of(1f, 0f, 0f);
    public static final UnitVector3D Y_AXIS = UnitVector3D.of(0f, 1f, 0f);
    public static final UnitVector3D Z_AXIS = UnitVector3D.of(0f, 0f, 1f);

    private static UnitVector3D of(float x, float y, float z) {
        return new UnitVector3D.Impl(x, y, z);
    }

    public static UnitVector3D ofNormalized(Vector3D vector) {
        var length = vector.length();
        if (length == 0f) {
            throw new IllegalStateException("Cannot scale a vector with length 0");
        }
        return of(vector.x() / length, vector.y() / length, vector.z() / length);
    }

    public static UnitVector3D ofRandom() {
        // https://math.stackexchange.com/questions/44689/how-to-find-a-random-axis-or-unit-vector-in-3d
        var theta = Math.random() * MathUtil.TAU;
        var z = Math.random() * 2 - 1;
        var zFactor = Math.sqrt(1 - z * z);
        return of((float) (zFactor * Math.cos(theta)), (float) (zFactor * Math.sin(theta)), (float) z);
    }

    record Impl(float x, float y, float z) implements UnitVector3D {
        public Impl(UnitVector3D vector) {
            this(vector.x(), vector.y(), vector.z());
        }

        @Override
        public String toString() {
            return Vector3D.toString(this);
        }
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
