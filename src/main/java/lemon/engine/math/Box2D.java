package lemon.engine.math;

import java.nio.FloatBuffer;

public record Box2D(float x, float y, float width, float height) implements FloatData {
	private static final int NUM_DIMENSIONS = 4;
	public Box2D(Box2D box) {
		this(box.x, box.y, box.width, box.height);
	}

	public boolean intersect(float x, float y) {
		return x > this.x() && x < this.x() + this.width() && y > this.y()
				&& y < this.y() + this.height();
	}

	@Override
	public int numDimensions() {
		return NUM_DIMENSIONS;
	}

	@Override
	public void putInBuffer(FloatBuffer buffer) {
		buffer.put(x);
		buffer.put(y);
		buffer.put(width);
		buffer.put(height);
	}

	@Override
	public void putInArray(float[] array) {
		array[0] = x;
		array[1] = y;
		array[2] = width;
		array[3] = height;
	}

	public static Box2D ofInner(Box2D outer, float padding) {
		return new Box2D(outer.x() + padding, outer.y() + padding, outer.width() - 2 * padding, outer.height() - 2 * padding);
	}
}
