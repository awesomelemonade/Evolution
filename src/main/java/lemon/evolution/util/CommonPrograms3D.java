package lemon.evolution.util;

import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.texture.TextureBank;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Toolbox;
import org.lwjgl.opengl.GL20;

import java.util.function.Consumer;

public enum CommonPrograms3D implements ShaderProgramHolder {

	WATER(names("position"), program -> {
		program.loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
		program.loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
		program.loadMatrix(MatrixType.PROJECTION_MATRIX, Matrix.IDENTITY_4);
	},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders/waterVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders/waterFragmentShader").orElseThrow())),
	COLOR(names("position", "color"), program -> {
		program.loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
		program.loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
		program.loadMatrix(MatrixType.PROJECTION_MATRIX, Matrix.IDENTITY_4);
	},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders/colorVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders/colorFragmentShader").orElseThrow())),
	TEXTURE(names("position", "textureCoords"), program -> {
		program.loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
		program.loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
		program.loadMatrix(MatrixType.PROJECTION_MATRIX, Matrix.IDENTITY_4);
		program.loadInt("textureSampler", TextureBank.REUSE.getId());
	},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders/textureVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders/textureFragmentShader").orElseThrow())),
	CUBEMAP(names("position"), program -> {
		program.loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
		program.loadMatrix(MatrixType.PROJECTION_MATRIX, Matrix.IDENTITY_4);
		program.loadInt("cubemapSampler", TextureBank.SKYBOX.getId());
	},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders/cubemapVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders/cubemapFragmentShader").orElseThrow())),
	POST_PROCESSING(names("position", "textureCoords"), program -> {
		program.loadInt("colorSampler", TextureBank.COLOR.getId());
		program.loadInt("depthSampler", TextureBank.DEPTH.getId());
	},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders/postVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders/postFragmentShader").orElseThrow())),
	PARTICLE(names("position", "transformationMatrix"), program -> {
		program.loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
		program.loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
		program.loadMatrix(MatrixType.PROJECTION_MATRIX, Matrix.IDENTITY_4);
		program.loadColor4f(Color.WHITE);
	},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders/particleVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders/particleFragmentShader").orElseThrow())),
	LIGHT(names("position", "color", "normal"), program -> {
		program.loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
		program.loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
		program.loadMatrix(MatrixType.PROJECTION_MATRIX, Matrix.IDENTITY_4);
		program.loadVector("sunlightDirection", Vector3D.of(0f, -1f, 0f));
		program.loadVector("viewPos", Vector3D.ZERO);
	},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders/lightVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders/lightFragmentShader").orElseThrow())),
	TERRAIN(names("position", "color", "normal"), program -> {
		program.loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
		program.loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
		program.loadMatrix(MatrixType.PROJECTION_MATRIX, Matrix.IDENTITY_4);
		program.loadInt("grassSampler", TextureBank.GRASS.getId());
		program.loadInt("slopeSampler", TextureBank.SLOPE.getId());
		program.loadInt("rockSampler", TextureBank.ROCK.getId());
		program.loadInt("baseSampler", TextureBank.BASE.getId());
	},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders/terrainVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders/terrainFragmentShader").orElseThrow()));
	private ShaderProgram shaderProgram;
	private final String[] names;
	private final Consumer<ShaderProgram> setDefaultUniformVariables;
	private final Shader[] shaders;

	private CommonPrograms3D(String[] names, Consumer<ShaderProgram> setDefaultUniformVariables, Shader... shaders) {
		this.names = names;
		this.setDefaultUniformVariables = setDefaultUniformVariables;
		this.shaders = shaders;
	}

	public void init() {
		if (shaderProgram == null) {
			shaderProgram = ShaderProgram.of(names, setDefaultUniformVariables, shaders);
		}
	}

	@Override
	public ShaderProgram shaderProgram() {
		return shaderProgram;
	}

	public static void initAll() {
		for (CommonPrograms3D program : CommonPrograms3D.values()) {
			if (program.shaderProgram() == null) {
				program.init();
			} else {
				program.use(p -> program.setDefaultUniformVariables.accept(program));
			}
		}
	}

	public static void setMatrices(MatrixType type, Matrix matrix) {
		for (var program : CommonPrograms3D.values()) {
			program.use(p -> {
				var uniform = p.getUniformVariable(type.getUniformVariableName());
				if (uniform != null) {
					uniform.loadMatrix(matrix);
				}
			});
		}
	}

	public static void disposeAll() {
		for (CommonPrograms3D program : CommonPrograms3D.values()) {
			if (program.shaderProgram() != null) {
				program.dispose();
			}
		}
	}

	private static String[] names(String... names) {
		return names;
	}
}
