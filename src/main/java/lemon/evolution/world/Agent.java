package lemon.evolution.world;

import lemon.engine.math.MutableVector3D;

public class Agent implements Entity {
	private final World world;
	private final MutableVector3D position;
	private final MutableVector3D velocity = MutableVector3D.ofZero();
	private final MutableVector3D force = MutableVector3D.ofZero();
	private final Inventory inventory = new Inventory();

	public Agent(Location location) {
		this.world = location.world();
		this.position = MutableVector3D.of(location.position());
	}

	@Override
	public World world() {
		return world;
	}

	@Override
	public MutableVector3D mutablePosition() {
		return position;
	}

	@Override
	public MutableVector3D mutableVelocity() {
		return velocity;
	}

	@Override
	public MutableVector3D mutableForce() {
		return force;
	}

	public Inventory inventory() {
		return inventory;
	}
}
