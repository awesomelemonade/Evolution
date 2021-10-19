package lemon.evolution.item;

import lemon.engine.game.Player;
import lemon.engine.texture.Texture;
import lemon.evolution.entity.RocketLauncherProjectile;
import lemon.evolution.world.ControllableEntity;

public enum RocketLauncherItemType implements ItemType {
	INSTANCE;
	@Override
	public String getName() {
		return "Rocket Launcher";
	}

	private final String imagePath2D = "/res/China.png";

	@Override
	public void use(ControllableEntity player) {
		player.world().entities().add(new RocketLauncherProjectile(
				player.location().add(player.vectorDirection().multiply(0.5f)),
				player.vectorDirection().multiply(5f)
		));
	}

	public String guiImagePath() {
		return "/res/inventory_icons/China.png";
	}
}
