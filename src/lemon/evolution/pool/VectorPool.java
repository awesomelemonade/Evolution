package lemon.evolution.pool;

import lemon.engine.math.Vector3D;

import java.util.function.Consumer;

public class VectorPool extends ObjectPool<VectorPool.PooledVector3D> {
	private static final VectorPool instance = new VectorPool();
	private VectorPool() {
		super(PooledVector3D::new);
	}

	public static PooledVector3D ofEmpty() {
		return instance.borrowObject();
	}

	public static PooledVector3D of(Vector3D vector, Consumer<PooledVector3D> consumer) {
		PooledVector3D ret = instance.borrowObject();
		ret.set(vector);
		consumer.accept(ret);
		return ret;
	}

	public static PooledVector3D of(Consumer<PooledVector3D> consumer) {
		PooledVector3D ret = instance.borrowObject();
		consumer.accept(ret);
		return ret;
	}

	public static PooledVector3D of(Vector3D vector) {
		return VectorPool.of(v -> v.set(vector));
	}

	public static PooledVector3D of(float x, float y, float z) {
		return VectorPool.of(v -> v.set(x, y, z));
	}

	public static class PooledVector3D extends Vector3D implements AutoCloseable {
		private PooledVector3D() {}
		@Override
		public void close() {
			instance.returnObject(this);
		}
	}
}
