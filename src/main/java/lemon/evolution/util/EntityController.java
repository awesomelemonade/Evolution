package lemon.evolution.util;

import lemon.engine.event.Observable;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Vector2D;
import lemon.engine.math.Vector3D;
import lemon.evolution.EvolutionControls;
import lemon.evolution.world.ControllableEntity;

public class EntityController {
	private static final boolean USE_SURF = false;
	private static final float MOUSE_SENSITIVITY = 0.001f;
	private static float playerSpeed = 0.1f;
	private final GLFWGameControls<EvolutionControls> controls;
	private final Observable<ControllableEntity> current;
	private float lastMouseX;
	private float lastMouseY;

	public EntityController(GLFWGameControls<EvolutionControls> controls, ControllableEntity entity) {
		this.controls = controls;
		this.current = new Observable<>(entity);
		controls.addCallback(input -> input.cursorPositionEvent().add(event -> {
			if (controls.isActivated(EvolutionControls.CAMERA_ROTATE)) {
				float deltaY = (float) (-(event.x() - lastMouseX) * MOUSE_SENSITIVITY);
				float deltaX = (float) (-(event.y() - lastMouseY) * MOUSE_SENSITIVITY);
				current.getValue().mutableRotation().asXYVector().add(deltaX, deltaY)
						.clampX(-MathUtil.PI / 2f, MathUtil.PI / 2f).modY(MathUtil.TAU);
				lastMouseX = (float) event.x();
				lastMouseY = (float) event.y();
			}
		}));
		controls.addCallback(input -> input.mouseScrollEvent().add(event -> {
			playerSpeed = Math.max(0f, playerSpeed + ((float) (event.yOffset() / 100f)));
		}));
	}

	public void update() {
		var entity = current.getValue();
		var velocity = entity.velocity();
		var mutableRotation = entity.mutableRotation();
		var rotation = entity.rotation();
		var mutableForce = current.getValue().mutableForce();
		float angle = (rotation.y() + MathUtil.PI / 2f);
		float sin = (float) Math.sin(angle);
		float cos = (float) Math.cos(angle);
		var playerHorizontalVector = Vector2D.of(playerSpeed * sin, playerSpeed * cos);

		if (controls.isActivated(EvolutionControls.STRAFE_LEFT)) {
			mutableForce.asXZVector().subtract(playerHorizontalVector);
		}
		if (controls.isActivated(EvolutionControls.STRAFE_RIGHT)) {
			mutableForce.asXZVector().add(playerHorizontalVector);
		}
		var playerForwardVector = Vector2D.of(playerSpeed * cos, -playerSpeed * sin);
		if (controls.isActivated(EvolutionControls.MOVE_FORWARDS)) {
			mutableForce.asXZVector().add(playerForwardVector);
		}
		if (controls.isActivated(EvolutionControls.MOVE_BACKWARDS)) {
			mutableForce.asXZVector().subtract(playerForwardVector);
		}
		if (controls.isActivated(EvolutionControls.JUMP)) {
			mutableForce.addY(playerSpeed);
		}
		if (controls.isActivated(EvolutionControls.CROUCH)) {
			mutableForce.subtractY(playerSpeed);
		}
		if (USE_SURF) {
			// Surfing
			var targetRotation = Vector3D.of(
					(float) Math.atan(velocity.y() / Math.hypot(velocity.x(), velocity.z())),
					(float) (Math.PI + Math.atan2(velocity.x(), velocity.z())), 0f);
			if (!targetRotation.hasNaN()) {
				var diff = targetRotation.subtract(rotation)
						.operate(x -> {
							x %= MathUtil.TAU;
							x += x < -MathUtil.PI ? MathUtil.TAU : 0f;
							x -= x > MathUtil.PI ? MathUtil.TAU : 0f;
							return x;
						});
				float diffLength = diff.length();
				if (diffLength > 0.0075f) {
					diff = diff.scaleToLength(Math.max(diffLength * 0.125f, 0.0075f));
				}
				mutableRotation.add(diff);
			}
		}
	}

	public void setCurrent(ControllableEntity entity) {
		current.setValue(entity);
	}

	public ControllableEntity current() {
		return current.getValue();
	}

	public float playerSpeed() {
		return playerSpeed;
	}
}
