package lemon.engine;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TaskQueue {
	private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

	public void run() {
		Runnable current;
		while ((current = queue.poll()) != null) {
			current.run();
		}
	}

	public void add(Runnable runnable) {
		queue.add(runnable);
	}
}
