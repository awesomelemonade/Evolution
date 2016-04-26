package lemon.engine.game;

import lemon.engine.control.UpdateEvent;
import lemon.engine.math.Camera;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector;

public class Player {
	private Camera camera;
	private Vector velocity;
	public Player(Projection projection){
		camera = new Camera(projection);
	}
	public void update(UpdateEvent event){
		
	}
	public Camera getCamera(){
		return camera;
	}
}
