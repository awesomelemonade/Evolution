package lemon.engine.event;

import org.junit.jupiter.api.Assertions;

public class TestListener implements Runnable {
	private int count = 0;
	public void reset() {
		count = 0;
	}
	public void assertNeverFired() {
		Assertions.assertEquals(0, count);
		reset();
	}
	public void assertFired(int count) {
		Assertions.assertEquals(count, this.count);
		reset();
	}
	public void run() {
		count++;
	}
}
