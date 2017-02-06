package lemon.engine.physics;

import lemon.engine.math.Vector3D;

public class CollisionEffect {
	private Vector3D position;
	private Vector3D velocity;
	private Vector3D motion;
	public CollisionEffect(Vector3D position, Vector3D velocity, Vector3D motion){
		this.position = position;
		this.velocity = velocity;
		this.motion = motion;
	}
	public CollisionEffect(Vector3D position, Vector3D velocity){
		this(position, velocity, null);
	}
	public Vector3D getPosition(){
		return position;
	}
	public Vector3D getVelocity(){
		return velocity;
	}
	public Vector3D getMotion(){
		return motion;
	}
	public boolean hasMotion(){
		return motion!=null;
	}
}
