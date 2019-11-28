package lemon.engine.render;

import org.lwjgl.opengl.GL15;

import java.util.function.BiConsumer;

public class VertexBuffer {
	private int id;

	public VertexBuffer() {
		id = GL15.glGenBuffers();
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
}
