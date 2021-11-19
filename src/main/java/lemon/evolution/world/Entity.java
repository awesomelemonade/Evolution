package lemon.evolution.world;

import lemon.engine.event.Event;
import lemon.engine.event.EventWith2;
import lemon.engine.math.MathUtil;
import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Vector3D;
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
	public default Vector3D vectorDirection() {
		return velocity().normalize();
	}
	public default Vector3D scalar() {
		return Vector3D.ONE;
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
	public EventWith2<Vector3D, Vector3D> onCollide();
	public default CollisionResponse getCollisionResponse() {
		return CollisionResponse.SLIDE;
	}
	public GroundWatcher groundWatcher();
	public EntityMeta meta();
	public default void setType(Object o) {
		meta().set("type", o);
	}
	public default <T> boolean isType(T type) {
		return meta().get("type", type.getClass()).map(type::equals).orElse(false);
	}

	public default void removeFromWorld() {
		world().entities().remove(this);
	}
}
