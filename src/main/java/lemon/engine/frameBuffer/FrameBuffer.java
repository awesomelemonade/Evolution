package lemon.engine.frameBuffer;

import lemon.engine.math.Box2D;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.GLState;
import org.lwjgl.opengl.GL30;

import java.util.function.Consumer;

public class FrameBuffer implements Disposable {
	private final int id;
	private final Box2D viewport;

	public FrameBuffer(int width, int height) {
		this(new Box2D(0, 0, width, height));
	}

	public FrameBuffer(Box2D viewport) {
		this.id = GL30.glGenFramebuffers();
		this.viewport = viewport;
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
		GLState.pushViewport(viewport);
		consumer.accept(this);
		GLState.popViewport();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}
}
