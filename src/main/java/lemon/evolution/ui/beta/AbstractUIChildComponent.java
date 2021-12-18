package lemon.evolution.ui.beta;

import lemon.futility.FObservable;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.futility.Observable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractUIChildComponent implements UIChildComponent {
	private final FObservable<Boolean> enabled = new FObservable<>(true);
	private final Observable<Boolean> visible;
	private final UIComponent parent;
	private final Set<UIComponent> children = new LinkedHashSet<>();
	protected final Disposables disposables = new Disposables();

	public AbstractUIChildComponent(UIComponent parent) {
		this.parent = parent;
		this.visible = FObservable.ofAnd(parent.observableVisible(), enabled, disposables::add);
	}

	@Override
	public FObservable<Boolean> observableEnabled() {
		return enabled;
	}

	@Override
	public Observable<Boolean> observableVisible() {
		return visible;
	}

	@Override
	public UIComponent parent() {
		return parent;
	}

	@Override
	public Set<UIComponent> children() {
		return children;
	}

	@Override
	public void dispose() {
		disposables.dispose();
		List.copyOf(children).forEach(Disposable::dispose);
		parent.children().remove(this);
	}
}
