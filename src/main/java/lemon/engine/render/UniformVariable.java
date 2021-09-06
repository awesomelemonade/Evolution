package lemon.engine.render;

import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Color;
import org.lwjgl.opengl.GL20;

public class UniformVariable {
	private final int id;
	private final String name;

	public UniformVariable(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public void loadInt(int value) {
		GL20.glUniform1i(id, value);
	}

	public void loadFloat(float value) {
		GL20.glUniform1f(id, value);
	}

	public void loadVector(Vector3D vector) {
		GL20.glUniform3f(id, vector.x(), vector.y(), vector.z());
	}

	public void loadColor4f(Color color) {
		GL20.glUniform4f(id, color.red(), color.green(), color.blue(), color.alpha());
	}

	public void loadColor3f(Color color) {
		GL20.glUniform3f(id, color.red(), color.green(), color.blue());
	}

	public void loadBoolean(boolean value) {
		loadFloat(value ? 1 : 0);
	}

	public void loadMatrix(Matrix matrix) {
		GL20.glUniformMatrix4fv(id, false, matrix.toFloatBuffer());
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}