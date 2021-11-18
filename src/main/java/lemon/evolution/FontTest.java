package lemon.evolution;

import lemon.engine.control.GLFWWindow;
import lemon.engine.draw.CommonDrawables;
import lemon.engine.draw.TextModel;
import lemon.engine.font.CommonFonts;
import lemon.engine.font.Font;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;
import lemon.engine.render.MatrixType;
import lemon.engine.texture.TextureBank;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.screen.beta.Screen;
import lemon.evolution.setup.CommonProgramsSetup;
import lemon.evolution.util.CommonPrograms2D;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;

import java.util.HashMap;
import java.util.Map;

public enum FontTest implements Screen {
	INSTANCE;
	private final Disposables disposables = new Disposables();
	private Font font;
	private Map<TextModel, Matrix> text;

	@Override
	public void onLoad(GLFWWindow window) {
		GL11.glViewport(0, 0, window.getWidth(), window.getHeight());
		Matrix projectionMatrix = MathUtil.getOrtho(window.getWidth(), window.getHeight(), -1, 1);
		CommonProgramsSetup.setup2D(projectionMatrix);
		CommonPrograms2D.TEXT.use(program -> {
			program.loadVector("color", Vector3D.of(0f, 1f, 1f));
		});
		font = CommonFonts.freeSans();
		text = new HashMap<>();

		text.put(new TextModel(font, "ABCDEFG"), MathUtil.getScalar(Vector3D.of(0.005f, 0.005f, 0.005f)));
		text.put(new TextModel(font, "the quick brown fox jumped over the lazy dog"), MathUtil.getScalar(Vector3D.of(0.2f, 0.2f, 0.2f)));
		text.put(new TextModel(font, "fox jumped over"), MathUtil.getTranslation(Vector3D.of(0f, 100f, 0f)));
		text.put(new TextModel(font, "the lazy dog"), MathUtil.getTranslation(Vector3D.of(0f, 200f, 0f)));
		text.put(new TextModel(font, "[UNKNOWN: Listeners]", GL15.GL_DYNAMIC_DRAW),
				MathUtil.getTranslation(Vector3D.of(0f, 50f, 0f))
						.multiply(MathUtil.getScalar(Vector3D.of(0.2f, 0.2f, 0.2f))));

		text.keySet().forEach(disposables::add);
	}

	@Override
	public void update(long deltaTime) {
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
		CommonPrograms2D.COLOR.use(program -> {
			program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX,
					MathUtil.getTranslation(Vector3D.of(150f, 150f, 0f))
							.multiply(MathUtil.getScalar(Vector3D.of(100f, 100f, 100f))));
			CommonDrawables.COLORED_QUAD.draw();
		});
		CommonPrograms2D.TEXT.use(program -> {
			text.forEach((model, matrix) -> {
				program.loadMatrix(MatrixType.MODEL_MATRIX, matrix);
				model.draw();
			});
		});
		GL11.glDisable(GL11.GL_BLEND);
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
