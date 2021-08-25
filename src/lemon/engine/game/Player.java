package lemon.engine.game;

import lemon.engine.math.Camera;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector3D;

public class Player {
	private static final float DELTA_MODIFIER = 0.000001f;
	private Camera camera;
	private Vector3D velocity;

	public Player(Projection projection) {
		camera = new Camera(projection);
		velocity = new Vector3D(0, 0, 0);
	}
	public void update(float delta) {
		camera.setPosition(camera.getPosition().add(velocity.multiply(delta * DELTA_MODIFIER)));
	}
	public Camera getCamera() {
		return camera;
	}
	public Vector3D getVectorDirection() {
		return new Vector3D(
				(float) (-(Math.sin(camera.getRotation().y())
						* Math.cos(camera.getRotation().x()))),
				(float) (Math.sin(camera.getRotation().x())),
				(float) (-(Math.cos(camera.getRotation().x())
						* Math.cos(camera.getRotation().y()))));
	}
	public Vector3D getPosition() {
		return camera.getPosition();
	}
	public Vector3D getVelocity() {
		return velocity;
	}
}
