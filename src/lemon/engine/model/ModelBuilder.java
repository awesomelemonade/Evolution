package lemon.engine.model;

import lemon.engine.math.Vector3D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public record ModelBuilder(List<Vector3D> vertices, List<Integer> indices) {
	public ModelBuilder() {
		this(new ArrayList<>(), new ArrayList<>());
	}
	public ModelBuilder addVertex(Vector3D vertex) {
		this.vertices.add(vertex);
		return this;
	}
	public ModelBuilder addVertices(Vector3D... vertices) {
		Collections.addAll(this.vertices, vertices);
		return this;
	}
	public ModelBuilder addIndices(int... indices) {
		for (int index : indices) {
			this.indices.add(index);
		}
		return this;
	}
	public <T> T build(BiFunction<int[], Vector3D[], T> constructor) {
		return constructor.apply(indices.stream().mapToInt(i -> i).toArray(),
				vertices.toArray(Vector3D.EMPTY_ARRAY));
	}
}
