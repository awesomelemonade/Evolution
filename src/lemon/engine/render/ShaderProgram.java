package lemon.engine.render;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import lemon.engine.texture.Texture;
import lemon.engine.toolbox.Color;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import lemon.engine.control.CleanUpEvent;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;

public class ShaderProgram implements Listener {
	private int id;
	private Map<String, UniformVariable> uniformVariables;

	public ShaderProgram(int[] indices, String[] names, Shader... shaders) {
		if (indices.length != names.length) {
			throw new IllegalArgumentException(
					"Indices and Name Arrays have different size: " + indices.length + " - " + names.length);
		}
		id = GL20.glCreateProgram();
		if (shaders != null) {
			for (Shader shader : shaders) {
				GL20.glAttachShader(id, shader.getId());
			}
		}
		for (int i = 0; i < indices.length; ++i) {
			GL20.glBindAttribLocation(id, indices[i], names[i]);
		}
		GL20.glLinkProgram(id);
		if (GL20.glGetProgrami(id, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
			throw new IllegalStateException("Shader Program Link Fail: " + GL20.glGetProgramInfoLog(id));
		}
		if (shaders != null) {
			for (Shader shader : shaders) {
				GL20.glDetachShader(id, shader.getId());
				GL20.glDeleteShader(shader.getId());
			}
		}
		GL20.glValidateProgram(id);
		if (GL20.glGetProgrami(id, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
			throw new IllegalStateException("Shader Program Validation Fail: " + GL20.glGetProgramInfoLog(id));
		}
		uniformVariables = new HashMap<String, UniformVariable>();
		EventManager.INSTANCE.registerListener(this);
	}
	public UniformVariable getUniformVariable(String name) {
		if (!uniformVariables.containsKey(name)) {
			uniformVariables.put(name, new UniformVariable(GL20.glGetUniformLocation(id, name), name));
		}
		return uniformVariables.get(name);
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
	@Subscribe
	public void cleanUp(CleanUpEvent event) {
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
