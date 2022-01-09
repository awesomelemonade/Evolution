package lemon.evolution;

import lemon.engine.control.GLFWWindow;
import lemon.engine.draw.CommonDrawables;
import lemon.engine.draw.TextModel;
import lemon.engine.font.CommonFonts;
import lemon.engine.font.Font;
import lemon.engine.render.MatrixType;
import lemon.engine.time.Benchmarker;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.util.CommonPrograms2D;
import lemon.evolution.util.GLFWGameControls;
import org.lwjgl.opengl.GL15;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

public class DebugOverlay implements Disposable {
	private final GLFWWindow window;
	private final Benchmarker benchmarker;

	private final StringBuilder debugMessage = new StringBuilder();
	private final Formatter debugFormatter = new Formatter(debugMessage);

	private final Font font;
	private final TextModel debugTextModel;
	private final Map<String, TextModel> keyTextModels = new HashMap<>();

	private final Disposables disposables = new Disposables();

	private final GLFWGameControls<EvolutionControls> controls;

	private static final Color[] DEBUG_GRAPH_COLORS = {
			Color.RED, Color.GREEN, Color.PURPLE, Color.YELLOW, Color.BLUE, Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.WHITE, Color.GRAY
	};

	public DebugOverlay(GLFWWindow window, GLFWGameControls<EvolutionControls> controls, Benchmarker benchmarker) {
		this.window = window;
		this.controls = controls;
		this.benchmarker = benchmarker;
		font = CommonFonts.freeSans();
		debugTextModel = disposables.add(new TextModel(font, "[Unknown]", GL15.GL_DYNAMIC_DRAW));
	}

	public void update(String format, Object... args) {
		debugMessage.setLength(0);
		debugFormatter.format(format, args);
		debugTextModel.setText(debugMessage);
	}

	public void render() {
		long debugOverlayTime = System.nanoTime();
		if (controls.isActivated(EvolutionControls.DEBUG_TOGGLE)) {
			// render graphs
			byte counter = 0;
			for (String benchmarker : this.benchmarker.getNames()) {
				Color color = DEBUG_GRAPH_COLORS[counter % DEBUG_GRAPH_COLORS.length];
				CommonPrograms2D.LINE.use(program -> {
					program.loadColor3f(color);
					program.loadFloat("spacing",
							2f / (this.benchmarker.getLineGraph(benchmarker).getSize() - 1));
					this.benchmarker.getLineGraph(benchmarker).render();
				});
				byte finalCounter = counter;
				CommonPrograms2D.COLOR.use(program -> {
					program.loadColor4f("filterColor", color);
					try (var translationMatrix = MatrixPool.ofTranslation(window.getWidth() - 200f, window.getHeight() - 50f - (finalCounter * 30f), 0f);
						 var scalarMatrix = MatrixPool.ofScalar(10f, 10f, 1f);
						 var transformationMatrix = MatrixPool.ofMultiplied(translationMatrix, scalarMatrix)) {
						program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, transformationMatrix);
					}
					CommonDrawables.COLORED_QUAD.draw();
					program.loadColor4f("filterColor", Color.WHITE);
				});
				CommonPrograms2D.TEXT.use(program -> {
					try (var translationMatrix = MatrixPool.ofTranslation(window.getWidth() - 170f, window.getHeight() - 55f - (finalCounter * 30f), 0f);
						 var scalarMatrix = MatrixPool.ofScalar(0.2f, 0.2f, 1f);
						 var transformationMatrix = MatrixPool.ofMultiplied(translationMatrix, scalarMatrix)) {
						program.loadMatrix(MatrixType.MODEL_MATRIX, transformationMatrix);
					}
					program.loadColor3f("color", Color.WHITE);
					keyTextModels.computeIfAbsent(benchmarker, s -> disposables.add(new TextModel(font, s, GL15.GL_STATIC_DRAW))).draw();
				});
				counter++;
			}
			// render debug text
			CommonPrograms2D.TEXT.use(program -> {
				try (var translationMatrix = MatrixPool.ofTranslation(5f, window.getHeight() - 20, 0f);
					 var scalarMatrix = MatrixPool.ofScalar(0.2f, 0.2f, 1f);
					 var transformationMatrix = MatrixPool.ofMultiplied(translationMatrix, scalarMatrix)) {
					program.loadMatrix(MatrixType.MODEL_MATRIX, transformationMatrix);
				}
				program.loadColor3f("color", Color.WHITE);
				debugTextModel.draw();
			});
		}
		debugOverlayTime = System.nanoTime() - debugOverlayTime;
		benchmarker.getLineGraph("debugOverlayTime").add(debugOverlayTime);
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
