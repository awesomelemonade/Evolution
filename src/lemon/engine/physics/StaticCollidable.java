package lemon.engine.physics;

public interface StaticCollidable extends Collidable {
	public void applyResponse(DynamicCollidable collidable);
}
