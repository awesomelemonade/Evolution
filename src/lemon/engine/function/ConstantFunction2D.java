package lemon.engine.function;

public class ConstantFunction2D<K, V> implements Function2D<K, V> {
	private V value;
	public ConstantFunction2D(V value){
		this.value = value;
	}
	@Override
	public V resolve(K key, K key2) {
		return value;
	}
}
