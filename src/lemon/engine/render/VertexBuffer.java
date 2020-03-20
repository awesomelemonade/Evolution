package lemon.engine.render;

import lemon.engine.control.CleanUpEvent;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import org.lwjgl.opengl.GL15;

import java.util.function.BiConsumer;

public class VertexBuffer implements Listener {
	private int id;

	public VertexBuffer() {
		id = GL15.glGenBuffers();
		EventManager.INSTANCE.registerListener(this);
	}
	public int getId() {
		return id;
	}

	public void bind(int target, BiConsumer<Integer, VertexBuffer> consumer) {
		bind(target, consumer, true);
	}
	public void bind(int target, BiConsumer<Integer, VertexBuffer> consumer, boolean unbind) {
		GL15.glBindBuffer(target, id);
		consumer.accept(target, this);
		if (unbind) {
			GL15.glBindBuffer(target, 0);
		}
	}
	@Subscribe
	public void cleanUp(CleanUpEvent event) {
		GL15.glDeleteBuffers(id);
	}
}
