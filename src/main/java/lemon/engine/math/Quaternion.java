package lemon.engine.math;

import com.google.errorprone.annotations.CheckReturnValue;

import java.nio.FloatBuffer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

// based on https://web.mit.edu/2.998/www/QuaternionReport1.pdf
public interface Quaternion extends Vector<Quaternion> {
    public static final int NUM_DIMENSIONS = 4;

    public float a();
    public float b();
    public float c();
    public float d();

    public static Quaternion of(float a, float b, float c, float d) {
        return new Impl(a, b, c, d);
    }

    public static Quaternion of(Supplier<Float> a, Supplier<Float> b, Supplier<Float> c, Supplier<Float> d) {
        return new Quaternion() {
            @Override
            public float a() {
                return a.get();
            }

            @Override
            public float b() {
                return b.get();
            }

            @Override
            public float c() {
                return c.get();
            }

            @Override
            public float d() {
                return d.get();
            }

            @Override
            public String toString() {
                return Quaternion.toString(this);
            }
        };
    }

    record Impl(float a, float b, float c, float d) implements Quaternion {
        @Override
        public String toString() {
            return Quaternion.toString(this);
        }
    }

    public static String toString(Quaternion quaternion) {
        return String.format("Quaternion[a=%f, b=%f, c=%f, d=%f]", quaternion.a(), quaternion.b(), quaternion.c(), quaternion.d());
    }

    @Override
    public default int numDimensions() {
        return NUM_DIMENSIONS;
    }

    @Override
    public default void putInBuffer(FloatBuffer buffer) {
        buffer.put(a());
        buffer.put(b());
        buffer.put(c());
        buffer.put(d());
    }

    @Override
    public default void putInArray(float[] array) {
        array[0] = a();
        array[1] = b();
        array[2] = c();
        array[3] = d();
    }

    @Override
    public default Quaternion operate(UnaryOperator<Float> operator) {
        return of(operator.apply(a()), operator.apply(b()), operator.apply(c()), operator.apply(d()));
    }

    @Override
    public default Quaternion add(Quaternion vector) {
        return of(a() + vector.a(), b() + vector.b(), c() + vector.c(), d() + vector.d());
    }

    @Override
    public default Quaternion subtract(Quaternion vector) {
        return of(a() - vector.a(), b() - vector.b(), c() - vector.c(), d() - vector.d());
    }

    // Hamilton Product
    @Override
    public default Quaternion multiply(Quaternion vector) {
        float a = a() * vector.a() - b() * vector.b() - c() * vector.c() - d() * vector.d();
        float b = a() * vector.b() + vector.a() * b() + c() * vector.d() - d() * vector.c();
        float c = a() * vector.c() + vector.a() * c() + d() * vector.b() - b() * vector.d();
        float d = a() * vector.d() + vector.a() * d() + b() * vector.c() - c() * vector.b();
        return of(a, b, c, d);
    }

    @Override
    public default Quaternion multiply(float scale) {
        return of(scale * a(), scale * b(), scale * c(), scale * d());
    }

    // Multiply by the multiplicative inverse
    @Override
    public default Quaternion divide(Quaternion vector) {
        return multiply(vector.inverse());
    }

    @Override
    public default Quaternion divide(float scale) {
        return of(a() / scale, b() / scale, c() / scale, d() / scale);
    }

    @Override
    public default float lengthSquared() {
        // norm squared??
        // https://math.stackexchange.com/questions/3174308/norm-of-quaternion
        // https://web.mit.edu/2.998/www/QuaternionReport1.pdf page 16 proposition 9
        // https://en.wikipedia.org/wiki/Quaternion#Conjugation,_the_norm,_and_reciprocal
        var a = a();
        var b = b();
        var c = c();
        var d = d();
        return a * a + b * b + c * c + d * d;
    }

    @Override
    public default float dotProduct(Quaternion vector) {
        return a() * vector.a() + b() * vector.b() + c() * vector.c() + d() * vector.d();
    }

