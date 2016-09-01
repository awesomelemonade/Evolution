package lemon.engine.function;

public interface Function2D<K, K2, V> {
	public V resolve(K key, K2 key2);
}
