package lemon.evolution;

import lemon.engine.draw.CommonDrawables;
import lemon.engine.event.Observable;
import lemon.engine.frameBuffer.FrameBuffer;
import lemon.engine.render.Renderable;
import org.lwjgl.opengl.GL11;

public class ViewModel implements Renderable {
	private final FrameBuffer frameBuffer;
	private final Observable<Boolean> visible = new Observable<>(true);
	private final Renderable renderable;

	public ViewModel(int width, int height, Renderable renderable) {
		this.frameBuffer = new FrameBuffer(width, height);
		this.renderable = renderable;
	}

	public void setVisible(boolean visible) {
		this.visible.setValue(visible);
	}

	public Observable<Boolean> visible() {
		return visible;
	}

	@Override
	public void render() {
		frameBuffer.bind(frameBuffer -> {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			renderable.render();
			GL11.glDisable(GL11.GL_DEPTH_TEST);
		});
		// TODO: render to textured quad
		// CommonDrawables.TEXTURED_QUAD.draw();
	}
}
