package lemon.engine.evolution;

import org.lwjgl.opengl.GL20;

import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.toolbox.Toolbox;

public enum CommonPrograms {
	COLOR(
			new int[]{0, 1},
			new String[]{"position", "color"},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders/colorVertexShader")),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders/colorFragmentShader"))	
	), TEXTURE(
			new int[]{0, 1},
			new String[]{"position", "textureCoords"},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders/textureVertexShader")),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders/textureFragmentShader"))
	), CUBEMAP(
			new int[]{0},
			new String[]{"position"},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders/cubemapVertexShader")),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders/cubemapFragmentShader"))
	), POST_PROCESSING(
			new int[]{0, 1},
			new String[]{"position", "textureCoords"},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders/postVertexShader")),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders/postFragmentShader"))
	), LINE(
			new int[]{0, 1},
			new String[]{"id", "value"},
			new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders2d/lineVertexShader")),
			new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders2d/lineFragmentShader"))
	);
	private ShaderProgram shaderProgram;
	private int[] indices;
	private String[] names;
	private Shader[] shaders;
	private CommonPrograms(int[] indices, String[] names, Shader... shaders){
		this.indices = indices;
		this.names = names;
		this.shaders = shaders;
	}
	public void init(){
		shaderProgram = new ShaderProgram(indices, names, shaders);
	}
	public ShaderProgram getShaderProgram(){
		return shaderProgram;
	}
	public static void initAll(){
		for(CommonPrograms program: CommonPrograms.values()){
			program.init();
		}
	}
}
