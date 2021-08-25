package lemon.engine.render;

import lemon.engine.toolbox.Disposable;
import org.lwjgl.opengl.GL15;

import java.util.function.BiConsumer;

public record VertexBuffer(int id) implements Disposable {
	public VertexBuffer() {
		this(GL15.glGenBuffers());
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
	@Override
	public void dispose() {
		GL15.glDeleteBuffers(id);
	}
}
