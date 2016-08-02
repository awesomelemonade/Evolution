package lemon.engine.game;

import lemon.engine.control.UpdateEvent;
import lemon.engine.math.Vector;

public interface Collidable {
	public Vector getPosition();
	public Vector getVelocity();
	public Vector collide(Collidable collidable, UpdateEvent event);
	public Vector[] getCollisionPoints();
}
