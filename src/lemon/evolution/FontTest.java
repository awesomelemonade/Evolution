package lemon.evolution;

import java.io.File;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import lemon.engine.control.RenderEvent;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.font.Font;
import lemon.engine.font.Text;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector3D;
import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.render.UniformVariable;
import lemon.engine.texture.TextureBank;
import lemon.engine.toolbox.Toolbox;

public enum FontTest implements Listener {
	INSTANCE;
	private ShaderProgram textProgram;
	private UniformVariable uniform_textModelMatrix;
	private UniformVariable uniform_textViewMatrix;
	private UniformVariable uniform_textProjectionMatrix;
	private UniformVariable uniform_textColor;
	private UniformVariable uniform_textSampler;
	private Font font;
	private Text text;

	@Override
	public void onRegister() {
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(GLFW.glfwGetCurrentContext(), width, height);
		int window_width = width.get();
		int window_height = height.get();
		GL11.glViewport(0, 0, window_width, window_height);
		Matrix projectionMatrix = MathUtil
				.getPerspective(new Projection(MathUtil.toRadians(60f),
						((float) window_width) / ((float) window_height), 0.01f, 1000f));
		textProgram = new ShaderProgram(new int[] { 0, 1 }, new String[] { "position", "textureCoords" },
				new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders2d/textVertexShader").orElseThrow()),
				new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders2d/textFragmentShader").orElseThrow()));
		uniform_textModelMatrix = textProgram.getUniformVariable("modelMatrix");
		uniform_textViewMatrix = textProgram.getUniformVariable("viewMatrix");
		uniform_textProjectionMatrix = textProgram.getUniformVariable("projectionMatrix");
		uniform_textColor = textProgram.getUniformVariable("color");
		uniform_textSampler = textProgram.getUniformVariable("sampler");
		GL20.glUseProgram(textProgram.getId());
		uniform_textModelMatrix.loadMatrix(Matrix.IDENTITY_4);
		uniform_textViewMatrix.loadMatrix(MathUtil.getTranslation(new Vector3D(0f, 0f, -500f)));
		uniform_textProjectionMatrix.loadMatrix(projectionMatrix);
		uniform_textColor.loadVector(new Vector3D(1f, 1f, 1f));
		uniform_textSampler.loadInt(TextureBank.REUSE.getId());
		GL20.glUseProgram(0);
		font = new Font(new File("res/fonts/FreeSans.fnt"));
		text = new Text(font, "Testing 123");
	}
	@Subscribe
	public void render(RenderEvent event) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL13.glActiveTexture(TextureBank.REUSE.getBind());
		GL20.glUseProgram(textProgram.getId());
		text.render();
		GL20.glUseProgram(0);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
