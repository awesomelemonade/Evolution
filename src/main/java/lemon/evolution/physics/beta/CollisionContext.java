package lemon.evolution.physics.beta;

import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Vector3D;

public interface CollisionContext {
	public void checkCollision(Vector3D position, Vector3D velocity, Collision collision);

	public default void collideAndSlide(MutableVector3D position, MutableVector3D velocity, Vector3D force, float dt) {
		CollisionPacket.collideAndSlide((p, v) -> {
			var collision = new Collision(Float.MAX_VALUE, null);
			checkCollision(p, v, collision);
			return collision;
		}, position, velocity, force, dt);
	}

	public default boolean collideAndCheck(MutableVector3D position, MutableVector3D velocity, Vector3D force, float dt) {
		return CollisionPacket.collideAndCheck((p, v) -> {
			var collision = new Collision(Float.MAX_VALUE, null);
			checkCollision(p, v, collision);
			return collision;
		}, position, velocity, force, dt);
	}
}
