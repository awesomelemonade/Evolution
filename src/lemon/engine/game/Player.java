package lemon.engine.game;

import lemon.engine.control.UpdateEvent;
import lemon.engine.math.Camera;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector;

public class Player implements Collidable {
	private static final float DELTA_MODIFIER = 0.000001f;
	private Camera camera;
	private Vector velocity;
	public Player(Projection projection){
		camera = new Camera(projection);
		velocity = new Vector();
	}
	public void update(UpdateEvent event){
		camera.getPosition().setX(camera.getPosition().getX()+velocity.getX()*event.getDelta()*DELTA_MODIFIER);
		camera.getPosition().setY(camera.getPosition().getY()+velocity.getY()*event.getDelta()*DELTA_MODIFIER);
		camera.getPosition().setZ(camera.getPosition().getZ()+velocity.getZ()*event.getDelta()*DELTA_MODIFIER);
	}
	public Camera getCamera(){
		return camera;
	}
	@Override
	public Vector getPosition(){
		return camera.getPosition();
	}
	@Override
	public Vector getVelocity(){
		return velocity;
	}
	@Override
	public Vector collide(Collidable collidable, UpdateEvent event) {
		return collidable.getVelocity();
	}
	@Override
	public Vector[] getCollisionPoints() {
		return new Vector[]{camera.getPosition()};
	}
}
