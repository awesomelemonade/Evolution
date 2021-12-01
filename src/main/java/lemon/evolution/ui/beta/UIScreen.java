package lemon.evolution.ui.beta;

import lemon.engine.font.Font;
import lemon.engine.game.Player;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.math.Box2D;
import lemon.engine.math.Vector2D;
import lemon.engine.render.Renderable;
import lemon.engine.texture.Texture;
import lemon.engine.toolbox.Color;
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

	public UIProgressBar addProgressBar(Box2D box, Supplier<Float> progressGetter, UIProgressBar.ProgressDirection direction) {
		return addComponent(new UIProgressBar(this, box, progressGetter, direction));
	}

	public UIMinimap addMinimap(Box2D box, World world, Supplier<Player> entitySupplier) {
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

	public UIText addText(String text, Vector2D position, float scale, Color color) {
		return addComponent(new UIText(this, text, position, scale, color));
	}

	public UIText addCenteredText(Font font, String text, Vector2D position, float scale, Color color) {
		return addComponent(UIText.ofCentered(this, font, text, position, scale, color));
	}

	public UIPlayerInfo addPlayerInfo(Box2D box, Player player) {
		var level = ((int) (Math.random() * 9)) + 1;
		return addPlayerInfo(box, player.name(), "Lvl. " + level, player.team().color(), player::healthAsPercentage);
	}

	public UIPlayerInfo addPlayerInfo(Box2D box, String name, String info, Color color, Supplier<Float> healthGetter) {
		return addComponent(new UIPlayerInfo(this, box, name, info, color, healthGetter));
	}

	@Override
	public void render() {
		if (isVisible()) {
			children().forEach(Renderable::render);
		}
	}
}
