package lemon.engine.game;

import lemon.engine.control.UpdateEvent;
import lemon.engine.math.Camera;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector;

public class Player {
	private static final float SPEED_MODIFIER = 0.000001f;
	private static final float FRICTION = 0.95f;
	private Camera camera;
	private Vector velocity;
	public Player(Projection projection){
		camera = new Camera(projection);
		velocity = new Vector();
	}
	public void update(UpdateEvent event){
		camera.getPosition().setX(camera.getPosition().getX()+velocity.getX()*event.getDelta()*SPEED_MODIFIER);
		camera.getPosition().setY(camera.getPosition().getY()+velocity.getY()*event.getDelta()*SPEED_MODIFIER);
		camera.getPosition().setZ(camera.getPosition().getZ()+velocity.getZ()*event.getDelta()*SPEED_MODIFIER);
		velocity.setX(velocity.getX()*FRICTION);
		velocity.setY(velocity.getY()*FRICTION);
		velocity.setZ(velocity.getZ()*FRICTION);
	}
	public Camera getCamera(){
		return camera;
	}
	public Vector getVelocity(){
		return velocity;
	}
}
