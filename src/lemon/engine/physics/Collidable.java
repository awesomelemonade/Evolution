package lemon.engine.physics;

public interface Collidable {
	public float getIntersection(Collidable effect);
	public void applyResponse();
}
