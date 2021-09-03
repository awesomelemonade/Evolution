package lemon.engine.toolbox;

import java.util.ArrayList;
import java.util.List;

public class Disposables implements Disposable {
	private final List<Disposable> list;

	public Disposables() {
		list = new ArrayList<>();
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
