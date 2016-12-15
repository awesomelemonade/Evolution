package lemon.engine.entity;

import java.util.HashMap;
import java.util.Map;

import lemon.engine.math.Vector;

public class SphereModelBuilder extends TriangularIndexedModel.Builder {
	private static final Vector[] OCTAHEDRON_VERTICES = new Vector[]{
			new Vector(0, -1, 0),
			new Vector(0, 1, 0),
			new Vector(-1, 0, -1),
			new Vector(-1, 0, 1),
			new Vector(1, 0, -1),
			new Vector(1, 0, 1)
	};
	private static final int[] OCTAHEDRON_INDICES = new int[]{
			0, 2, 3,
			0, 3, 5,
			0, 5, 4,
			0, 4, 2,
			1, 2, 3,
			1, 3, 5,
			1, 5, 4,
			1, 4, 2
	};
	public SphereModelBuilder(float radius, int iterations){
		super();
		//add vertices and indices
		this.addVertices(OCTAHEDRON_VERTICES)
			.addIndices(splitTriangles(OCTAHEDRON_INDICES, iterations));
		normalize(radius);
	}
	public int[] splitTriangles(int[] indices, int count){
		if(count<=0){
			return indices;
		}
		int[] newIndices = new int[indices.length*4];
		Map<Vector, Map<Vector, Integer>> newVertices = new HashMap<Vector, Map<Vector, Integer>>();
		for(int i=0;i<indices.length;i+=3){
			Vector a = this.getVertices().get(indices[i]);
			Vector b = this.getVertices().get(indices[i+1]);
			Vector c = this.getVertices().get(indices[i+2]);
			
			int index1 = addToMap(newVertices, a, b, a.average(b));
			int index2 = addToMap(newVertices, b, c, b.average(c));
			int index3 = addToMap(newVertices, c, a, c.average(a));
			
			newIndices[i*4] = indices[i];
			newIndices[i*4+1] = index1;
			newIndices[i*4+2] = index3;
			
			newIndices[i*4+3] = indices[i+1];
			newIndices[i*4+4] = index1;
			newIndices[i*4+5] = index2;

			newIndices[i*4+6] = indices[i+2];
			newIndices[i*4+7] = index2;
			newIndices[i*4+8] = index3;
			
			newIndices[i*4+9] = index1;
			newIndices[i*4+10] = index2;
			newIndices[i*4+11] = index3;
		}
		return splitTriangles(newIndices, count-1);
	}
	private int addToMap(Map<Vector, Map<Vector, Integer>> vertices, Vector a, Vector b, Vector vertex){
		if(vertices.containsKey(a)){
			if(vertices.get(a).containsKey(b)){
				return vertices.get(a).get(b);
			}
		}
		if(vertices.containsKey(b)){
			if(vertices.get(b).containsKey(a)){
				return vertices.get(b).get(a);
			}
		}
		if(!vertices.containsKey(a)){
			vertices.put(a, new HashMap<Vector, Integer>());
		}
		vertices.get(a).put(b, this.getVertices().size());
		this.addVertices(vertex);
		return this.getVertices().size()-1;
	}
	public void normalize(float radius){
		for(Vector vertex: this.getVertices()){
			vertex.set(vertex.multiply(radius).divide(vertex.absoluteValue()));
		}
	}
}