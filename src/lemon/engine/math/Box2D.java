package lemon.engine.math;

public class Box2D extends Vector<Box2D> {
	public Box2D(float x, float y, float width, float height) {
		super(Box2D.class, Box2D::new, x, y, width, height);
	}
	public Box2D(Box2D box) {
		this(box.getX(), box.getY(), box.getWidth(), box.getHeight());
	}
	public boolean intersect(float x, float y) {
		return x > this.getX() && x < this.getX() + this.getWidth() && y > this.getY()
				&& y < this.getY() + this.getHeight();
	}
	public void setX(float x) {
		super.set(0, x);
	}
	public float getX() {
		return super.get(0);
	}
	public void setY(float y) {
		super.set(1, y);
	}
	public float getY() {
		return super.get(1);
	}
	public void setWidth(float width) {
		super.set(2, width);
	}
	public float getWidth() {
		return super.get(2);
	}
	public void setHeight(float height) {
		super.set(3, height);
	}
	public float getHeight() {
		return super.get(3);
	}
}
