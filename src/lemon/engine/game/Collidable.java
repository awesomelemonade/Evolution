package lemon.engine.game;

import lemon.engine.math.Vector;

public interface Collidable {
	public Vector getPosition();
	public Vector getVelocity();
	public Vector collide(Collidable collidable);
	public Vector[] getCollisionPoints();
}
