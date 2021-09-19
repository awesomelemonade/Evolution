package lemon.futility;

import lemon.engine.toolbox.TaskQueue;

import java.util.Iterator;

public class FBufferedSetWithEvents<T> extends FSetWithEvents<T> {
	private final TaskQueue operations = TaskQueue.ofSingleThreaded();
	@Override
	public boolean add(T item) {
		operations.add(() -> super.add(item));
		return true; // Assumed
	}

	@Override
	public boolean remove(Object o) {
		operations.add(() -> super.remove(o));
		return true; // Assumed
	}

	@Override
	public void clear() {
		operations.add(super::clear);
	}

	@Override
	public Iterator<T> iterator() {
		var backingIterator = backingSet().iterator();
		return new Iterator<>() {
			T item = null;
			@Override
			public boolean hasNext() {
				return backingIterator.hasNext();
			}

			@Override
			public T next() {
				item = backingIterator.next();
				return item;
			}

			@Override
			public void remove() {
				operations.add(() -> FBufferedSetWithEvents.super.remove(item));
			}
		};
	}

	public void flush() {
		operations.run();
	}
}
