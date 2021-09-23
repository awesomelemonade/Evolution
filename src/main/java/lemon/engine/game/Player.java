package lemon.engine.game;

import lemon.engine.math.Camera;
import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector3D;
import lemon.evolution.world.Entity;
import lemon.evolution.world.Location;
import lemon.evolution.world.World;

public class Player implements Entity {
	private static final float DELTA_MODIFIER = 0.000001f;

	private final Camera camera;
	private final World world;
	private final MutableVector3D position;
	private final MutableVector3D velocity;
	private final MutableVector3D force;

	public Player(Location location, Projection projection) {
		this.world = location.world();
		this.position = MutableVector3D.of(location.position());
		this.velocity = MutableVector3D.ofZero();
		this.force = MutableVector3D.ofZero();
		this.camera = new Camera(position, MutableVector3D.ofZero(), projection);
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

	public MutableVector3D mutableRotation() {
		return camera.mutableRotation();
	}

	public Vector3D rotation() {
		return camera.rotation();
	}
}
