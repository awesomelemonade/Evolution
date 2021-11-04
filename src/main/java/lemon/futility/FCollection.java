package lemon.futility;

import java.util.Collection;
import java.util.stream.Collectors;

public abstract class FCollection<T> implements Collection<T> {
	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean result = false;
		for (var item : c) {
			if (add(item)) {
				result = true;
			}
		}
		return result;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		var set = c.stream().filter(this::contains).collect(Collectors.toSet());
		return this.removeIf(item -> !set.contains(item));
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean result = false;
		for (var item : c) {
			if (remove(item)) {
				result = true;
			}
		}
		return result;
	}
}
