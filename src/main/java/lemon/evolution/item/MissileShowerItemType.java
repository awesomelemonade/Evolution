package lemon.evolution.item;

import lemon.engine.game.Player;
import lemon.evolution.entity.MissileShowerEntity;

public enum MissileShowerItemType implements ItemType {
	INSTANCE;

	@Override
	public String getName() {
		return "Missile Shower";
	}

	@Override
	public void use(Player player) {
		player.world().entities().add(new MissileShowerEntity(
				player.location().add(player.vectorDirection().multiply(0.95f)),
				player.vectorDirection().multiply(5f)
		));
	}
}
