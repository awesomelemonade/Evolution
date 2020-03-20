package lemon.evolution;

import java.io.File;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import lemon.engine.control.UpdateEvent;
import lemon.engine.draw.CommonDrawables;
import lemon.engine.draw.TextModel;
import lemon.engine.event.EventManager;
import lemon.engine.render.MatrixType;
import lemon.evolution.setup.CommonProgramsSetup;
import lemon.evolution.util.CommonPrograms2D;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import lemon.engine.control.RenderEvent;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.font.Font;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Projection;
import lemon.engine.math.Vector3D;
import lemon.engine.texture.TextureBank;
import org.lwjgl.opengl.GL15;

public enum FontTest implements Listener {
	INSTANCE;
	private Font font;
	private Map<TextModel, Matrix> text;

	@Override
	public void onRegister() {
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(GLFW.glfwGetCurrentContext(), width, height);
		int window_width = width.get();
		int window_height = height.get();
		GL11.glViewport(0, 0, window_width, window_height);
		CommonProgramsSetup.setup2D();
		CommonPrograms2D.COLOR.getShaderProgram().use(program -> {
			program.loadMatrix(MatrixType.PROJECTION_MATRIX, MathUtil.getOrtho(window_width, window_height, -1, 1));
		});
		CommonPrograms2D.TEXT.getShaderProgram().use(program -> {
			program.loadMatrix(MatrixType.MODEL_MATRIX, Matrix.IDENTITY_4);
			program.loadMatrix(MatrixType.VIEW_MATRIX, Matrix.IDENTITY_4);
			//program.loadMatrix(MatrixType.PROJECTION_MATRIX, Matrix.IDENTITY_4);
			program.loadMatrix(MatrixType.PROJECTION_MATRIX, MathUtil.getOrtho(window_width, window_height, -1, 1));
			program.loadVector("color", new Vector3D(0f, 1f, 1f));
			program.loadInt("sampler", TextureBank.REUSE.getId());
		});
		font = new Font(new File("res/fonts/FreeSans.fnt"));
		text = new HashMap<TextModel, Matrix>();
		//text = new Text(font, "Evolution");

		text.put(new TextModel(font, "ABCDEFG"), MathUtil.getScalar(new Vector3D(0.005f, 0.005f, 0.005f)));
		text.put(new TextModel(font, "the quick brown fox jumped over the lazy dog"), MathUtil.getScalar(new Vector3D(0.2f, 0.2f, 0.2f)));
		text.put(new TextModel(font, "fox jumped over"), MathUtil.getTranslation(new Vector3D(0f, 100f, 0f)));
		text.put(new TextModel(font, "the lazy dog"), MathUtil.getTranslation(new Vector3D(0f, 200f, 0f)));
		text.put(new TextModel(font, "[UNKNOWN: Listeners]", GL15.GL_DYNAMIC_DRAW),
				MathUtil.getTranslation(new Vector3D(0f, 50f, 0f))
						.multiply(MathUtil.getScalar(new Vector3D(0.2f, 0.2f, 0.2f))));
	}
	@Subscribe
	public void update(UpdateEvent event) {
		String message = String.format("Listeners Registered=%d, Methods=%d, Preloaded=%d",
				EventManager.INSTANCE.getListenersRegistered(),
				EventManager.INSTANCE.getListenerMethodsRegistered(),
				EventManager.INSTANCE.getPreloadedMethodsRegistered());
		text.keySet().forEach(textModel -> {
			if (textModel.getText().contains("Listeners")) {
				if (!textModel.getText().equals(message)) {
					textModel.setText(message);
				}
			}
		});
	}
	@Subscribe
	public void render(RenderEvent event) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL13.glActiveTexture(TextureBank.REUSE.getBind());
		CommonPrograms2D.COLOR.getShaderProgram().use(program -> {
			program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX,
					MathUtil.getTranslation(new Vector3D(150f, 150f, 0f))
					.multiply(MathUtil.getScalar(new Vector3D(100f, 100f, 100f))));
			CommonDrawables.COLORED_QUAD.draw();
		});
		CommonPrograms2D.TEXT.getShaderProgram().use(program -> {
			for (TextModel t : text.keySet()) {
				program.loadMatrix(MatrixType.MODEL_MATRIX, text.get(t));
				t.draw();
			}
		});
		GL11.glDisable(GL11.GL_BLEND);
	}
}
