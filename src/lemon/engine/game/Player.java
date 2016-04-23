package lemon.engine.game;

import lemon.engine.math.Camera;
import lemon.engine.math.Projection;

public class Player {
	private Camera camera;
	public Player(Projection projection){
		camera = new Camera(projection);
	}
	public Camera getCamera(){
		return camera;
	}
}
