package lemon.evolution.ui.beta;

import lemon.engine.math.Box2D;
import lemon.engine.render.CommonRenderables;
import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureData;
import lemon.engine.toolbox.Toolbox;
import org.lwjgl.opengl.GL11;

public class UIImage extends AbstractUIComponent {
	private final Box2D box;
	private final Texture texture;

	public UIImage(UIComponent parent, Box2D box, String path) {
		this(parent, box, new Texture());
		texture.load(new TextureData(Toolbox.readImage(path).orElseThrow()));
	}

	public UIImage(UIComponent parent, Box2D box, Texture texture) {
		super(parent);
		this.box = box;
		this.texture = texture;
	}

	@Override
	public void render() {
		if (isVisible()) {
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			CommonRenderables.renderTexturedQuad2D(box, texture);
			GL11.glDisable(GL11.GL_BLEND);
		}
	}
}
