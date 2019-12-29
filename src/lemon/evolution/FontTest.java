package lemon.evolution;

import java.io.File;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import lemon.engine.render.MatrixType;
import lemon.evolution.setup.CommonProgramsSetup;
import lemon.evolution.util.CommonPrograms2D;
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
	private Font font;
	private Map<Text, Matrix> text;

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
		CommonProgramsSetup.setup2D();
		CommonPrograms2D.TEXT.getShaderProgram().use(program -> {
			program.loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
			program.loadMatrix(MatrixType.VIEW_MATRIX, MathUtil.getTranslation(new Vector3D(-300f, 0f, -500f)));
			program.loadMatrix(MatrixType.PROJECTION_MATRIX, projectionMatrix);
			program.loadVector("color", new Vector3D(1f, 1f, 1f));
			program.loadInt("sampler", TextureBank.REUSE.getId());
		});
		font = new Font(new File("res/fonts/FreeSans.fnt"));
		text = new HashMap<Text, Matrix>();
		//text = new Text(font, "Evolution");

		text.put(new Text(font, "the quick brown"), Matrix.IDENTITY_4);
		text.put(new Text(font, "fox jumped over"), MathUtil.getTranslation(new Vector3D(0f, -100f, 0f)));
		text.put(new Text(font, "the lazy dog"), MathUtil.getTranslation(new Vector3D(0f, -200f, 0f)));
	}
	@Subscribe
	public void render(RenderEvent event) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL13.glActiveTexture(TextureBank.REUSE.getBind());
		CommonPrograms2D.TEXT.getShaderProgram().use(program -> {
			for (Text t : text.keySet()) {
				program.loadMatrix(MatrixType.MODEL_MATRIX, text.get(t));
				t.draw();
			}
		});
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
