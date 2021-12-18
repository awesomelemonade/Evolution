package lemon.evolution.ui.beta;

import com.google.common.collect.ImmutableSet;
import lemon.futility.FObservable;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.render.Renderable;
import lemon.engine.toolbox.Disposable;
import lemon.futility.Observable;

import java.util.Set;

public interface UIComponent extends Renderable, Disposable {
	public default void render() {
		// Do Nothing
	}

	public default void renderOverlay() {
		// Do Nothing
	}

	public FObservable<Boolean> observableEnabled();

	public Observable<Boolean> observableVisible();

	public default void setEnabled(boolean enabled) {
		observableEnabled().setValue(enabled);
	}

	public default boolean enabled() {
		return observableEnabled().getValue();
	}

	public default boolean visible() {
		return observableVisible().getValue();
	}

	public default Set<UIComponent> children() {
		return ImmutableSet.of();
	}

	public GLFWInput input();
}
