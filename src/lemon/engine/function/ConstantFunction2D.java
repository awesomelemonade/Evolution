package lemon.engine.function;

public class ConstantFunction2D<K, K2, V> implements Function2D<K, K2, V> {
	private V value;
	public ConstantFunction2D(V value){
		this.value = value;
	}
	@Override
	public V resolve(K key, K2 key2) {
		return value;
	}
}
