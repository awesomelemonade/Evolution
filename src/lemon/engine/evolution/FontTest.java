package lemon.engine.evolution;

import java.io.File;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lemon.engine.control.RenderEvent;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.font.Font;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector;
import lemon.engine.render.Shader;
import lemon.engine.render.ShaderProgram;
import lemon.engine.render.UniformVariable;
import lemon.engine.render.VertexArray;
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
	private VertexArray text;
	public void start(long window){
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(window, width, height);
		int window_width = width.get();
		int window_height = height.get();
		GL11.glViewport(0, 0, window_width, window_height);
		Matrix projectionMatrix = MathUtil.getPerspective(new Projection(60f, ((float)window_width)/((float)window_height), 0.01f, 1000f));
		textProgram = new ShaderProgram(
				new int[]{0, 1},
				new String[]{"position", "textureCoords"},
				new Shader(GL20.GL_VERTEX_SHADER, Toolbox.getFile("shaders2d/textVertexShader")),
				new Shader(GL20.GL_FRAGMENT_SHADER, Toolbox.getFile("shaders2d/textFragmentShader"))
		);
		uniform_textModelMatrix = textProgram.getUniformVariable("modelMatrix");
		uniform_textViewMatrix = textProgram.getUniformVariable("viewMatrix");
		uniform_textProjectionMatrix = textProgram.getUniformVariable("projectionMatrix");
		uniform_textColor = textProgram.getUniformVariable("color");
		uniform_textSampler = textProgram.getUniformVariable("sampler");
		GL20.glUseProgram(textProgram.getId());
		uniform_textModelMatrix.loadMatrix(Matrix.getIdentity(4));
		uniform_textViewMatrix.loadMatrix(MathUtil.getTranslation(new Vector(0f, 0f, -10f)));
		uniform_textProjectionMatrix.loadMatrix(projectionMatrix);
		uniform_textColor.loadVector(new Vector(1f, 1f, 1f));
		uniform_textSampler.loadInt(TextureBank.REUSE.getId());
		GL20.glUseProgram(0);
		text = new VertexArray();
		GL30.glBindVertexArray(text.getId());
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, text.generateVbo().getId());
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, Toolbox.toFloatBuffer(
				-1f, 1f, 0f, 0f,
				-1f, -1f, 0f, 1f,
				1f, 1f, 1f, 0f,
				1f, -1f, 1f, 1f
		), GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 4*4, 0);
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 4*4, 2*4);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
		font = new Font(new File("res/fonts/FreeSans.fnt"));
		EventManager.INSTANCE.registerListener(this);
	}
	@Subscribe
	public void render(RenderEvent event){
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL13.glActiveTexture(TextureBank.REUSE.getBind());
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, font.getTexture().getId());
		GL20.glUseProgram(textProgram.getId());
		GL30.glBindVertexArray(text.getId());
		GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		GL30.glBindVertexArray(0);
		GL20.glUseProgram(0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
