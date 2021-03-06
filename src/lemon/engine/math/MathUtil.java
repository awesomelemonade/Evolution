package lemon.engine.math;

import java.nio.IntBuffer;
import java.util.Optional;
import java.util.function.Supplier;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

public class MathUtil {
	public static final float PI = (float) Math.PI;
	public static final float TAU = (float) (2.0 * Math.PI);

	private MathUtil() {}
	
	public static boolean inRange(float a, float min, float max) {
		return a >= min && a <= max;
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
	public static Supplier<Matrix> getTransformationSupplier(Vector3D translation, Vector3D rotation) {
		Matrix a = new Matrix(4);
		Matrix b = new Matrix(4);
		Matrix c = new Matrix(4);
		return () -> {
			MathUtil.getRotationX(a, rotation.getX());
			MathUtil.getRotationY(b, rotation.getY());
			Matrix.multiply(c, a, b);
			MathUtil.getRotationZ(a, rotation.getZ());
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
		matrix.set(0, 3, vector.getX());
		matrix.set(1, 3, vector.getY());
		matrix.set(2, 3, vector.getZ());
	}
	public static Matrix getRotation(Vector3D rotation) {
		return MathUtil.getRotationX(rotation.getX())
				.multiply(MathUtil.getRotationY(rotation.getY()).multiply(MathUtil.getRotationZ(rotation.getZ())));
	}
	public static Supplier<Matrix> getRotationSupplier(Vector3D rotation) {
		Matrix a = new Matrix(4);
		Matrix b = new Matrix(4);
		Matrix c = new Matrix(4);
		return () -> {
			MathUtil.getRotationX(a, rotation.getX());
			MathUtil.getRotationY(b, rotation.getY());
			Matrix.multiply(c, a, b);
			MathUtil.getRotationZ(a, rotation.getZ());
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
		matrix.set(0, 0, vector.getX());
		matrix.set(1, 1, vector.getY());
		matrix.set(2, 2, vector.getZ());
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
}
