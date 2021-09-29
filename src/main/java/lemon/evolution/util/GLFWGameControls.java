package lemon.evolution.util;

import lemon.engine.event.Observable;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.futility.FSetWithEvents;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class GLFWGameControls<T> implements GameControls<T>, Disposable {
	private final GLFWInput input;
	private final FSetWithEvents<Integer> keyboardHolds = new FSetWithEvents<>();
	private final FSetWithEvents<Integer> keyboardToggles = new FSetWithEvents<>();
	private final FSetWithEvents<Integer> mouseHolds = new FSetWithEvents<>();
	private final Map<T, Observable<Boolean>> controls = new HashMap<>();
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
		bind(key, control, keyboardHolds);
	}

	public void bindKeyboardToggle(int key, T control) {
		bind(key, control, keyboardToggles);
	}

	public void bindMouseHold(int key, T control) {
		bind(key, control, mouseHolds);
	}

	private void bind(int key, T control, FSetWithEvents<Integer> set) {
		var observable = activated(control);
		observable.setValue(keyboardHolds.contains(key));
		disposables.add(set.onAdd(k -> {
			if (k == key) {
				observable.setValue(true);
			}
		}));
		disposables.add(set.onRemove(k -> {
			if (k == key) {
				observable.setValue(false);
			}
		}));
	}

	@Override
	public Observable<Boolean> activated(T control) {
		return controls.computeIfAbsent(control, c -> new Observable<>(false));
	}

	@Override
	public void dispose() {
		disposables.dispose();
	}
}
