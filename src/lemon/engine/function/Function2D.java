package lemon.engine.function;

public interface Function2D<K, V> {
	public V resolve(K key, K key2);
}
