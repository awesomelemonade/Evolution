package lemon.evolution.util;

import org.lwjgl.opengl.GL20;

import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.toolbox.Toolbox;

public enum CommonPrograms3D implements ShaderProgramHolder {
	COLOR(new int[] { 0, 1 }, new String[] { "position", "color" },
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders/colorVertexShader")),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders/colorFragmentShader"))), TEXTURE(
					new int[] { 0, 1 }, new String[] { "position", "textureCoords" },
					new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders/textureVertexShader")),
					new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders/textureFragmentShader"))), CUBEMAP(
							new int[] { 0 }, new String[] { "position" },
							new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders/cubemapVertexShader")),
							new Shader(GL20.GL_FRAGMENT_SHADER,
									Toolbox.getFile("shaders/cubemapFragmentShader"))), POST_PROCESSING(
											new int[] { 0, 1 }, new String[] { "position", "textureCoords" },
											new Shader(GL20.GL_VERTEX_SHADER,
													Toolbox.getFile("shaders/postVertexShader")),
											new Shader(GL20.GL_FRAGMENT_SHADER,
													Toolbox.getFile("shaders/postFragmentShader")));
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
}
