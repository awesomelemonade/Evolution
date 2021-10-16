package lemon.evolution.ui.beta;

import lemon.engine.glfw.GLFWInput;
import lemon.engine.math.Box2D;
import lemon.engine.math.Vector2D;
import lemon.engine.render.Renderable;
import lemon.engine.toolbox.Color;
import lemon.evolution.UIMinimap;
import lemon.evolution.destructible.beta.TerrainRenderer;
import lemon.evolution.world.ControllableEntity;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class UIScreen extends AbstractUIComponent {
	public UIScreen(GLFWInput input) {
		super(input);
	}

	private <T extends UIComponent> T addComponent(T component) {
		children().add(component);
		return component;
	}

	public UIButton addButton(Box2D box, Color color, Consumer<UIButton> eventHandler) {
		return addComponent(new UIButton(this, box, color, eventHandler));
	}

	public UIWheel addWheel(Vector2D position, float radius, float value, Color color) {
		return addComponent(new UIWheel(this, position, radius, value, color));
	}

	public UICheckbox addCheckbox(Box2D box) {
		return addComponent(new UICheckbox(this, box));
	}

	public UIProgressBar addProgressBar(Box2D box, Supplier<Float> progressGetter) {
		return addComponent(new UIProgressBar(this, box, progressGetter));
	}

	public UIMinimap addMinimap(Box2D box, TerrainRenderer renderer, Supplier<ControllableEntity> entitySupplier) {
		return addComponent(new UIMinimap(this, box, renderer, entitySupplier));
	}

	@Override
	public void render() {
		if (isVisible()) {
			children().forEach(Renderable::render);
		}
	}
}
