package lemon.evolution.ui.beta;

import lemon.engine.glfw.GLFWInput;
import lemon.engine.math.Box2D;
import lemon.engine.math.Vector2D;
import lemon.engine.render.Renderable;
import lemon.engine.texture.Texture;
import lemon.engine.toolbox.Color;
import lemon.evolution.UIMinimap;
import lemon.evolution.world.ControllableEntity;
import lemon.evolution.world.Inventory;
import lemon.evolution.world.World;

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

	public UIMinimap addMinimap(Box2D box, World world, Supplier<ControllableEntity> entitySupplier) {
		return addComponent(new UIMinimap(this, box, world, entitySupplier));
	}

	public UIImage addImage(Box2D box, String path) {
		return addComponent(new UIImage(this, box, path));
	}

	public UIImage addImage(Box2D box, Texture texture) {
		return addComponent(new UIImage(this, box, texture));
	}

	public UIInventory addInventory(Inventory inventory) {
		return addComponent(new UIInventory(this, inventory));
	}

	@Override
	public void render() {
		if (isVisible()) {
			children().forEach(Renderable::render);
		}
	}
}