    @Override
    public default Quaternion inverse() {
        // conjugate().divide(lengthSquared())
        float lengthSquared = lengthSquared();
        return of(a() / lengthSquared, -b() / lengthSquared, -c() / lengthSquared, -d() / lengthSquared);
    }

    public default Quaternion conjugate() {
        return of(a(), -b(), -c(), -d());
    }

    public default Quaternion pow(float t) {
        var theta = Math.acos(a());
        // v = vector part of this quaternion
        // log q = [0, v / sin(theta) * theta]
        // t * log q = [0, t * theta / sin(theta) * v]
        // newtheta = t * theta / sin(theta) * length(v)
        var b = b();
        var c = c();
        var d = d();
        var length_v = Math.sqrt(b * b + c * c + d * d);
        var newtheta = t * theta / Math.sin(theta) * length_v;
        // q ^ t = exp (t * log q) = [cos(newtheta), sin(newtheta) * v / length(v)]
        var sin_newtheta = Math.sin(newtheta);
        return of((float) Math.cos(newtheta),
                (float) (sin_newtheta * b / length_v),
                (float) (sin_newtheta * c / length_v),
                (float) (sin_newtheta * d / length_v));
    }

    public default Vector3D apply(Vector3D vector) {
        // Optimizable: https://gamedev.stackexchange.com/questions/28395/rotating-vector3-by-a-quaternion
        // Apply rotation
        var q = of(0f, vector.x(), vector.y(), vector.z());
        var result = this.multiply(q).multiply(this.inverse());
        return Vector3D.of(result.b(), result.c(), result.d());
    }

    public default Matrix toRotationMatrix() {
        Matrix result = new Matrix(4);
        toRotationMatrix(result);
        return result;
    }

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

    public static Quaternion slerp(Quaternion p, Quaternion q, float h) {
        return p.multiply(p.conjugate().multiply(q).pow(h)); // Equation 6.4
    }

    // Loosely based on
    // https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles

    public static EulerAngles toEulerAngles(Quaternion quaternion) {
        var w = quaternion.a();
        var x = quaternion.b();
        var y = quaternion.c();
        var z = quaternion.d();

        // pitch (x-axis rotation)
        var sinr_cosp = 2f * (w * x + y * z);
        var cosr_cosp = 1f - 2f * (x * x + y * y);
        var pitch = Math.atan2(sinr_cosp, cosr_cosp);

        // yaw (y-axis rotation)
        var sinp = 2f * (w * y - z * x);
        var yaw = Math.abs(sinp) >= 1f ? Math.copySign(Math.PI / 2, sinp) : Math.asin(sinp); // use 90 degrees if out of range

        // roll (z-axis rotation)
        var siny_cosp = 2f * (w * z + x * y);
        var cosy_cosp = 1f - 2f * (y * y + z * z);
        var roll = Math.atan2(siny_cosp, cosy_cosp);

        var vector = Vector3D.of((float) pitch, (float) yaw, (float) roll);
        return new EulerAngles(vector, EulerAnglesConvention.YAW_PITCH_ROLL);
    }

    public static UnitQuaternion fromEulerAngles(EulerAngles eulerAngles) {
        return UnitQuaternion.fromEulerAngles(eulerAngles);
    }

    public default EulerAngles toEulerAngles() {
        return Quaternion.toEulerAngles(this);
    }

    public default float angleBetween(Quaternion q) {
        // Proposition 10: q dot q' = ||q|| * ||q'|| * cos(theta)
        // theta = angle between q and q'
        return (float) Math.acos(dotProduct(q) / length() / q.length());
    }

    @CheckReturnValue
    public static boolean isEqual(Quaternion a, Quaternion b, float tolerance) {
        // Optimizable: take out the Math.acos() call in a.angleBetween()
        return a.angleBetween(b) <= tolerance;
    }
}
