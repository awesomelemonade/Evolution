package lemon.evolution.item;

import lemon.engine.texture.Texture;
import lemon.evolution.world.ControllableEntity;

public interface ItemType {
	public String getName();
	public default String getDescription() {
		return getName();
	}
	public void use(ControllableEntity player);
	public default boolean isWeapon() {
		return true;
	}
	public String guiImagePath();
}
