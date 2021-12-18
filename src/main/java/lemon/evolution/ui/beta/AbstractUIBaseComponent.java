package lemon.evolution.ui.beta;

import lemon.futility.FObservable;
import lemon.engine.glfw.GLFWInput;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.futility.Observable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractUIBaseComponent implements UIBaseComponent {
	private final FObservable<Boolean> enabled = new FObservable<>(true);
	private final GLFWInput input;
	private final Set<UIComponent> children = new LinkedHashSet<>();
	protected final Disposables disposables = new Disposables();

	public AbstractUIBaseComponent(GLFWInput input) {
		this.input = input;
	}

	@Override
	public FObservable<Boolean> observableEnabled() {
		return enabled;
	}

	@Override
	public Observable<Boolean> observableVisible() {
		return enabled;
	}

	@Override
	public GLFWInput input() {
		return input;
	}

	@Override
	public Set<UIComponent> children() {
		return children;
	}

	@Override
	public void dispose() {
		disposables.dispose();
		List.copyOf(children).forEach(Disposable::dispose);
	}
}
