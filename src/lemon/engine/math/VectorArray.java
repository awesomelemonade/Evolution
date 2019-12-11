package lemon.engine.math;

import java.util.Arrays;

public class VectorArray {
	private Vector3D[] array;

	public VectorArray(int size) {
		array = new Vector3D[size];
		for (int i = 0; i < size; ++i) {
			array[i] = new Vector3D();
		}
	}
	public VectorArray(Vector3D... array) {
		this.array = array;
	}
	public void set(int index, Vector3D vector) {
		if (vector == null) {
			throw new NullPointerException("Vector cannot be null");
		}
		array[index] = vector;
	}
	public Vector3D get(int index) {
		return array[index];
	}
	public int getLength() {
		return array.length;
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof VectorArray) {
			VectorArray x = (VectorArray) o;
			if (this.getLength() != x.getLength()) {
				return false;
			}
			for (int i = 0; i < this.getLength(); i++) {
				if (!this.get(i).equals(x.get(i))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	@Override
	public String toString(){
		return Arrays.toString(array);
	}
}
