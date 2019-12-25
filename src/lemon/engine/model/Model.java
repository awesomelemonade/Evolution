package lemon.engine.model;

import lemon.engine.math.Vector3D;

public interface Model {
	public Vector3D[] getVertices();
	public int[] getIndices();
}
