package lemon.engine.frameBuffer;

import lemon.engine.toolbox.Disposable;
import org.lwjgl.opengl.GL30;

public class RenderBuffer implements Disposable {
	private final int id;

	public RenderBuffer() {
		id = GL30.glGenRenderbuffers();
	}

	public int getId() {
		return id;
	}

	@Override
	public void dispose() {
		GL30.glDeleteRenderbuffers(id);
	}
}
