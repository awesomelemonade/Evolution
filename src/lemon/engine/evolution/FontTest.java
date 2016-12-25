package lemon.engine.evolution;

import java.io.File;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import lemon.engine.control.RenderEvent;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.font.Font;
import lemon.engine.font.Text;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.texture.TextureBank;

public enum FontTest implements Listener {
	INSTANCE;
	private Font font;
	private Text text;
	public void start(long window){
		font = new Font(new File("res/fonts/FreeSans.fnt"));
		text = new Text(font, "");
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(window, width, height);
		int window_width = width.get();
		int window_height = height.get();
		GL11.glViewport(0, 0, window_width, window_height);
		Matrix projectionMatrix = MathUtil.getPerspective(new Projection(60f, ((float)window_width)/((float)window_height), 0.01f, 1000f));
		GL20.glUseProgram(CommonPrograms2D.TEXT.getShaderProgram().getId());
		CommonPrograms2D.TEXT.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
		CommonPrograms2D.TEXT.getShaderProgram().loadMatrix(MatrixType.VIEW_MATRIX, MathUtil.getTranslation(new Vector3D(0f, 0f, -500f)));
		CommonPrograms2D.TEXT.getShaderProgram().loadMatrix(MatrixType.PROJECTION_MATRIX, projectionMatrix);
		CommonPrograms2D.TEXT.getShaderProgram().loadVector("color", new Vector3D(1f, 1f, 1f));
		CommonPrograms2D.TEXT.getShaderProgram().loadInt("sampler", TextureBank.REUSE.getId());
		GL20.glUseProgram(0);
		EventManager.INSTANCE.registerListener(this);
	}
	@Subscribe
	public void render(RenderEvent event){
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL13.glActiveTexture(TextureBank.REUSE.getBind());
		GL20.glUseProgram(CommonPrograms2D.TEXT.getShaderProgram().getId());
		CommonPrograms2D.TEXT.getShaderProgram().loadMatrix(MatrixType.MODEL_MATRIX, MathUtil.getTranslation(new Vector3D(-text.getWidth()/2, 0f, 0f)));
		text.render();
		GL20.glUseProgram(0);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		if(Math.random()<0.05){
			String abc = "abcdefghijklmnopqrstuvwxyz";
			text = new Text(font, text.getText()+abc.charAt((int)(Math.random()*abc.length())));
		}
	}
}
