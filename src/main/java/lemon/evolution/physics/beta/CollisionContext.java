package lemon.evolution.physics.beta;

import lemon.engine.math.MutableTriangle;
import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Triangle;
import lemon.engine.math.Vector3D;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface CollisionContext {
	public void checkCollision(Vector3D position, Vector3D velocity, Consumer<Triangle> checker);

	public default void collideWithWorld(MutableVector3D position, MutableVector3D velocity, MutableVector3D force,
										 Vector3D scalar, float dt, BiConsumer<Vector3D, Vector3D> onCollide, Supplier<CollisionResponse> responder) {
		var scalarSquared = scalar.multiply(scalar);
		var transformed = new MutableTriangle();
		position.divide(scalar);
		velocity.divide(scalar);
		force.divide(scalar);
		CollisionPacket.collideWithWorld((p, v) -> {
			var collision = new Collision();
			checkCollision(p.multiply(scalar), v.multiply(scalar), triangle -> {
				transformed.setAndDivideByScalar(triangle, scalar, scalarSquared);
				CollisionPacket.checkTriangle(p, v, transformed, collision);
			});
			return collision;
		}, position, velocity, force, dt, collision -> {
			onCollide.accept(collision.intersection().multiply(scalar), collision.negSlidePlaneNormal().multiply(scalar));
			return responder.get();
		});
		position.multiply(scalar);
		velocity.multiply(scalar);
		force.multiply(scalar);
	}
}
