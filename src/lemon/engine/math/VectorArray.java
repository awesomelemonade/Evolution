package lemon.engine.math;

public class VectorArray {
	private Vector[] array;
	public VectorArray(int size){
		array = new Vector[size];
		for(int i=0;i<size;++i){
			array[i] = new Vector();
		}
	}
	public void set(int index, Vector vector){
		if(vector==null){
			throw new NullPointerException("Vector cannot be null");
		}
		array[index] = vector;
	}
	public Vector get(int index){
		return array[index];
	}
}
