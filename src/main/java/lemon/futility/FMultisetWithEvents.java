package lemon.futility;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.errorprone.annotations.CheckReturnValue;
import lemon.engine.event.EventWith;
import lemon.engine.toolbox.Disposable;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

public class FMultisetWithEvents<T> extends FCollection<T> implements Multiset<T> {
	private final Multiset<T> backingSet = HashMultiset.create();
	private final EventWith<T> onRaiseAboveZero = new EventWith<>();
	private final EventWith<T> onFallToZero = new EventWith<>();

	@Override
	public int size() {
		return backingSet.size();
	}

	@Override
	public boolean isEmpty() {
		return backingSet.isEmpty();
	}

	@Override
	public int count(@Nullable Object o) {
		return backingSet.count(o);
	}

	@Override
	public int add(@Nullable T t, int i) {
		var before = backingSet.add(t, i);
		if (before == 0) {
			onRaiseAboveZero.callListeners(t);
		}
		return before;
	}

	@Override
	public boolean add(T t) {
		add(t, 1);
		return true;
	}

	@Override
	public int remove(@Nullable Object o, int i) {
		var ret = backingSet.remove(o, i);
		if (ret == i) {
			onFallToZero.callListeners((T) o);
		}
		return ret;
	}

	@Override
	public boolean remove(@Nullable Object o) {
		var ret = backingSet.remove(o);
		if (ret && !contains(o)) {
			onFallToZero.callListeners((T) o);
		}
		return ret;
	}

	@Override
	public int setCount(T t, int i) {
		return backingSet.setCount(t, i);
	}

	@Override
	public boolean setCount(T t, int i, int i1) {
		return backingSet.setCount(t, i, i1);
	}

	@Override
	public Set<T> elementSet() {
		// Does not support removal yet
		return Collections.unmodifiableSet(backingSet.elementSet());
	}

	@Override
	public Set<Entry<T>> entrySet() {
		// Does not support removal yet
		return Collections.unmodifiableSet(backingSet.entrySet());
	}

	@Override
	public Iterator<T> iterator() {
		var backingIterator = backingSet.iterator();
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
				backingIterator.remove();
				if (!contains(item)) {
					onFallToZero.callListeners(item);
				}
			}
		};
	}

	@Override
	public Object[] toArray() {
		return backingSet.toArray();
	}

	@Override
	public <T1> T1[] toArray(T1[] a) {
		return backingSet.toArray(a);
	}

	@Override
	public boolean contains(@Nullable Object o) {
		return backingSet.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		return backingSet.containsAll(collection);
	}

	@Override
	public void clear() {
		var copy = Set.copyOf(backingSet.elementSet());
		backingSet.clear();
		copy.forEach(onFallToZero::callListeners);
	}

	public EventWith<T> onRaiseAboveZero() {
		return onRaiseAboveZero;
	}

	public EventWith<T> onFallToZero() {
		return onFallToZero;
	}

	@CheckReturnValue
	public Disposable onRaiseAboveZero(Consumer<? super T> listener) {
		return onRaiseAboveZero.add(listener);
	}

	@CheckReturnValue
	public Disposable onFallToZero(Consumer<? super T> listener) {
		return onFallToZero.add(listener);
	}
}
