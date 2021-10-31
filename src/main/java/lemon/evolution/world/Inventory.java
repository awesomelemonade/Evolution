package lemon.evolution.world;

import lemon.engine.event.Observable;
import lemon.evolution.item.ItemType;
import lemon.futility.FMultisetWithEvents;

import java.util.Optional;

public class Inventory {
	private final FMultisetWithEvents<ItemType> items = new FMultisetWithEvents<>();
	private final Observable<Optional<ItemType>> currentItem = new Observable<>(Optional.empty());

	public FMultisetWithEvents<ItemType> items() {
		return items;
	}

	public void addItem(ItemType item) {
		items.add(item);
	}

	public void addAndSetCurrentItem(ItemType item) {
		items.add(item);
		currentItem.setValue(Optional.of(item));
	}

	public void setCurrentItem(ItemType item) {
		if (items.contains(item)) {
			currentItem.setValue(Optional.of(item));
		} else {
			throw new IllegalStateException();
		}
	}

	public void clearCurrentItem() {
		currentItem.setValue(Optional.empty());
	}

	public Optional<ItemType> currentItem() {
		return currentItem.getValue();
	}

	public void useCurrentItem() {
		this.currentItem.getValue().ifPresent(item -> {
			items.remove(item);
		});
	}
}
