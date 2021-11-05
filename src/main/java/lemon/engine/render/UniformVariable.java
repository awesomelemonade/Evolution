package lemon.engine.render;

import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;
import lemon.engine.math.Vector4D;
import lemon.engine.toolbox.Color;
import org.lwjgl.opengl.GL20;

public record UniformVariable(int id, String name) {
	public void loadInt(int value) {
		GL20.glUniform1i(id, value);
	}

	public void loadFloat(float value) {
		GL20.glUniform1f(id, value);
	}

	public void loadVector(Vector3D vector) {
		GL20.glUniform3f(id, vector.x(), vector.y(), vector.z());
	}

	public void loadVector(Vector4D vector) {
		GL20.glUniform4f(id, vector.x(), vector.y(), vector.z(), vector.w());
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
}