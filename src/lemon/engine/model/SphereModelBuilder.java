package lemon.engine.model;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import lemon.engine.math.Vector3D;

public class SphereModelBuilder<T extends Model> extends ModelBuilder<T> {
	private static final Vector3D[] OCTAHEDRON_VERTICES = new Vector3D[] { new Vector3D(0, -1, 0),
			new Vector3D(0, 1, 0), new Vector3D(-1, 0, -1), new Vector3D(-1, 0, 1), new Vector3D(1, 0, -1),
			new Vector3D(1, 0, 1) };
	private static final int[] OCTAHEDRON_INDICES = new int[] { 0, 2, 3, 0, 3, 5, 0, 5, 4, 0, 4, 2, 1, 2, 3, 1, 3, 5, 1,
			5, 4, 1, 4, 2 };

	public SphereModelBuilder(BiFunction<Vector3D[], int[], T> constructor, float radius, int iterations) {
		super(constructor);
		// add vertices and indices
		this.addVertices(OCTAHEDRON_VERTICES).addIndices(splitTriangles(OCTAHEDRON_INDICES, iterations));
		normalize(radius);
	}
	public int[] splitTriangles(int[] indices, int count) {
		if (count <= 0) {
			return indices;
		}
		int[] newIndices = new int[indices.length * 4];
		Map<Vector3D, Map<Vector3D, Integer>> newVertices = new HashMap<Vector3D, Map<Vector3D, Integer>>();
		for (int i = 0; i < indices.length; i += 3) {
			Vector3D a = this.getVertices().get(indices[i]);
			Vector3D b = this.getVertices().get(indices[i + 1]);
			Vector3D c = this.getVertices().get(indices[i + 2]);

			int index1 = addToMap(newVertices, a, b);
			int index2 = addToMap(newVertices, b, c);
			int index3 = addToMap(newVertices, c, a);

			newIndices[i * 4] = indices[i];
			newIndices[i * 4 + 1] = index1;
			newIndices[i * 4 + 2] = index3;

			newIndices[i * 4 + 3] = indices[i + 1];
			newIndices[i * 4 + 4] = index1;
			newIndices[i * 4 + 5] = index2;

			newIndices[i * 4 + 6] = indices[i + 2];
			newIndices[i * 4 + 7] = index2;
			newIndices[i * 4 + 8] = index3;

			newIndices[i * 4 + 9] = index1;
			newIndices[i * 4 + 10] = index2;
			newIndices[i * 4 + 11] = index3;
		}
		return splitTriangles(newIndices, count - 1);
	}
	private int addToMap(Map<Vector3D, Map<Vector3D, Integer>> vertices, Vector3D a, Vector3D b) {
		if (vertices.containsKey(a)) {
			if (vertices.get(a).containsKey(b)) {
				return vertices.get(a).get(b);
			}
		}
		if (vertices.containsKey(b)) {
			if (vertices.get(b).containsKey(a)) {
				return vertices.get(b).get(a);
			}
		}
		if (!vertices.containsKey(a)) {
			vertices.put(a, new HashMap<Vector3D, Integer>());
		}
		vertices.get(a).put(b, this.getVertices().size());
		this.addVertices(a.copy().average(b));
		return this.getVertices().size() - 1;
	}
	public void normalize(float radius) {
		for (Vector3D vertex : this.getVertices()) {
			vertex.scaleToLength(radius);
		}
	}
}
