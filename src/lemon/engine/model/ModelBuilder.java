package lemon.engine.model;

import lemon.engine.math.Vector3D;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class ModelBuilder {
	private List<Vector3D> vertices;
	private List<Integer> indices;

	public ModelBuilder() {
		vertices = new ArrayList<>();
		indices = new ArrayList<>();
	}
	public ModelBuilder addVertex(Vector3D vertex) {
		this.vertices.add(vertex);
		return this;
	}
	public ModelBuilder addVertices(Vector3D... vertices) {
		for (Vector3D vertex : vertices) {
			this.vertices.add(vertex);
		}
		return this;
	}
	public ModelBuilder addIndices(int... indices) {
		for (int index : indices) {
			this.indices.add(index);
		}
		return this;
	}
	public List<Vector3D> getVertices() {
		return vertices;
	}
	public List<Integer> getIndices() {
		return indices;
	}
	public <T> T build(BiFunction<int[], Vector3D[], T> constructor) {
		return constructor.apply(indices.stream().mapToInt(i -> i).toArray(),
				vertices.toArray(Vector3D.EMPTY_ARRAY));
	}
}
