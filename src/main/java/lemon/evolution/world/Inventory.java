package lemon.evolution.world;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import lemon.evolution.item.ItemType;

public class Inventory {
	private Multiset<ItemType> items = HashMultiset.create();
}
