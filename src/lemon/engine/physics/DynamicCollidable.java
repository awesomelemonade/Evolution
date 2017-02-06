package lemon.engine.physics;

import lemon.engine.math.Vector3D;

public interface DynamicCollidable extends Collidable {
	public Vector3D getPosition();
	public Vector3D getVelocity();
}
