package lemon.evolution.item;

import lemon.evolution.world.ControllableEntity;

public interface ItemType {
	public String getName();
	public default String getDescription() {
		return getName();
	}
	public void use(ControllableEntity player);
}
