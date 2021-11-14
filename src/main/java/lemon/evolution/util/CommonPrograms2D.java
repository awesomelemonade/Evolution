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

public enum CommonPrograms2D implements ShaderProgramHolder {
	MENUBUTTON(names("position", "color"), program -> {
		program.loadFloat("yOffset", -.3f);
		program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, Matrix.IDENTITY_4);
		program.loadMatrix(MatrixType.PROJECTION_MATRIX, Matrix.IDENTITY_4);
		program.loadColor4f("filterColor", Color.WHITE);
	},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders2d/menuButtonVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders2d/menuButtonFragmentShader").orElseThrow())),
	MENUSCROLLER(names("position", "color"), program -> {
		program.loadFloat("scrollPortion", -.5f);
		program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, Matrix.IDENTITY_4);
		program.loadMatrix(MatrixType.PROJECTION_MATRIX, Matrix.IDENTITY_4);
		program.loadColor4f("filterColor", Color.WHITE);
	},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders2d/menuScrollerVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders2d/menuScrollerFragmentShader").orElseThrow())),
	COLOR(names("position", "color"), program -> {
		program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, Matrix.IDENTITY_4);
		program.loadMatrix(MatrixType.PROJECTION_MATRIX, Matrix.IDENTITY_4);
		program.loadColor4f("filterColor", Color.WHITE);
	},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders2d/colorVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders2d/colorFragmentShader").orElseThrow())),
	TEXTURE(names("position", "textureCoords"), program -> {
		program.loadMatrix(MatrixType.PROJECTION_MATRIX, Matrix.IDENTITY_4);
		program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, Matrix.IDENTITY_4);
		program.loadSampler("textureSampler", TextureBank.REUSE);
	},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders2d/textureVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders2d/textureFragmentShader").orElseThrow())),
	LINE(names("id", "value"), program -> {
		program.loadColor3f(Color.WHITE);
		program.loadFloat("spacing", 1f);
		program.loadInt("index", 0);
		program.loadInt("total", 0);
		program.loadFloat("alpha", 1f);
	},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders2d/lineVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders2d/lineFragmentShader").orElseThrow())),
	TEXT(names("position", "textureCoords"), program -> {
		program.loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
		program.loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
		program.loadMatrix(MatrixType.PROJECTION_MATRIX, Matrix.IDENTITY_4);
		program.loadVector("color", Vector3D.ZERO);
		program.loadSampler("textureSampler", TextureBank.REUSE);
	},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders2d/textVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders2d/textFragmentShader").orElseThrow())),
	MINIMAP(names("position", "textureCoords"), program -> {
		program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, Matrix.IDENTITY_4);
		program.loadMatrix(MatrixType.PROJECTION_MATRIX, Matrix.IDENTITY_4);
		program.loadSampler("colorSampler", TextureBank.MINIMAP_COLOR);
		program.loadSampler("depthSampler", TextureBank.MINIMAP_DEPTH);
	},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders/minimapVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders/minimapFragmentShader").orElseThrow()));
	private ShaderProgram shaderProgram;
	private final String[] names;
	private final Consumer<ShaderProgram> setDefaultUniformVariables;
	private final Shader[] shaders;

	private CommonPrograms2D(String[] names, Consumer<ShaderProgram> setDefaultUniformVariables, Shader... shaders) {
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
		for (CommonPrograms2D program : CommonPrograms2D.values()) {
			if (program.shaderProgram() == null) {
				program.init();
			} else {
				program.use(p -> program.setDefaultUniformVariables.accept(program));
			}
		}
	}

	public static void setMatrices(MatrixType type, Matrix matrix) {
		for (var program : CommonPrograms2D.values()) {
			program.use(p -> {
				var uniform = p.getUniformVariable(type.getUniformVariableName());
				if (uniform != null) {
					uniform.loadMatrix(matrix);
				}
			});
		}
	}

	public static void disposeAll() {
		for (CommonPrograms2D program : CommonPrograms2D.values()) {
			if (program.shaderProgram() != null) {
				program.dispose();
			}
		}
	}

	private static String[] names(String... names) {
		return names;
	}
}
