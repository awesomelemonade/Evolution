package lemon.engine.frameBuffer;

import org.lwjgl.opengl.GL30;

import lemon.engine.control.CleanUpEvent;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;

public class FrameBuffer implements Listener {
	private int id;
	public FrameBuffer(){
		id = GL30.glGenFramebuffers();
		EventManager.INSTANCE.registerListener(this);
	}
	public int getId(){
		return id;
	}
	@Subscribe
	public void cleanUp(CleanUpEvent event){
		GL30.glDeleteFramebuffers(id);
	}
}
