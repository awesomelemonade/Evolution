package lemon.evolution.ui.beta;

import lemon.engine.math.Box2D;
import lemon.engine.render.CommonRenderables;
import lemon.engine.toolbox.Color;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class UIButton extends AbstractUIComponent {
	private final Box2D box;
	private final Color color;

	public UIButton(UIComponent parent, Box2D box, Color color, Consumer<UIButton> eventHandler) {
		super(parent);
		this.box = box;
		this.color = color;
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
			CommonRenderables.renderQuad2D(box, color);
		}
	}
}
