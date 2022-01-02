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

	public float centerX() {
		return x + (width / 2f);
	}

	public float centerY() {
		return y + (height / 2f);
	}

	public static Box2D ofInner(Box2D outer, float padding) {
		return new Box2D(outer.x() + padding, outer.y() + padding, outer.width() - 2 * padding, outer.height() - 2 * padding);
	}

	public static Box2D ofInner(Box2D outer, float paddingLeft, float paddingRight, float paddingUp, float paddingDown) {
		var width = outer.width() - paddingLeft - paddingRight;
		var height = outer.height() - paddingUp - paddingDown;
		return new Box2D(outer.x() + paddingLeft, outer.y() + paddingDown, width, height);
	}

	public static Box2D ofUpperBox(Box2D box, float percentage) {
		var height = box.height() * percentage;
		return new Box2D(box.x(), box.y() + (box.height() - height), box.width(), height);
	}

	public static Box2D ofLowerBox(Box2D box, float percentage) {
		var height = box.height() * percentage;
		return new Box2D(box.x(), box.y(), box.width(), height);
	}

	public static Box2D ofVerticalBox(Box2D box, float middlePercentage, float heightPercentage) {
		var halfHeightPercentage = heightPercentage / 2.0f;
		var lowerPaddingHeight = box.height() * (middlePercentage - halfHeightPercentage);
		var upperPaddingHeight = box.height() * (1.0f - (middlePercentage + halfHeightPercentage));
		return new Box2D(box.x(), box.y() + lowerPaddingHeight, box.width(), box.height() - lowerPaddingHeight - upperPaddingHeight);
	}

	public static Box2D ofLeftBox(Box2D box, float percentage) {
		var width = box.width() * percentage;
		return new Box2D(box.x(), box.y(), width, box.height());
	}

	public static Box2D ofRightBox(Box2D box, float percentage) {
		var width = box.width() * percentage;
		return new Box2D(box.x() + (box.width() - width), box.y(), width, box.height());
	}
}
