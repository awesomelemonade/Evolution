package lemon.evolution.util;

import lemon.engine.event.Observable;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.EvolutionControls;
import lemon.evolution.world.ControllableEntity;

public class EntityController<T extends ControllableEntity> implements Disposable {
	private static final boolean USE_SURF = false;
	private static final float MOUSE_SENSITIVITY = 0.001f;
	private static final Vector3D JUMP_DIRECTION = Vector3D.of(0f, 1f, 0f);
	private static final float JUMP_HEIGHT = 1f;
	private static float playerSpeed = 0.08f;
	private final Disposables disposables = new Disposables();
	private final GameControls<EvolutionControls, GLFWInput> controls;
	private final Observable<T> current;

	public EntityController(GameControls<EvolutionControls, GLFWInput> controls, T entity) {
		this.controls = controls;
		this.current = new Observable<>(entity);
		controls.addCallback(GLFWInput::cursorDeltaEvent, event -> {
			if (controls.isActivated(EvolutionControls.CAMERA_ROTATE)) {
				float deltaY = (float) (-(event.x()) * MOUSE_SENSITIVITY);
				float deltaX = (float) (-(event.y()) * MOUSE_SENSITIVITY);
				current.getValue().mutableRotation().asXYVector().add(deltaX, deltaY)
						.clampX(-MathUtil.PI / 2f, MathUtil.PI / 2f).modY(MathUtil.TAU);
			}
		});
		controls.addCallback(GLFWInput::mouseScrollEvent, event -> {
			playerSpeed = Math.max(0f, playerSpeed + ((float) (event.yOffset() / 100f)));
		});
		disposables.add(controls.onActivated(EvolutionControls.JUMP, () -> {
			var currentEntity = current.getValue();
			currentEntity.groundWatcher().groundNormal().ifPresent(normal ->
					currentEntity.mutableVelocity().add(JUMP_DIRECTION.multiply(JUMP_HEIGHT)));
		}));
		var currentCleanup = disposables.add(new Disposables());
		disposables.add(current.onChangeAndRun(newEntity -> {
			currentCleanup.dispose();
			currentCleanup.add(newEntity.onUpdate().add(this::update));
		}));
	}

	public void update() {
		var entity = current.getValue();
		var velocity = entity.velocity();
		var mutableRotation = entity.mutableRotation();
		var rotation = entity.rotation();
		var mutableForce = current.getValue().mutableForce();
		var playerForwardVector = entity.groundWatcher().groundParallel().orElse(entity.vectorDirection()).multiply(playerSpeed);
		var playerHorizontalVector = Vector3D.of(-playerForwardVector.z(), playerForwardVector.y(), playerForwardVector.x());
		if (controls.isActivated(EvolutionControls.STRAFE_LEFT)) {
			mutableForce.subtract(playerHorizontalVector);
		}
		if (controls.isActivated(EvolutionControls.STRAFE_RIGHT)) {
			mutableForce.add(playerHorizontalVector);
		}
		if (controls.isActivated(EvolutionControls.MOVE_FORWARDS)) {
			mutableForce.add(playerForwardVector);
		}
		if (controls.isActivated(EvolutionControls.MOVE_BACKWARDS)) {
			mutableForce.subtract(playerForwardVector);
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

	public void setCurrent(T entity) {
		current.setValue(entity);
	}

	public T current() {
		return current.getValue();
	}

	public Observable<T> observableCurrent() {
		return current;
	}

	public float playerSpeed() {
		return playerSpeed;
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
