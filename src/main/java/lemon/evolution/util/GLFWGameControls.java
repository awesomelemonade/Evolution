package lemon.evolution.util;

import lemon.engine.glfw.GLFWInput;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public class GLFWGameControls<T> implements GameControls<T>, Disposable {
	private final GLFWInput input;
	private final Set<Integer> keyboardHolds = new HashSet<>();
	private final Set<Integer> keyboardToggles = new HashSet<>();
	private final Set<Integer> mouseHolds = new HashSet<>();
	private final Map<T, BooleanSupplier> controls = new HashMap<>();
	private final Disposables disposables = new Disposables();

	public GLFWGameControls(GLFWInput input) {
		this.input = input;
		disposables.add(input.keyEvent().add(event -> {
			if (event.action() == GLFW.GLFW_PRESS) {
				keyboardHolds.add(event.key());
			}
			if (event.action() == GLFW.GLFW_RELEASE) {
				keyboardHolds.remove(event.key());
			}
		}));
		disposables.add(input.keyEvent().add(event -> {
			if (event.action() == GLFW.GLFW_RELEASE) {
				if (!keyboardToggles.remove(event.key())) {
					keyboardToggles.add(event.key());
				}
			}
		}));
		disposables.add(input.mouseButtonEvent().add(event -> {
			if (event.action() == GLFW.GLFW_PRESS) {
				mouseHolds.add(event.button());
			}
			if (event.action() == GLFW.GLFW_RELEASE) {
				mouseHolds.remove(event.button());
			}
		}));
	}

	public void addCallback(Function<GLFWInput, Disposable> callback) {
		disposables.add(callback.apply(input));
	}

	public void bindKeyboardHold(int key, T control) {
		controls.put(control, () -> keyboardHolds.contains(key));
	}

	public void bindKeyboardToggle(int key, T control) {
		controls.put(control, () -> keyboardToggles.contains(key));
	}

	public void bindMouseHold(int key, T control) {
		controls.put(control, () -> mouseHolds.contains(key));
	}

	@Override
	public boolean isActivated(T control) {
		var supplier = controls.get(control);
		return supplier != null && supplier.getAsBoolean();
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
