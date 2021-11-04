package lemon.engine.math;

import java.nio.FloatBuffer;

public interface FloatData {
	public int numDimensions();
	public void putInBuffer(FloatBuffer buffer);
	public void putInArray(float[] array);

	public static FloatData of(float[] array, int offset, int size) {
		return new FloatData() {
			@Override
			public int numDimensions() {
				return size;
			}

			@Override
			public void putInBuffer(FloatBuffer buffer) {
				buffer.put(array, offset, size);
			}

			@Override
			public void putInArray(float[] a) {
				System.arraycopy(array, offset, a, 0, size);
			}
		};
	}

	public static FloatData of(float[] array) {
		return new FloatData() {
			@Override
			public int numDimensions() {
				return array.length;
			}

			@Override
			public void putInBuffer(FloatBuffer buffer) {
				buffer.put(array);
			}

			@Override
			public void putInArray(float[] a) {
				System.arraycopy(array, 0, a, 0, array.length);
			}
		};
	}
}
