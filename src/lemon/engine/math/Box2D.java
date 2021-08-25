package lemon.engine.math;

import lemon.engine.toolbox.Lazy;

public record Box2D(float x, float y, float width, float height, Lazy<float[]> dataArray) implements VectorData {
	public Box2D(float x, float y, float width, float height) {
		this(x, y, width, height, Lazy.of(() -> new float[] {x, y, width, height}));
	}
	public Box2D(Box2D box) {
		this(box.x, box.y, box.width, box.height, box.dataArray);
	}
	public boolean intersect(float x, float y) {
		return x > this.x() && x < this.x() + this.width() && y > this.y()
				&& y < this.y() + this.height();
	}
}
