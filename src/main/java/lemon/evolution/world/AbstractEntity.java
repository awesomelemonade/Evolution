package lemon.evolution.world;

import lemon.engine.event.Event;
import lemon.engine.event.EventWith2;
import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Lazy;

public abstract class AbstractEntity implements Entity {
	private final Event onUpdate = new Event();
	private final EventWith2<Vector3D, Vector3D> onCollide = new EventWith2<>();
	private final Lazy<GroundWatcher> groundWatcher = new Lazy<>(() -> new GroundWatcher(this));
	private final World world;
	private final MutableVector3D position;
	private final MutableVector3D velocity;
	private final MutableVector3D force;
	private final Vector3D scalar;
	private final EntityMeta meta = new EntityMeta();

	public AbstractEntity(Location location, Vector3D velocity) {
		this(location, velocity, Vector3D.ONE);
	}

	public AbstractEntity(Location location, Vector3D velocity, float scalar) {
		this(location, velocity, Vector3D.of(scalar, scalar, scalar));
	}

	public AbstractEntity(Location location, Vector3D velocity, Vector3D scalar) {
		this.world = location.world();
		this.position = MutableVector3D.of(location.position());
		this.velocity = MutableVector3D.of(velocity);
		this.force = MutableVector3D.ofZero();
		this.scalar = scalar;
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
	public EventWith2<Vector3D, Vector3D> onCollide() {
		return onCollide;
	}

	@Override
	public GroundWatcher groundWatcher() {
		return groundWatcher.get();
	}

	@Override
	public Vector3D scalar() {
		return scalar;
	}

	@Override
	public EntityMeta meta() {
		return meta;
	}
}
