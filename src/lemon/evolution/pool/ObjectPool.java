package lemon.evolution.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ObjectPool<T> {
	private final ThreadLocal<List<T>> lists;
	private final Supplier<T> constructor;

	public ObjectPool(Supplier<T> constructor) {
		this.lists = ThreadLocal.withInitial(ArrayList::new);
		this.constructor = constructor;
	}

	protected T borrowObject() {
		List<T> list = lists.get();
		if (list.isEmpty()) {
			return constructor.get();
		} else {
			T object = list.get(list.size() - 1);
			list.remove(list.size() - 1);
			return object;
		}
	}

	protected void returnObject(T object) {
		lists.get().add(object);
	}
}
