package lemon.evolution;

import java.nio.IntBuffer;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import lemon.engine.control.GLFWWindow;
import lemon.engine.draw.CommonDrawables;
import lemon.engine.draw.TextModel;
import lemon.engine.render.MatrixType;
import lemon.evolution.screen.beta.Screen;
import lemon.evolution.setup.CommonProgramsSetup;
import lemon.evolution.util.CommonPrograms2D;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import lemon.engine.font.Font;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;
import lemon.engine.texture.TextureBank;
import org.lwjgl.opengl.GL15;

public enum FontTest implements Screen {
	INSTANCE;
	private Font font;
	private Map<TextModel, Matrix> text;

	@Override
	public void onLoad(GLFWWindow window) {
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(GLFW.glfwGetCurrentContext(), width, height);
		int window_width = width.get();
		int window_height = height.get();
		GL11.glViewport(0, 0, window_width, window_height);
		Matrix projectionMatrix = MathUtil.getOrtho(window_width, window_height, -1, 1);
		CommonProgramsSetup.setup2D(projectionMatrix);
		CommonPrograms2D.TEXT.getShaderProgram().use(program -> {
			program.loadVector("color", new Vector3D(0f, 1f, 1f));
		});
		font = new Font(Paths.get("/res/fonts/FreeSans.fnt"));
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
	@Override
	public void update() {
//		String message = String.format("Listeners Registered=%d, Methods=%d, Preloaded=%d",
//				EventManager.INSTANCE.getListenersRegistered(),
//				EventManager.INSTANCE.getListenerMethodsRegistered(),
//				EventManager.INSTANCE.getPreloadedMethodsRegistered());
		String message = "A test message";
		text.keySet().forEach(textModel -> {
			if (textModel.getText().toString().contains("Listeners")) {
				if (!textModel.getText().equals(message)) {
					textModel.setText(message);
				}
			}
		});
	}
	@Override
	public void render() {
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

	@Override
	public void dispose() {

	}
}
