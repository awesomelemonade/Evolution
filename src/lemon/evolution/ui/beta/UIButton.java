package lemon.evolution.ui.beta;

import lemon.engine.draw.CommonDrawables;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.math.Box2D;
import lemon.engine.render.MatrixType;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposable;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.util.CommonPrograms2D;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class UIButton implements UIInputComponent {
	private Box2D box;
	private Color color;
	private Consumer<UIButton> eventHandler;

	public UIButton(Box2D box, Color color, Consumer<UIButton> eventHandler) {
		this.box = box;
		this.color = color;
		this.eventHandler = eventHandler;
	}

	@Override
	public Disposable registerInputEvents(GLFWInput input) {
		return input.mouseButtonEvent().add(event -> {
			if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_1 && event.getAction() == GLFW.GLFW_RELEASE) {
				event.getWindow().pollMouse((mouseX, mouseY) -> {
					if (box.intersect(mouseX, mouseY)) {
						eventHandler.accept(this);
					}
				});
			}
		});
	}

	@Override
	public void render() {
		CommonPrograms2D.COLOR.getShaderProgram().use(program -> {
			try (var translationMatrix = MatrixPool.ofTranslation(box.x() + box.width() / 2f, box.y() + box.height() / 2f, 0f);
				 var scalarMatrix = MatrixPool.ofScalar(box.width() / 2f, box.height() / 2f, 1f);
				 var matrix = MatrixPool.ofMultiplied(translationMatrix, scalarMatrix)) {
				program.loadColor4f("filterColor", color);
				program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, matrix);
				CommonDrawables.COLORED_QUAD.draw();
				program.loadColor4f("filterColor", Color.WHITE);
			}
		});
	}
}
