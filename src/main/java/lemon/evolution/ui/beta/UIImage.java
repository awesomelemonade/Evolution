package lemon.evolution.ui.beta;

import lemon.engine.math.Box2D;
import lemon.engine.render.CommonRenderables;
import lemon.engine.texture.Texture;
import lemon.engine.texture.TextureData;
import lemon.engine.toolbox.Toolbox;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import javax.swing.*;
import java.util.function.Consumer;

public class UIImage extends AbstractUIComponent {
	private final Box2D box;
	private final Texture texture;

	public UIImage(UIComponent parent, Box2D box, Texture texture) {
		super(parent);
		this.box = box;
		this.texture = texture;
	}

	public UIImage(UIComponent parent, Box2D box, String path) {
		this(parent, box, new Texture());
		texture.load(new TextureData(Toolbox.readImage(path).orElseThrow(), true));
	}

	// constructor for adding an event on click
	public UIImage(UIComponent parent, Box2D box, String path, Consumer<UIImage> eventHandler) {
		this(parent, box, path);
		disposables.add(input().mouseButtonEvent().add(event -> {
			if (isVisible() &&
					event.button() == GLFW.GLFW_MOUSE_BUTTON_1 &&
					event.action() == GLFW.GLFW_RELEASE) {
				event.glfwWindow().pollMouse((mouseX, mouseY) -> {
					if (box.intersect(mouseX, mouseY)) {
						eventHandler.accept(this);
					}
				});
			}
		}));
	}

	// constructor for adding an event on click with Texture input
	public UIImage(UIComponent parent, Box2D box, Texture texture, Consumer<UIImage> eventHandler) {
		this(parent, box, texture);
		disposables.add(input().mouseButtonEvent().add(event -> {
			if (isVisible() &&
					event.button() == GLFW.GLFW_MOUSE_BUTTON_1 &&
					event.action() == GLFW.GLFW_RELEASE) {
				event.glfwWindow().pollMouse((mouseX, mouseY) -> {
					if (box.intersect(mouseX, mouseY)) {
						eventHandler.accept(this);
					}
				});
			}
		}));
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
