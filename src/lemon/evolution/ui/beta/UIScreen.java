package lemon.evolution.ui.beta;

import lemon.engine.glfw.GLFWInput;
import lemon.engine.math.Box2D;
import lemon.engine.render.Renderable;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UIScreen implements Renderable, Disposable {
	private final List<UIComponent> components = new ArrayList<>();
	private final Disposables disposables = new Disposables();
	private final GLFWInput input;
	public UIScreen(GLFWInput input) {
		this.input = input;
	}
	public UIButton addButton(Box2D box, Color color, Consumer<UIButton> eventHandler) {
		UIButton button = new UIButton(box, color, eventHandler);
		disposables.add(button.registerInputEvents(input));
		components.add(button);
		return button;
	}
	@Override
	public void render() {
		components.forEach(Renderable::render);
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
