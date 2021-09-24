package lemon.engine.game;

import lemon.engine.math.Camera;
import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector3D;
import lemon.evolution.world.ControllableEntity;
import lemon.evolution.world.Location;
import lemon.evolution.world.World;

public class Player implements ControllableEntity {
	private static final float DELTA_MODIFIER = 0.000001f;

	private final Camera camera;
	private final World world;
	private final MutableVector3D position;
	private final MutableVector3D velocity;
	private final MutableVector3D force;
	private final MutableVector3D rotation;

	public Player(Location location, Projection projection) {
		this.world = location.world();
		this.position = MutableVector3D.of(location.position());
		this.velocity = MutableVector3D.ofZero();
		this.force = MutableVector3D.ofZero();
		this.rotation = MutableVector3D.ofZero();
		this.camera = new Camera(position, rotation, projection);
	}

	public void update(float delta) {
		camera.mutablePosition().add(velocity().multiply(delta * DELTA_MODIFIER));
	}

	public Vector3D getVectorDirection() {
		var rotation = camera.rotation();
		return Vector3D.of(
				(float) (-(Math.sin(rotation.y())
						* Math.cos(rotation.x()))),
				(float) (Math.sin(rotation.x())),
				(float) (-(Math.cos(rotation.x())
						* Math.cos(rotation.y()))));
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

	public Camera camera() {
		return camera;
	}
}
