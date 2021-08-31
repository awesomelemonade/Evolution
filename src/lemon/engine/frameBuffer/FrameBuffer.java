package lemon.engine.frameBuffer;

import lemon.engine.toolbox.Disposable;
import org.lwjgl.opengl.GL30;

import java.util.function.Consumer;

public class FrameBuffer implements Disposable {
	private final int id;

	public FrameBuffer() {
		id = GL30.glGenFramebuffers();
	}

	public int getId() {
		return id;
	}

	@Override
	public void dispose() {
		GL30.glDeleteFramebuffers(id);
	}

	public void bind(Consumer<FrameBuffer> consumer) {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id);
		consumer.accept(this);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}
}
