package lemon.engine.render;

import com.google.common.collect.ImmutableMap;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;
import lemon.engine.texture.TextureBank;
import lemon.engine.math.Vector4D;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface ShaderProgram extends Disposable {
	public static ShaderProgram of(String[] attributesInOrder, Consumer<ShaderProgram> setDefaultUniformVariables, Shader... shaders) {
		var map = IntStream.range(0, attributesInOrder.length).boxed().collect(Collectors.toMap(Function.identity(), i -> attributesInOrder[i]));
		return of(map, setDefaultUniformVariables, shaders);
	}

	public static ShaderProgram of(Map<Integer, String> attributes, Consumer<ShaderProgram> setDefaultUniformVariables, Shader... shaders) {
		return new Impl(attributes, setDefaultUniformVariables, shaders);
	}

	class Impl implements ShaderProgram {
		private final int id;
		private final ImmutableMap<String, UniformVariable> uniformVariables;
		public Impl(Map<Integer, String> attributes, Consumer<ShaderProgram> setDefaultUniformVariables, Shader... shaders) {
			id = GL20.glCreateProgram();
			for (Shader shader : shaders) {
				GL20.glAttachShader(id, shader.id());
			}
			attributes.forEach((index, name) -> GL20.glBindAttribLocation(id, index, name));
			GL20.glLinkProgram(id);
			if (GL20.glGetProgrami(id, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
				throw new IllegalStateException("Shader Program Link Fail: " + GL20.glGetProgramInfoLog(id));
			}
			for (Shader shader : shaders) {
				GL20.glDetachShader(id, shader.id());
				shader.dispose();
			}
			var builder = ImmutableMap.<String, UniformVariable>builder();
			this.use(self -> {
				setDefaultUniformVariables.accept(new ShaderProgram() {
					@Override
					public int id() {
						return self.id();
					}

					@Override
					public UniformVariable getUniformVariable(String name) {
						var uniformVariable = new UniformVariable(GL20.glGetUniformLocation(id, name), name);
						builder.put(name, uniformVariable);
						return uniformVariable;
					}

					@Override
					public void dispose() {
						self.dispose();
					}
				});
			});
			uniformVariables = builder.build();
		}

		@Override
		public int id() {
			return id;
		}

		@Override
		public UniformVariable getUniformVariable(String name) {
			return uniformVariables.get(name);
		}

		@Override
		public void dispose() {
			GL20.glDeleteProgram(id);
		}
	}

	public int id();
	public UniformVariable getUniformVariable(String name);
	public default void loadInt(String name, int value) {
		this.getUniformVariable(name).loadInt(value);
	}
	public default void loadSampler(String name, TextureBank textureBank) {
		this.loadInt(name, textureBank.id());
	}
	public default void loadFloat(String name, float value) {
		this.getUniformVariable(name).loadFloat(value);
	}
	public default void loadVector(String name, Vector3D vector) {
		this.getUniformVariable(name).loadVector(vector);
	}
	public default void loadVector(String name, Vector4D vector) {
		this.getUniformVariable(name).loadVector(vector);
	}
	public default void loadColor3f(Color color) {
		loadColor3f("color", color);
	}
	public default void loadColor3f(String name, Color color) {
		this.getUniformVariable(name).loadColor3f(color);
	}
	public default void loadColor4f(Color color) {
		loadColor4f("color", color);
	}
	public default void loadColor4f(String name, Color color) {
		this.getUniformVariable(name).loadColor4f(color);
	}
	public default void loadBoolean(String name, boolean value) {
		this.getUniformVariable(name).loadBoolean(value);
	}
	public default void loadMatrix(String name, Matrix matrix) {
		this.getUniformVariable(name).loadMatrix(matrix);
	}
	public default void loadMatrix(MatrixType type, Matrix matrix) {
		loadMatrix(type.getUniformVariableName(), matrix);
	}
	public default void use(Consumer<ShaderProgram> consumer) {
		GL20.glUseProgram(id());
		consumer.accept(this);
		GL20.glUseProgram(0);
	}
}
