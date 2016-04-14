package lemon.engine.cad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lemon.engine.math.Vector;

public class IcoSphere implements Shape {
	private static final float VALUE = (float)((1f+Math.sqrt(5))/2f);
	private List<Vector> vertices;
	private List<TriangleIndices> faces;
	private List<Float> data;
	private List<Integer> indices;
	public IcoSphere(int refinement){
		vertices = new ArrayList<Vector>();
		faces = new ArrayList<TriangleIndices>();
		generate();
		refine(refinement);
		for(Vector vector: vertices){
			data.add(vector.getX());
			data.add(vector.getY());
			data.add(vector.getZ());
		}
		for(TriangleIndices face: faces){
			indices.add(face.getVertex());
			indices.add(face.getVertex2());
			indices.add(face.getVertex3());
		}
	}
	private void generate(){
		vertices.add(new Vector(-1, VALUE, 0));
		vertices.add(new Vector(1, VALUE, 0));
		vertices.add(new Vector(-1, -VALUE, 0));
		vertices.add(new Vector(1, -VALUE, 0));

		vertices.add(new Vector(0, -1, VALUE));
		vertices.add(new Vector(0, 1, VALUE));
		vertices.add(new Vector(0, -1, -VALUE));
		vertices.add(new Vector(0, 1, -VALUE));
		
		vertices.add(new Vector(VALUE, 0, -1));
		vertices.add(new Vector(VALUE, 0, 1));
		vertices.add(new Vector(-VALUE, 0, -1));
		vertices.add(new Vector(-VALUE, 0, 1));
		
		faces.add(new TriangleIndices(0, 11, 5));
		faces.add(new TriangleIndices(0, 5, 1));
		faces.add(new TriangleIndices(0, 1, 7));
		faces.add(new TriangleIndices(0, 7, 10));
		faces.add(new TriangleIndices(0, 10, 11));
		
		faces.add(new TriangleIndices(1, 5, 9));
		faces.add(new TriangleIndices(5, 11, 4));
		faces.add(new TriangleIndices(11, 10, 2));
		faces.add(new TriangleIndices(10, 7, 6));
		faces.add(new TriangleIndices(7, 1, 8));
		
		faces.add(new TriangleIndices(3, 9, 4));
		faces.add(new TriangleIndices(3, 4, 2));
		faces.add(new TriangleIndices(3, 2, 6));
		faces.add(new TriangleIndices(3, 6, 8));
		faces.add(new TriangleIndices(3, 8, 9));
		
		faces.add(new TriangleIndices(4, 9, 5));
		faces.add(new TriangleIndices(2, 4, 11));
		faces.add(new TriangleIndices(6, 2, 10));
		faces.add(new TriangleIndices(8, 6, 7));
		faces.add(new TriangleIndices(9, 8, 1));
	}
	private void refine(int refinement){
		
		if(refinement>0){
			refine(refinement-1);
		}
	}
	@Override
	public List<Float> getData() {
		return Collections.unmodifiableList(data);
	}
	@Override
	public List<Integer> getIndices() {
		return Collections.unmodifiableList(indices);
	}
}
