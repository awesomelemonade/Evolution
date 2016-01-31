package lemon.engine.math;

public interface Castable<K, V extends Comparable<V>> {
	public V cast(K caster, V value);
}
