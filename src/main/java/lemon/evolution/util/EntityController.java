package lemon.evolution.util;

import lemon.engine.event.Observable;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.EvolutionControls;
import lemon.evolution.Game;
import lemon.evolution.entity.RocketLauncherProjectile;
import lemon.evolution.world.ControllableEntity;
import org.lwjgl.glfw.GLFW;

public class EntityController<T extends ControllableEntity> implements Disposable {
	private static final boolean USE_SURF = false;
	private static final float MOUSE_SENSITIVITY = 0.001f;
	private static final float JUMP_HEIGHT = 2f;
	private static float playerSpeed = 0.08f;
	private final Disposables disposables = new Disposables();
	private final GLFWGameControls<EvolutionControls> controls;
	private final Observable<T> current;
	private float lastMouseX;
	private float lastMouseY;

	public EntityController(GLFWGameControls<EvolutionControls> controls, T entity) {
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
		controls.addCallback(input -> input.mouseButtonEvent().add(event -> {
			if (event.action() == GLFW.GLFW_PRESS && event.button() == GLFW.GLFW_MOUSE_BUTTON_1) {
				var currentEntity = current.getValue();
				currentEntity.world().entities().add(new RocketLauncherProjectile(
						currentEntity.location().add(currentEntity.vectorDirection().multiply(0.95f)),
						currentEntity.vectorDirection().multiply(2f),
						Game.INSTANCE.rocketLauncherProjectileModel // TODO: Temporary
				));
			}
		}));
		disposables.add(controls.activated(EvolutionControls.JUMP).onChange(activated -> {
			if (activated) {
				var currentEntity = current.getValue();
				currentEntity.groundWatcher().groundNormal().ifPresent(normal -> currentEntity.mutableForce().add(normal).multiply(JUMP_HEIGHT));
			}
		}));
		var currentCleanup = new Disposables(current.getValue().onUpdate().add(this::update));
		disposables.add(current.onChange(newEntity -> {
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

	public float playerSpeed() {
		return playerSpeed;
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
