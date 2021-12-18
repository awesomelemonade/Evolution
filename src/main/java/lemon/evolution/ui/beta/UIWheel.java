package lemon.evolution.ui.beta;

import lemon.engine.draw.CommonDrawables;
import lemon.futility.FObservable;
import lemon.engine.math.Vector2D;
import lemon.engine.render.MatrixType;
import lemon.engine.toolbox.Color;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.util.CommonPrograms2D;
import org.lwjgl.glfw.GLFW;

public class UIWheel extends AbstractUIChildComponent {
	private final Vector2D position;
	private final float radius;
	private final Color color;
	private final FObservable<Float> value; // 0 to 2 * pi
	private final FObservable<Boolean> heldDown = new FObservable<>(false);

	public UIWheel(UIComponent parent, Vector2D position, float radius, float value, Color color) {
		super(parent);
		this.position = position;
		this.radius = radius;
		this.color = color;
		this.value = new FObservable<>(value);
		disposables.add(input().mouseButtonEvent().add(event -> {
			if (visible() && event.button() == GLFW.GLFW_MOUSE_BUTTON_1) {
				if (event.action() == GLFW.GLFW_PRESS) {
					event.glfwWindow().pollMouse((mouseX, mouseY) -> {
						if (position.distanceSquared(mouseX, mouseY) <= radius * radius) {
							setValue(mouseX, mouseY);
							heldDown.setValue(true);
						}
					});
				}
				if (event.action() == GLFW.GLFW_RELEASE) {
					heldDown.setValue(false);
				}
			}
		}));
		disposables.add(input().cursorPositionEvent().add(event -> {
			if (visible() && isHeldDown()) {
				this.setValue(event.x(), event.glfwWindow().getHeight() - event.y());
			}
		}));
	}

	@Override
	public void render() {
		CommonPrograms2D.COLOR.use(program -> {
			try (var translation = MatrixPool.ofTranslation(1f, 0f, 0f);
				 var scalar = MatrixPool.ofScalar(radius / 2f, radius / 20f, 0f);
				 var translation2 = MatrixPool.ofTranslation(position.x(), position.y(), 0f);
				 var rotation = MatrixPool.ofRotationZ(-getValue());
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
		this.value.setValue((float) Math.atan2(mouseY - position.y(), mouseX - position.x()));
	}

	// Returns a value between [0, 1]
	public float getValue() {
		return value.getValue();
	}

	public FObservable<Float> value() {
		return value;
	}

	public FObservable<Boolean> heldDown() {
		return heldDown;
	}

	public boolean isHeldDown() {
		return heldDown.getValue();
	}
}
