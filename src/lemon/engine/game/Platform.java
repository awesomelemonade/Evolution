package lemon.engine.game;

import lemon.engine.math.Vector3D;

public class Platform {
	private Vector3D center;
	public Platform(Vector3D center){
		this.center = center;
	}
	public Vector3D getPosition() {
		return center;
	}
	public Vector3D getVelocity() {
		return Vector3D.ZERO;
	}
}
