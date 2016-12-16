package lemon.engine.render;

import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;

import org.lwjgl.opengl.GL20;

public class UniformVariable {
	private int id;
	private String name;
	public UniformVariable(int id, String name){
		this.id = id;
		this.name = name;
	}
	public void loadInt(int value){
		GL20.glUniform1i(id, value);
	}
	public void loadFloat(float value){
		GL20.glUniform1f(id, value);
	}
	public void loadVector(Vector3D vector){
		GL20.glUniform3f(id, vector.getX(), vector.getY(), vector.getZ());
	}
	public void loadBoolean(boolean value){
		loadFloat(value?1:0);
	}
	public void loadMatrix(Matrix matrix){
		GL20.glUniformMatrix4fv(id, false, matrix.toFloatBuffer());
	}
	public int getId(){
		return id;
	}
	public String getName(){
		return name;
	}
}