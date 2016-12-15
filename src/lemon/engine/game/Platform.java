package lemon.engine.game;

import lemon.engine.control.UpdateEvent;
import lemon.engine.math.Vector;

public class Platform {
	private Vector center;
	public Platform(Vector center){
		this.center = center;
	}
	public Vector getPosition() {
		return center;
	}
	public Vector getVelocity() {
		return Vector.ZERO;
	}
}
