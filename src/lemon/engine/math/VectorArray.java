package lemon.engine.math;

public class VectorArray {
	private Vector3D[] array;
	public VectorArray(int size){
		array = new Vector3D[size];
		for(int i=0;i<size;++i){
			array[i] = new Vector3D();
		}
	}
	public void set(int index, Vector3D vector){
		if(vector==null){
			throw new NullPointerException("Vector cannot be null");
		}
		array[index] = vector;
	}
	public Vector3D get(int index){
		return array[index];
	}
}
