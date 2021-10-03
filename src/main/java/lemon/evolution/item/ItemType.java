package lemon.evolution.item;

import lemon.engine.game.Player;

public interface ItemType {
	public String getName();
	public default String getDescription() {
		return getName();
	}
	public void use(Player player);
}
