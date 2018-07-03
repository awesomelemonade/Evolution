package lemon.evolution.util;

import org.lwjgl.opengl.GL20;

import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.toolbox.Toolbox;

public enum CommonPrograms2D {
	COLOR(new int[] { 0, 1 }, new String[] { "position", "color" },
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders2d/colorVertexShader")),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders2d/colorFragmentShader"))), LINE(
					new int[] { 0, 1 }, new String[] { "id", "value" },
					new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders2d/lineVertexShader")),
					new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders2d/lineFragmentShader"))), TEXT(
							new int[] { 0, 1 }, new String[] { "position", "textureCoords" },
							new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders2d/textVertexShader")),
							new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders2d/textFragmentShader")));
	private ShaderProgram shaderProgram;
	private int[] indices;
	private String[] names;
	private Shader[] shaders;

	private CommonPrograms2D(int[] indices, String[] names, Shader... shaders) {
		this.indices = indices;
		this.names = names;
		this.shaders = shaders;
	}
	public void init() {
		if (shaderProgram == null) {
			shaderProgram = new ShaderProgram(indices, names, shaders);
		}
	}
	public ShaderProgram getShaderProgram() {
		return shaderProgram;
	}
	public static void initAll() {
		for (CommonPrograms2D program : CommonPrograms2D.values()) {
			program.init();
		}
	}
}
