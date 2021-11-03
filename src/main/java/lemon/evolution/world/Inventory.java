package lemon.evolution.world;

import lemon.evolution.item.ItemType;
import lemon.futility.FMultisetWithEvents;

public class Inventory {
	private final FMultisetWithEvents<ItemType> items = new FMultisetWithEvents<>();

	public FMultisetWithEvents<ItemType> items() {
		return items;
	}
}
