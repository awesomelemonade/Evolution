package lemon.evolution.item;

import lemon.engine.game.Player;
import lemon.evolution.entity.RocketLauncherProjectile;

public enum RocketLauncherItemType implements ItemType {
	INSTANCE;
	@Override
	public String getName() {
		return "Rocket Launcher";
	}

	@Override
	public void use(Player player) {
		player.world().entities().add(new RocketLauncherProjectile(
				player.location().add(player.vectorDirection().multiply(0.5f)),
				player.vectorDirection().multiply(5f)
		));
	}
}
