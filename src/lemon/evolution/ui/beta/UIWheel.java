package lemon.evolution.ui.beta;

import lemon.engine.draw.CommonDrawables;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.math.Vector2D;
import lemon.engine.render.MatrixType;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposable;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.util.CommonPrograms2D;
import org.lwjgl.glfw.GLFW;

public class UIWheel implements UIInputComponent {
	private Vector2D position;
	private float radius;
	private float value; // 0 to 2 * pi
	private Color color;
	private boolean heldDown;
	public UIWheel(Vector2D position, float radius, float value, Color color) {
		this.position = position;
		this.radius = radius;
		this.value = value;
		this.color = color;
	}

	@Override
	public Disposable registerInputEvents(GLFWInput input) {
		return Disposable.of(input.mouseButtonEvent().add(event -> {
			if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_1) {
				if (event.getAction() == GLFW.GLFW_PRESS) {
					event.getWindow().pollMouse((mouseX, mouseY) -> {
						if (position.distanceSquared(mouseX, mouseY) <= radius * radius) {
							setValue(mouseX, mouseY);
							heldDown = true;
						}
					});
				}
				if (event.getAction() == GLFW.GLFW_RELEASE) {
					heldDown = false;
				}
			}
		}), input.cursorPositionEvent().add(event -> {
			if (heldDown) {
				this.setValue(event.getX(), event.getWindow().getHeight() - event.getY());
			}
		}));
	}

	@Override
	public void render() {
		CommonPrograms2D.COLOR.getShaderProgram().use(program -> {
			try (var translation = MatrixPool.ofTranslation(1f, 0f, 0f);
				 var scalar = MatrixPool.ofScalar(radius / 2f, radius / 20f, 0f);
				 var translation2 = MatrixPool.ofTranslation(position.x(), position.y(), 0f);
				 var rotation = MatrixPool.ofRotationZ(-value);
				 var multiplied = MatrixPool.ofMultiplied(scalar, translation);
				 var multiplied2 = MatrixPool.ofMultiplied(rotation, multiplied);
				 var multiplied3 = MatrixPool.ofMultiplied(translation2, multiplied2)) {
				program.loadColor4f("filterColor", color);
				program.loadMatrix(MatrixType.TRANSFORMATION_MATRIX, multiplied3);
				CommonDrawables.COLORED_QUAD.draw();
				program.loadColor4f("filterColor", Color.WHITE);
			}
		});
	}
	private void setValue(double mouseX, double mouseY) {
		this.value = (float) Math.atan2(mouseY - position.y(), mouseX - position.x());
	}
	// Returns a value between [0, 1]
	public float getValue() {
		return value;
	}
}
