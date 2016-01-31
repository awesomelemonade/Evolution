package lemon.engine.math;

public class Caster<K, T extends Castable<K, V>, V extends Comparable<V>> {
	private K caster;
	private T castable;
	private V value;
	public Caster(K caster, V startValue){
		this.caster = caster;
		this.castable = null;
		this.value = startValue;
	}
	public void simulate(T castable){
		V x = castable.cast(caster, value);
		if(value.compareTo(x)>0){
			this.castable = castable;
			value = x;
		}
	}
	public T getCastable(){
		return castable;
	}
	public V getValue(){
		return value;
	}
}
