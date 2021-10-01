package lemon.evolution.world;

import lemon.engine.event.Event;
import lemon.engine.event.EventWith;
import lemon.engine.math.MathUtil;
import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Vector3D;
import lemon.evolution.physics.beta.Collision;
import lemon.evolution.physics.beta.CollisionResponse;

public interface Entity {
	public World world();
	public default Vector3D position() {
		return mutablePosition().asImmutable();
	}
	public default Vector3D velocity() {
		return mutableVelocity().asImmutable();
	}
	public default Vector3D force() {
		return mutableForce().asImmutable();
	}
	public default Vector3D rotation() {
		return velocity();
	}
	public default Vector3D vectorDirection() {
		return MathUtil.getVectorDirection(rotation());
	}
	public MutableVector3D mutablePosition();
	public MutableVector3D mutableVelocity();
	public MutableVector3D mutableForce();
	public default Vector3D getEnvironmentalForce() {
		return world().getEnvironmentalForce(this);
	}
	public default Location location() {
		return new Location(world(), position());
	}
	public Event onUpdate();
	public EventWith<Collision> onCollide();
	public default CollisionResponse getCollisionResponse() {
		return CollisionResponse.SLIDE;
	}
	public GroundWatcher groundWatcher();

	public default void removeFromWorld() {
		world().entities().remove(this);
	}
}
