package lemon.engine.game;

import lemon.engine.control.UpdateEvent;
import lemon.engine.math.Camera;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector;

public class Player {
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
	public Vector getVectorDirection(){
		return new Vector((float)(-(Math.sin(Math.toRadians(camera.getRotation().getY()))*Math.cos(Math.toRadians(camera.getRotation().getX())))),
				(float)(Math.sin(Math.toRadians(camera.getRotation().getX()))),
						(float)(-(Math.cos(Math.toRadians(camera.getRotation().getX()))*Math.cos(Math.toRadians(camera.getRotation().getY())))));
	}
	public Vector getPosition(){
		return camera.getPosition();
	}
	public Vector getVelocity(){
		return velocity;
	}
}
