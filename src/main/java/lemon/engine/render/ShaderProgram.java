package lemon.engine.render;

import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ShaderProgram implements Disposable {
	private final int id;
	private final Map<String, UniformVariable> uniformVariables = new HashMap<>();

	public ShaderProgram(int[] indices, String[] names, Shader... shaders) {
		if (indices.length != names.length) {
			throw new IllegalArgumentException(
					"Indices and Name Arrays have different size: " + indices.length + " - " + names.length);
		}
		id = GL20.glCreateProgram();
		for (Shader shader : shaders) {
			GL20.glAttachShader(id, shader.id());
		}
		for (int i = 0; i < indices.length; ++i) {
			GL20.glBindAttribLocation(id, indices[i], names[i]);
		}
		GL20.glLinkProgram(id);
		if (GL20.glGetProgrami(id, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
			throw new IllegalStateException("Shader Program Link Fail: " + GL20.glGetProgramInfoLog(id));
		}
		for (Shader shader : shaders) {
			GL20.glDetachShader(id, shader.id());
			shader.dispose();
		}
		GL20.glValidateProgram(id);
		if (GL20.glGetProgrami(id, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
			throw new IllegalStateException("Shader Program Validation Fail: " + GL20.glGetProgramInfoLog(id));
		}
	}

	public UniformVariable getUniformVariable(String name) {
		return uniformVariables.computeIfAbsent(name, n -> new UniformVariable(GL20.glGetUniformLocation(id, n), n));
	}

	public void loadInt(String name, int value) {
		this.getUniformVariable(name).loadInt(value);
	}

	public void loadFloat(String name, float value) {
		this.getUniformVariable(name).loadFloat(value);
	}

	public void loadVector(String name, Vector3D vector) {
		this.getUniformVariable(name).loadVector(vector);
	}

	public void loadColor3f(Color color) {
		this.getUniformVariable("color").loadColor3f(color);
	}

	public void loadColor3f(String name, Color color) {
		this.getUniformVariable(name).loadColor3f(color);
	}

	public void loadColor4f(Color color) {
		this.getUniformVariable("color").loadColor4f(color);
	}

	public void loadColor4f(String name, Color color) {
		this.getUniformVariable(name).loadColor4f(color);
	}

	public void loadBoolean(String name, boolean value) {
		this.getUniformVariable(name).loadBoolean(value);
	}

	public void loadMatrix(String name, Matrix matrix) {
		this.getUniformVariable(name).loadMatrix(matrix);
	}

	public void loadMatrix(MatrixType type, Matrix matrix) {
		this.loadMatrix(type.getUniformVariableName(), matrix);
	}

	@Override
	public void dispose() {
		GL20.glDeleteProgram(id);
	}

	public int getId() {
		return id;
	}

	public void use(Consumer<ShaderProgram> consumer) {
		GL20.glUseProgram(this.getId());
		consumer.accept(this);
		GL20.glUseProgram(0);
	}
}
