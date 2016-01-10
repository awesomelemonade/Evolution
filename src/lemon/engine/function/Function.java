package lemon.engine.function;

public interface Function<K, V> {
	public V resolve(K key);
}
