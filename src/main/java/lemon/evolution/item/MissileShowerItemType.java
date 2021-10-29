package lemon.evolution.item;

import lemon.evolution.entity.MissileShowerEntity;
import lemon.evolution.world.ControllableEntity;

public enum MissileShowerItemType implements ItemType {
	INSTANCE;

	@Override
	public String getName() {
		return "Missile Shower";
	}

	@Override
	public void use(ControllableEntity player) {
		player.world().entities().add(new MissileShowerEntity(
				player.location().add(player.vectorDirection().multiply(0.95f)),
				player.vectorDirection().multiply(5f)
		));
	}

	@Override
	public String guiImagePath() {
		return "/res/inventory_icons/missile.png";
	}
}
