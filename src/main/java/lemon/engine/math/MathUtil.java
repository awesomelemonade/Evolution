package lemon.engine.math;

import lemon.evolution.pool.MatrixPool;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

import java.nio.IntBuffer;
import java.util.function.Supplier;

public class MathUtil {
	public static final float PI = (float) Math.PI;
	public static final float TAU = (float) (2.0 * Math.PI);

	private MathUtil() {
	}

	public static float cos(float angle) {
		return (float) Math.cos(angle);
	}

	public static float sin(float angle) {
		return (float) Math.sin(angle);
	}

	public static boolean inRange(float a, float min, float max) {
		return a >= min && a <= max;
	}

	public static float pow(float base, float power) {
		return (float) Math.pow(base, power);
	}

	public static float toRadians(float degrees) {
		return (float) Math.toRadians(degrees);
	}

	public static float toDegrees(float radians) {
		return (float) Math.toDegrees(radians);
	}

	public static float getAspectRatio(long window) {
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(window, width, height);
		return ((float) width.get()) / ((float) height.get());
	}

	public static Vector3D getVectorDirection(Vector3D rotation) {
		var cosX = Math.cos(rotation.x());
		return Vector3D.of(
				(float) (-(Math.sin(rotation.y()) * cosX)),
				(float) (Math.sin(rotation.x())),
				(float) (-(cosX * Math.cos(rotation.y()))));
	}

	public static Matrix getPerspective(Projection projection) {
		Matrix matrix = new Matrix(4);
		getPerspective(matrix, projection);
		return matrix;
	}

	public static void getPerspective(Matrix matrix, Projection projection) {
		float yScale = (float) (1f / Math.tan(projection.getFov() / 2f));
		float xScale = yScale / projection.getAspectRatio();
		matrix.set(0, 0, xScale);
		matrix.set(1, 1, yScale);
		matrix.set(2, 2, -(projection.getNearPlane() + projection.getFarPlane())
				/ (projection.getFarPlane() - projection.getNearPlane()));
		matrix.set(2, 3, (-2 * projection.getNearPlane() * projection.getFarPlane())
				/ (projection.getFarPlane() - projection.getNearPlane()));
		matrix.set(3, 2, -1);
		matrix.set(3, 3, 0);
	}

	public static Matrix getOrtho(float width, float height, float near, float far) {
		Matrix matrix = new Matrix(4);
		matrix.set(0, 0, 2f / width);
		matrix.set(1, 1, 2f / height);
		matrix.set(2, 2, 1f / (far - near));
		matrix.set(0, 3, -1f);
		matrix.set(1, 3, -1f);
		matrix.set(2, 3, (-near) / (far - near));
		matrix.set(3, 3, 1);
		return matrix;
	}

	public static Matrix getOrtho(float left, float right, float top, float bottom, float near, float far) {
		Matrix matrix = new Matrix(4);
		matrix.set(0, 0, 2f / (right - left));
		matrix.set(1, 1, 2f / (top - bottom));
		matrix.set(2, 2, -2f / (far - near));
		matrix.set(0, 3, -(right + left) / (right - left));
		matrix.set(1, 3, -(top + bottom) / (top - bottom));
		matrix.set(2, 3, -(far + near) / (far - near));
		matrix.set(3, 3, 1);
		return matrix;
	}

	public static Supplier<Matrix> getTransformationSupplier(Supplier<Vector3D> translationSupplier, Supplier<Vector3D> rotationSupplier) {
		Matrix a = new Matrix(4);
		Matrix b = new Matrix(4);
		Matrix c = new Matrix(4);
		return () -> {
			var translation = translationSupplier.get();
			var rotation = rotationSupplier.get();
			MathUtil.getRotationX(a, rotation.x());
			MathUtil.getRotationY(b, rotation.y());
			Matrix.multiply(c, a, b);
			MathUtil.getRotationZ(a, rotation.z());
			Matrix.multiply(b, c, a);
			MathUtil.getTranslation(a, translation);
			Matrix.multiply(c, b, a);
			return c;
		};
	}

	public static Matrix getTranslation(Vector3D vector) {
		Matrix matrix = Matrix.getIdentity(4);
		getTranslation(matrix, vector);
		return matrix;
	}

	public static void getTranslation(Matrix matrix, Vector3D vector) {
		matrix.clear();
		matrix.set(0, 0, 1f);
		matrix.set(1, 1, 1f);
		matrix.set(2, 2, 1f);
		matrix.set(3, 3, 1f);
		matrix.set(0, 3, vector.x());
		matrix.set(1, 3, vector.y());
		matrix.set(2, 3, vector.z());
	}

