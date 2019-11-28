package lemon.evolution.util;

import org.lwjgl.opengl.GL20;

import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.toolbox.Toolbox;

public enum CommonPrograms2D implements ShaderProgramHolder {
	COLOR(new int[] { 0, 1 }, new String[] { "position", "color" },
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders2d/colorVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders2d/colorFragmentShader").orElseThrow())),
	LINE(new int[] { 0, 1 }, new String[] { "id", "value" },
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders2d/lineVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders2d/lineFragmentShader").orElseThrow())),
	TEXT(new int[] { 0, 1 }, new String[] { "position", "textureCoords" },
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders2d/textVertexShader").orElseThrow()),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders2d/textFragmentShader").orElseThrow()));
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
	@Override
	public ShaderProgram getShaderProgram() {
		return shaderProgram;
	}
	public static void initAll() {
		for (CommonPrograms2D program : CommonPrograms2D.values()) {
			program.init();
		}
	}
}
