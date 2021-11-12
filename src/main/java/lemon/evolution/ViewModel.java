package lemon.evolution;

import lemon.engine.event.Observable;
import lemon.engine.frameBuffer.FrameBuffer;
import lemon.engine.math.Box2D;
import lemon.engine.render.CommonRenderables;
import lemon.engine.render.Renderable;
import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureBank;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;

public class ViewModel implements Renderable, Disposable {
	private final Disposables disposables = new Disposables();
	private final Box2D renderBox;
	private final FrameBuffer frameBuffer;
	private final Observable<Boolean> visible = new Observable<>(true);
	private final Renderable renderable;

	public ViewModel(int width, int height, Renderable renderable) {
		this.renderBox = new Box2D(0, 0, width, height);
		this.frameBuffer = disposables.add(new FrameBuffer(width, height));
		this.renderable = renderable;
		frameBuffer.bind(frameBuffer -> {
			GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
			Texture colorTexture = disposables.add(new Texture());
			TextureBank.VIEWMODEL_COLOR.bind(() -> {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture.id());
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA,
						GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, colorTexture.id(), 0);
			});
			Texture depthTexture = disposables.add(new Texture());
			TextureBank.VIEWMODEL_DEPTH.bind(() -> {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture.id());
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, width, height, 0,
						GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
				GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, depthTexture.id(), 0);
			});
		});
	}

	public void setVisible(boolean visible) {
		this.visible.setValue(visible);
	}

	public Observable<Boolean> visible() {
		return visible;
	}

	@Override
	public void render() {
		if (visible.getValue()) {
			frameBuffer.bind(frameBuffer -> {
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
				renderable.render();
			});
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			CommonRenderables.renderTexturedQuad2D(renderBox, TextureBank.VIEWMODEL_COLOR);
			GL11.glDisable(GL11.GL_BLEND);
		}
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
