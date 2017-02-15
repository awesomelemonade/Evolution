package lemon.engine.physics;

import lemon.engine.math.Vector3D;

public class CollisionEffect {
	private DynamicCollidable collidable;
	private Vector3D motion;
	public CollisionEffect(DynamicCollidable collidable){
		this.collidable = collidable;
		this.motion = new Vector3D(collidable.getVelocity());
	}
	public DynamicCollidable getCollidable(){
		return collidable;
	}
	public Vector3D getMotion(){
		return motion;
	}
	public boolean hasMotion(){
		return motion!=null;
	}
}
