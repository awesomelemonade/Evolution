package lemon.evolution.ui.beta;

import lemon.engine.math.Box2D;
import lemon.engine.render.CommonRenderables;
import lemon.engine.render.Renderable;
import lemon.engine.toolbox.Color;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class UIButton extends AbstractUIChildComponent {
	private final Renderable renderable;

	public UIButton(UIComponent parent, Box2D box, Consumer<UIButton> eventHandler, Color color) {
		this(parent, box, eventHandler, () -> CommonRenderables.renderQuad2D(box, color));
	}

	public UIButton(UIComponent parent, Box2D box, Consumer<UIButton> eventHandler, Renderable renderable) {
		super(parent);
		this.renderable = renderable;
		disposables.add(input().mouseButtonEvent().add(event -> {
			if (visible() &&
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
		renderable.render();
	}
}
