package lemon.engine.model;

import java.util.HashMap;
import java.util.Map;

import lemon.engine.math.Vector3D;

public class SphereModelBuilder {
	private static final Vector3D[] OCTAHEDRON_VERTICES = new Vector3D[] { new Vector3D(0, -1, 0),
			new Vector3D(0, 1, 0), new Vector3D(-1, 0, -1), new Vector3D(-1, 0, 1), new Vector3D(1, 0, -1),
			new Vector3D(1, 0, 1) };
	private static final int[] OCTAHEDRON_INDICES = new int[] { 0, 2, 3, 0, 3, 5, 0, 5, 4, 0, 4, 2, 1, 2, 3, 1, 3, 5, 1,
			5, 4, 1, 4, 2 };

	public static ModelBuilder build(ModelBuilder builder, float radius, int iterations) {
		// add vertices and indices
		builder.addVertices(OCTAHEDRON_VERTICES).addIndices(splitTriangles(builder, OCTAHEDRON_INDICES, iterations));
		// normalize
		for (Vector3D vertex : builder.vertices()) {
			vertex.scaleToLength(radius);
		}
		return builder;
	}
	public static int[] splitTriangles(ModelBuilder builder, int[] indices, int count) {
		if (count <= 0) {
			return indices;
		}
		int[] newIndices = new int[indices.length * 4];
		Map<Vector3D, Map<Vector3D, Integer>> newVertices = new HashMap<Vector3D, Map<Vector3D, Integer>>();
		for (int i = 0; i < indices.length; i += 3) {
			Vector3D a = builder.vertices().get(indices[i]);
			Vector3D b = builder.vertices().get(indices[i + 1]);
			Vector3D c = builder.vertices().get(indices[i + 2]);

			int index1 = addToMap(builder, newVertices, a, b);
			int index2 = addToMap(builder, newVertices, b, c);
			int index3 = addToMap(builder, newVertices, c, a);

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
		return splitTriangles(builder, newIndices, count - 1);
	}
	private static int addToMap(ModelBuilder builder, Map<Vector3D, Map<Vector3D, Integer>> vertices, Vector3D a, Vector3D b) {
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
			vertices.put(a, new HashMap<>());
		}
		vertices.get(a).put(b, builder.vertices().size());
		builder.addVertex(a.copy().average(b));
		return builder.vertices().size() - 1;
	}
}
