package lemon.evolution.physics.beta;

import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Triangle;
import lemon.engine.math.Vector3D;

import java.util.function.Consumer;
import java.util.function.Function;

public interface CollisionContext {
	public void checkCollision(Vector3D position, Vector3D velocity, Consumer<Triangle> checker);

	public default void collideWithWorld(MutableVector3D position, MutableVector3D velocity, MutableVector3D force,
										 float dt, CollisionResponse response) {
		collideWithWorld(position, velocity, force, dt, c -> response);
	}
	public default void collideWithWorld(MutableVector3D position, MutableVector3D velocity, MutableVector3D force,
										 float dt, Function<Collision, CollisionResponse> responder) {
		CollisionPacket.collideWithWorld((p, v) -> {
			var collision = new Collision();
			checkCollision(p, v, triangle -> CollisionPacket.checkTriangle(p, v, triangle, collision));
			return collision;
		}, position, velocity, force, dt, responder);
	}
}
