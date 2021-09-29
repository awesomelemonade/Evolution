package lemon.evolution.world;

import lemon.engine.event.Event;
import lemon.engine.event.EventWith;
import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Lazy;
import lemon.evolution.physics.beta.Collision;

public abstract class AbstractEntity implements Entity {
	private final Event onUpdate = new Event();
	private final EventWith<Collision> onCollide = new EventWith<>();
	private final Lazy<GroundWatcher> groundWatcher = new Lazy<>(() -> new GroundWatcher(this));
	private final World world;
	private final MutableVector3D position;
	private final MutableVector3D velocity;
	private final MutableVector3D force;
	public AbstractEntity(Location location, Vector3D velocity) {
		this.world = location.world();
		this.position = MutableVector3D.of(location.position());
		this.velocity = MutableVector3D.of(velocity);
		this.force = MutableVector3D.ofZero();
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

	@Override
	public Event onUpdate() {
		return onUpdate;
	}

	@Override
	public EventWith<Collision> onCollide() {
		return onCollide;
	}

	@Override
	public GroundWatcher groundWatcher() {
		return groundWatcher.get();
	}
}
