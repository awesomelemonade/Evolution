package lemon.engine.game;

import lemon.engine.math.Vector;

public class Platform implements Collidable {
	private Vector center;
	@Override
	public Vector getPosition() {
		return center;
	}
	@Override
	public Vector getVelocity() {
		return Vector.ZERO;
	}
	@Override
	public Vector collide(Collidable collidable) {
		return collidable.getVelocity();
	}
	@Override
	public Vector[] getCollisionPoints() {
		return Vector.EMPTY_ARRAY;
	}
}
