package lemon.evolution.world;

import lemon.engine.event.Observable;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.item.ItemType;
import lemon.futility.FListWithEvents;
import lemon.futility.FMultisetWithEvents;

import java.util.Optional;

public class Inventory implements Disposable {
	private final Disposables disposables = new Disposables();
	private final FMultisetWithEvents<ItemType> items = new FMultisetWithEvents<>();
	private final FListWithEvents<ItemType> itemsList = FListWithEvents.fromMultiset(items, disposables::add);
	private final Observable<Optional<ItemType>> currentItem = new Observable<>(Optional.empty());

	public Inventory() {
		disposables.add(items.onFallToZero(item -> {
			currentItem.getValue().ifPresent(current -> {
				if (current.equals(item)) {
					clearCurrentItem();
				}
			});
		}));
	}

	public FMultisetWithEvents<ItemType> items() {
		return items;
	}

	public FListWithEvents<ItemType> itemsList() {
		return itemsList;
	}

	public void addItem(ItemType item) {
		items.add(item);
	}

	public void addItems(ItemType item, int amount) {
		items.add(item, amount);
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

	public Observable<Optional<ItemType>> observableCurrentItem() {
		return currentItem;
	}

	public Optional<ItemType> currentItem() {
		return currentItem.getValue();
	}

	public void removeOneOfCurrentItem() {
		this.currentItem.getValue().ifPresent(items::remove);
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
