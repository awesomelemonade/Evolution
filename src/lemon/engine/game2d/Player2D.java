package lemon.engine.game2d;

import lemon.engine.control.UpdateEvent;
import lemon.engine.math.Vector;
import lemon.engine.render.Renderable;
import lemon.engine.toolbox.Color;

public class Player2D implements Renderable {
	private static final float DELTA_MODIFIER = 0.0000001f;
	private Vector position;
	private Vector velocity;
	private Quad2D quad;
	public Player2D(Vector position, Vector velocity){
		quad = new Quad2D(new Box2D(0f, 0, 20f, 50f), new Color(1f, 0f, 0f));
		this.position = position;
		this.velocity = velocity;
	}
	public Vector update(UpdateEvent event){
		Vector position = new Vector(this.position);
		position.setX(position.getX()+velocity.getX()*event.getDelta()*DELTA_MODIFIER);
		position.setY(position.getY()+velocity.getY()*event.getDelta()*DELTA_MODIFIER);
		return position;
	}
	@Override
	public void render(){
		quad.render();
	}
	public void setPosition(Vector position){
		this.position = position;
	}
	public void setVelocity(Vector velocity){
		this.velocity = velocity;
	}
	public Vector getPosition(){
		return position;
	}
	public Vector getVelocity(){
		return velocity;
	}
}
