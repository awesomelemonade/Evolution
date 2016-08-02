package lemon.engine.game;

import lemon.engine.control.UpdateEvent;
import lemon.engine.math.Vector;

public class Platform implements Collidable {
	private Vector center;
	public Platform(Vector center){
		this.center = center;
	}
	@Override
	public Vector getPosition() {
		return center;
	}
	@Override
	public Vector getVelocity() {
		return Vector.ZERO;
	}
	@Override
	public Vector collide(Collidable collidable, UpdateEvent event) {
		return collidable.getVelocity();
	}
	@Override
	public Vector[] getCollisionPoints() {
		return Vector.EMPTY_ARRAY;
	}
}
