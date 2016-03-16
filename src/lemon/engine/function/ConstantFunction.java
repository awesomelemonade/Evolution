package lemon.engine.function;

public class ConstantFunction<K, V> implements Function<K, V> {
	private V value;
	public ConstantFunction(V value){
		this.value = value;
	}
	@Override
	public V resolve(K key) {
		return value;
	}
}
