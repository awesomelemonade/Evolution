package lemon.evolution.world;

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
	public MutableVector3D mutablePosition();
	public MutableVector3D mutableVelocity();
	public MutableVector3D mutableForce();
	public default Vector3D getEnvironmentalForce() {
		return world().getEnvironmentalForce();
	}
	public default Location location() {
		return new Location(world(), position());
	}
	public default CollisionResponse onCollide(Collision collision) {
		return CollisionResponse.SLIDE;
	}
}
