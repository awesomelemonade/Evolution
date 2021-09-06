package lemon.engine.render;

import lemon.engine.toolbox.Disposable;
import org.lwjgl.opengl.GL30;

import java.util.function.Consumer;

public record VertexArray(int id) implements Disposable {
	public VertexArray() {
		this(GL30.glGenVertexArrays());
	}

	@Override
	public void dispose() {
		GL30.glDeleteVertexArrays(id);
	}

	public void bind(Consumer<VertexArray> consumer) {
		GL30.glBindVertexArray(id);
		consumer.accept(this);
		GL30.glBindVertexArray(0);
	}
}
