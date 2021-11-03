package lemon.engine.toolbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Disposables implements Disposable {
	private final List<Disposable> list = new ArrayList<>();

	public Disposables() {
		// Do Nothing
	}

	public Disposables(Disposable disposable) {
		list.add(disposable);
	}

	public Disposables(Disposable... disposables) {
		list.addAll(Arrays.asList(disposables));
	}

	public Disposables(Collection<? extends Disposable> disposables) {
		list.addAll(disposables);
	}

	public <T extends Disposable> T add(T disposable) {
		list.add(disposable);
		return disposable;
	}

	@Override
	public void dispose() {
		for (Disposable disposable : list) {
			disposable.dispose();
		}
		list.clear();
	}
}
