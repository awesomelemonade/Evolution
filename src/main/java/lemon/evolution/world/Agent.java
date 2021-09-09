package lemon.evolution.world;

import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Vector3D;

public class Agent implements Entity {
	private final MutableVector3D position;
	private final MutableVector3D velocity;
	private final Location location;
	private Inventory inventory;

	public Agent(World world, Vector3D position) {
		this.position = MutableVector3D.of(position);
		this.velocity = MutableVector3D.ofZero();
		this.location = new Location(world, this.position.asImmutable());
	}

	public Location location() {
		return location;
	}

	@Override
	public MutableVector3D mutablePosition() {
		return position;
	}

	@Override
	public MutableVector3D mutableVelocity() {
		return velocity;
	}
}
