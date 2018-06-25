package lemon.engine.frameBuffer;

import org.lwjgl.opengl.GL30;

import lemon.engine.control.CleanUpEvent;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;

public class RenderBuffer implements Listener {
	private int id;

	public RenderBuffer() {
		id = GL30.glGenRenderbuffers();
		EventManager.INSTANCE.registerListener(this);
	}
	public int getId() {
		return id;
	}
	@Subscribe
	public void cleanUp(CleanUpEvent event) {
		GL30.glDeleteRenderbuffers(id);
	}
}
