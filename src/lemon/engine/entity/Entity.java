package lemon.engine.entity;

import lemon.engine.math.Vector;

public class Entity {
	private Vector translation;
	private Vector rotation;
	private EntityType type;
	
	public Entity(EntityType type, Vector translation, Vector rotation){
		this.type = type;
		this.translation = translation;
		this.rotation = rotation;
	}
	public Vector getTranslation(){
		return translation;
	}
	public Vector getRotation(){
		return rotation;
	}
	public EntityType getType(){
		return type;
	}
}
