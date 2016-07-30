package lemon.engine.game;

import org.lwjgl.glfw.GLFW;

import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.input.KeyEvent;
import lemon.engine.input.MouseButtonEvent;

public class StandardControls extends PlayerControls<Integer, Integer> implements Listener {
	public StandardControls(){
		super();
		EventManager.INSTANCE.registerListener(this);
	}
	@Subscribe
	public void onKey(KeyEvent event){
		if(event.getAction()==GLFW.GLFW_PRESS){
			this.setKeyState(event.getKey(), true);
		}
		if(event.getAction()==GLFW.GLFW_RELEASE){
			this.setKeyState(event.getKey(), false);
		}
	}
	@Subscribe
	public void onMouse(MouseButtonEvent event){
		if(event.getAction()==GLFW.GLFW_PRESS){
			this.setKeyState(event.getButton(), true);
		}
		if(event.getAction()==GLFW.GLFW_RELEASE){
			this.setKeyState(event.getButton(), false);
		}
	}
}
