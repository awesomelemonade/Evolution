package lemon.evolution.pool;

import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;

import java.util.function.Consumer;

public class MatrixPool extends ObjectPool<MatrixPool.PooledMatrix> {
	private static final MatrixPool instance = new MatrixPool();
	private MatrixPool() {
		super(PooledMatrix::new);
	}

	public static PooledMatrix ofEmpty() {
		return instance.borrowObject();
	}

	public static PooledMatrix of(Consumer<PooledMatrix> consumer) {
		PooledMatrix ret = instance.borrowObject();
		consumer.accept(ret);
		return ret;
	}

	public static PooledMatrix ofMultiplied(Matrix left, Matrix right) {
		return MatrixPool.of(matrix -> Matrix.multiply(matrix, left, right));
	}

	public static PooledMatrix ofTranslation(Vector3D vector) {
		return MatrixPool.of(matrix -> MathUtil.getTranslation(matrix, vector));
	}

	public static PooledMatrix ofTranslation(float x, float y, float z) {
		try (var vector = VectorPool.of(x, y, z)) {
			return MatrixPool.ofTranslation(vector);
		}
	}

	public static PooledMatrix ofScalar(Vector3D vector) {
		return MatrixPool.of(matrix -> MathUtil.getScalar(matrix, vector));
	}

	public static PooledMatrix ofScalar(float x, float y, float z) {
		try (var vector = VectorPool.of(x, y, z)) {
			return MatrixPool.ofScalar(vector);
		}
	}

	public static PooledMatrix ofRotationX(float x) {
		return MatrixPool.of(matrix -> MathUtil.getRotationX(matrix, x));
	}

	public static PooledMatrix ofRotationY(float y) {
		return MatrixPool.of(matrix -> MathUtil.getRotationY(matrix, y));
	}

	public static PooledMatrix ofRotationZ(float z) {
		return MatrixPool.of(matrix -> MathUtil.getRotationZ(matrix, z));
	}

	public static class PooledMatrix extends Matrix implements AutoCloseable {
		private PooledMatrix() {
			super(4);
		}
		@Override
		public void close() {
			instance.returnObject(this);
		}
	}
}
