package lemon.engine.cad;

public class TriangleIndices {
	private int vertex;
	private int vertex2;
	private int vertex3;
	public TriangleIndices(int vertex, int vertex2, int vertex3){
		this.vertex = vertex;
		this.vertex2 = vertex2;
		this.vertex3 = vertex3;
	}
	public int getVertex(){
		return vertex;
	}
	public int getVertex2(){
		return vertex2;
	}
	public int getVertex3(){
		return vertex3;
	}
}