	public static Matrix getRotation(Vector3D rotation) {
		return MathUtil.getRotationX(rotation.x())
				.multiply(MathUtil.getRotationY(rotation.y()).multiply(MathUtil.getRotationZ(rotation.z())));
	}

	public static void getRotation(Matrix matrix, Vector3D rotation) {
		MathUtil.getRotationY(matrix, rotation.y());
		try (var a = MatrixPool.ofRotationX(rotation.x());
			 var b = MatrixPool.ofMultiplied(a, matrix)) {
			MathUtil.getRotationZ(a, rotation.z());
			Matrix.multiply(matrix, b, a);
		}
	}

	public static Supplier<Matrix> getRotationSupplier(Supplier<Vector3D> rotationSupplier) {
		Matrix a = new Matrix(4);
		Matrix b = new Matrix(4);
		Matrix c = new Matrix(4);
		return () -> {
			var rotation = rotationSupplier.get();
			MathUtil.getRotationX(a, rotation.x());
			MathUtil.getRotationY(b, rotation.y());
			Matrix.multiply(c, a, b);
			MathUtil.getRotationZ(a, rotation.z());
			Matrix.multiply(b, c, a);
			return b;
		};
	}

	public static Matrix getRotationX(float angle) {
		Matrix matrix = new Matrix(4);
		getRotationX(matrix, angle);
		return matrix;
	}

	public static void getRotationX(Matrix matrix, float angle) {
		matrix.clear();
		float sin = (float) Math.sin(angle);
		float cos = (float) Math.cos(angle);
		matrix.set(0, 0, 1);
		matrix.set(1, 1, cos);
		matrix.set(2, 1, sin);
		matrix.set(1, 2, -sin);
		matrix.set(2, 2, cos);
		matrix.set(3, 3, 1);
	}

	public static Matrix getRotationY(float angle) {
		Matrix matrix = new Matrix(4);
		getRotationY(matrix, angle);
		return matrix;
	}

	public static void getRotationY(Matrix matrix, float angle) {
		matrix.clear();
		float sin = (float) Math.sin(angle);
		float cos = (float) Math.cos(angle);
		matrix.set(0, 0, cos);
		matrix.set(1, 1, 1);
		matrix.set(2, 0, -sin);
		matrix.set(0, 2, sin);
		matrix.set(2, 2, cos);
		matrix.set(3, 3, 1);
	}

	public static Matrix getRotationZ(float angle) {
		Matrix matrix = new Matrix(4);
		getRotationZ(matrix, angle);
		return matrix;
	}

	public static void getRotationZ(Matrix matrix, float angle) {
		matrix.clear();
		float sin = (float) Math.sin(angle);
		float cos = (float) Math.cos(angle);
		matrix.set(0, 0, cos);
		matrix.set(0, 1, sin);
		matrix.set(1, 0, -sin);
		matrix.set(1, 1, cos);
		matrix.set(2, 2, 1);
		matrix.set(3, 3, 1);
	}

	public static Matrix getScalar(Vector3D vector) {
		Matrix matrix = new Matrix(4);
		getScalar(matrix, vector);
		return matrix;
	}

	public static void getScalar(Matrix matrix, Vector3D vector) {
		matrix.clear();
		matrix.set(0, 0, vector.x());
		matrix.set(1, 1, vector.y());
		matrix.set(2, 2, vector.z());
		matrix.set(3, 3, 1);
	}

	public static Matrix lookAt(Vector3D direction) {
		Matrix matrix = new Matrix(4);
		lookAt(matrix, direction, Vector3D.of(0f, 1f, 0f));
		return matrix;
	}

	public static void lookAt(Matrix matrix, Vector3D direction, Vector3D up) {
		var f = direction.normalize();
		var u = up.normalize();
		var s = f.crossProduct(u).normalize();
		u = s.crossProduct(f);

		matrix.clear();
		matrix.set(0, 0, s.x());
		matrix.set(1, 0, s.y());
		matrix.set(2, 0, s.z());
		matrix.set(0, 1, u.x());
		matrix.set(1, 1, u.y());
		matrix.set(2, 1, u.z());
		matrix.set(0, 2, -f.x());
		matrix.set(1, 2, -f.y());
		matrix.set(2, 2, -f.z());
		matrix.set(3, 3, 1);
	}

	/**
	 * @param value value to be clamped
	 * @param lower lower bound
	 * @param upper upper bound
	 * @return clamped value so it is in [a, b]
	 */
	public static float clamp(float value, float lower, float upper) {
		return Math.max(lower, Math.min(upper, value));
	}

	public static float saturate(float value) {
		return clamp(value, 0f, 1f);
	}

	public static float mod(float value, float mod) {
		return ((value % mod) + mod) % mod;
	}

	public static <T> T randomChoice(T[] array) {
		return array[(int) (Math.random() * array.length)];
	}
}
