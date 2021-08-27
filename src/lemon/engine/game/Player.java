package lemon.engine.game;

import lemon.engine.math.Camera;
import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector3D;

public record Player(Camera camera, MutableVector3D mutableVelocity) {
	private static final float DELTA_MODIFIER = 0.000001f;
	public Player(Projection projection) {
		this(new Camera(projection), MutableVector3D.ofZero());
	}
	public void update(float delta) {
		camera.mutablePosition().add(velocity().multiply(delta * DELTA_MODIFIER));
	}
	public Vector3D getVectorDirection() {
		var rotation = camera.rotation();
		return new Vector3D(
				(float) (-(Math.sin(rotation.y())
						* Math.cos(rotation.x()))),
				(float) (Math.sin(rotation.x())),
				(float) (-(Math.cos(rotation.x())
						* Math.cos(rotation.y()))));
	}
	public MutableVector3D mutablePosition() {
		return camera.mutablePosition();
	}
	public Vector3D position() {
		return camera.position();
	}
	public MutableVector3D mutableRotation() {
		return camera.mutableRotation();
	}
	public Vector3D rotation() {
		return camera.rotation();
	}
	public Vector3D velocity() {
		return mutableVelocity.toImmutable();
	}
}
