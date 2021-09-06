package lemon.evolution.util;

import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.toolbox.Toolbox;
import org.lwjgl.opengl.GL20;

public enum CommonPrograms3D implements ShaderProgramHolder {
	COLOR(new int[] {0, 1}, new String[] {"position", "color"},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders/colorVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders/colorFragmentShader").orElseThrow())),
	TEXTURE(new int[] {0, 1}, new String[] {"position", "textureCoords"},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders/textureVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders/textureFragmentShader").orElseThrow())),
	CUBEMAP(new int[] {0}, new String[] {"position"},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders/cubemapVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders/cubemapFragmentShader").orElseThrow())),
	POST_PROCESSING(new int[] {0, 1}, new String[] {"position", "textureCoords"},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders/postVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders/postFragmentShader").orElseThrow())),
	PARTICLE(new int[] {0, 1}, new String[] {"position", "transformationMatrix"},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders/particleVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders/particleFragmentShader").orElseThrow())),
	LIGHT(new int[] {0, 1, 2}, new String[] {"position", "color", "normal"},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders/lightVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders/lightFragmentShader").orElseThrow())),
	TERRAIN(new int[] {0, 1, 2}, new String[] {"position", "color", "normal"},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("/shaders/terrainVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("/shaders/terrainFragmentShader").orElseThrow()));
	private ShaderProgram shaderProgram;
	private int[] indices;
	private String[] names;
	private Shader[] shaders;

	private CommonPrograms3D(int[] indices, String[] names, Shader... shaders) {
		this.indices = indices;
		this.names = names;
		this.shaders = shaders;
	}

	public void init() {
		if (shaderProgram == null) {
			shaderProgram = new ShaderProgram(indices, names, shaders);
		}
	}

	@Override
	public ShaderProgram getShaderProgram() {
		return shaderProgram;
	}

	public static void initAll() {
		for (CommonPrograms3D program : CommonPrograms3D.values()) {
			program.init();
		}
	}

	public static void disposeAll() {
		for (CommonPrograms3D program : CommonPrograms3D.values()) {
			// TODO: May not be initialized
			program.dispose();
		}
	}
}
