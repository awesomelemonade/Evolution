package lemon.engine.frameBuffer;

import org.lwjgl.opengl.GL30;

import lemon.engine.control.CleanUpEvent;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;

import java.util.function.Consumer;

public class FrameBuffer implements Listener {
	private int id;

	public FrameBuffer() {
		id = GL30.glGenFramebuffers();
		EventManager.INSTANCE.registerListener(this);
	}
	public int getId() {
		return id;
	}
	@Subscribe
	public void cleanUp(CleanUpEvent event) {
		GL30.glDeleteFramebuffers(id);
	}

	public void bind(Consumer<FrameBuffer> consumer) {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id);
		consumer.accept(this);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}
}
