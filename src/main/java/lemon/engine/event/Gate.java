package lemon.engine.event;

import com.google.errorprone.annotations.CheckReturnValue;
import lemon.engine.toolbox.Disposable;

public class Gate {
	private final Observable<Boolean> output = new Observable<>(true);
	private int numBlocking = 0;

	@CheckReturnValue
	public Disposable addInput(Observable<Boolean> shouldBlock) {
		if (shouldBlock.getValue()) {
			if (numBlocking == 0) {
				output.setValue(false);
			}
			numBlocking++;
		}
		return shouldBlock.onChange(input -> {
			if (input) {
				if (numBlocking == 0) {
					output.setValue(false);
				}
				numBlocking++;
			} else {
				numBlocking--;
				if (numBlocking == 0) {
					output.setValue(true);
				}
			}
		});
	}

	public Observable<Boolean> output() {
		return output;
	}
}
