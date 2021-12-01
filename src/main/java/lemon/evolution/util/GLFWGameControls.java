package lemon.evolution.util;

import lemon.engine.event.EventWith;
import lemon.engine.event.Observable;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.evolution.EvolutionControls;
import lemon.futility.FSetWithEvents;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class GLFWGameControls<T> implements GameControls<T, GLFWInput>, Disposable {
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

	@Override
	public <U> void addCallback(Function<GLFWInput, EventWith<U>> inputEvent, Consumer<? super U> callback) {
		disposables.add(inputEvent.apply(input).add(callback));
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
		observable.setValue(set.contains(key));
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

	public static <T extends Enum<T> & DefaultBinder<T>> GLFWGameControls<T> getDefaultControls(GLFWInput input, Class<T> clazz) {
		var controls = new GLFWGameControls<T>(input);
		for (var control : clazz.getEnumConstants()) {
			control.defaultBinder().accept(controls, control);
		}
		return controls;
	}

	public interface DefaultBinder<T> {
		public BiConsumer<GLFWGameControls<T>, T> defaultBinder();

		public static BiConsumer<GLFWGameControls<EvolutionControls>, EvolutionControls> mouseHold(int key) {
			return (input, self) -> input.bindMouseHold(key, self);
		}

		public static BiConsumer<GLFWGameControls<EvolutionControls>, EvolutionControls> keyboardHold(int key) {
			return (input, self) -> input.bindKeyboardHold(key, self);
		}

		public static BiConsumer<GLFWGameControls<EvolutionControls>, EvolutionControls> keyboardToggle(int key, boolean initialState) {
			return (input, self) -> {
				var observable = input.activated(self);
				observable.setValue(initialState);
				input.disposables.add(input.keyboardHolds.onAdd(k -> {
					if (k == key) {
						observable.setValue(!observable.getValue());
					}
				}));
			};
		}
	}
}
