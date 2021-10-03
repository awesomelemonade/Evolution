package lemon.engine.game;

import lemon.engine.event.Event;
import lemon.engine.event.EventWith;
import lemon.engine.event.Observable;
import lemon.engine.math.Camera;
import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Projection;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.physics.beta.Collision;
import lemon.evolution.physics.beta.CollisionResponse;
import lemon.evolution.world.ControllableEntity;
import lemon.evolution.world.GroundWatcher;
import lemon.evolution.world.Location;
import lemon.evolution.world.World;

public class Player implements ControllableEntity {
	private final Disposables disposables = new Disposables();
	private final String name;
	private final Camera camera;
	private final World world;
	private final MutableVector3D position;
	private final MutableVector3D velocity;
	private final MutableVector3D force;
	private final MutableVector3D rotation;
	private final Event onUpdate = new Event();
	private final EventWith<Collision> onCollide = new EventWith<>();
	private final GroundWatcher groundWatcher = disposables.add(new GroundWatcher(this));
	private final Observable<Boolean> alive;

	public Player(String name, Location location, Projection projection) {
		this.name = name;
		this.world = location.world();
		this.position = MutableVector3D.of(location.position());
		this.velocity = MutableVector3D.ofZero();
		this.force = MutableVector3D.ofZero();
		this.rotation = MutableVector3D.ofZero();
		this.camera = new Camera(position, rotation, projection);
		disposables.add(onUpdate.add(() -> {
			if (position.y() < -250f) {
				world.entities().remove(this);
			}
		}));
		this.alive = world.entities().observableContains(this, disposables::add);
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
	public CollisionResponse getCollisionResponse() {
		return CollisionResponse.SLIDE;
	}

	@Override
	public GroundWatcher groundWatcher() {
		return groundWatcher;
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
	public MutableVector3D mutableRotation() {
		return rotation;
	}

	public String name() {
		return name;
	}

	public Camera camera() {
		return camera;
	}

	public Observable<Boolean> alive() {
		return alive;
	}
}
