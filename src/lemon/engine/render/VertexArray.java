package lemon.engine.render;

import lemon.engine.control.CleanUpEvent;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;

import java.util.function.Consumer;

import org.lwjgl.opengl.GL30;

public class VertexArray implements Listener {
	private int id;

	public VertexArray() {
		id = GL30.glGenVertexArrays();
		EventManager.INSTANCE.registerListener(this);
	}
	public int getId() {
		return id;
	}
	@Subscribe
	public void cleanUp(CleanUpEvent event) {
		GL30.glDeleteVertexArrays(id);
	}
	public void bind(Consumer<VertexArray> consumer) {
		GL30.glBindVertexArray(id);
		consumer.accept(this);
		GL30.glBindVertexArray(0);
	}
}
