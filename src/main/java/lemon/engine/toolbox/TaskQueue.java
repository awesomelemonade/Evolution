package lemon.engine.toolbox;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface TaskQueue {
	public static TaskQueue ofSingleThreaded() {
		return new TaskQueue() {
			private final List<Runnable> queue = new ArrayList<>();
			@Override
			public void run() {
				for (Runnable runnable : queue) {
					runnable.run();
				}
				queue.clear();
			}

			@Override
			public void add(Runnable runnable) {
				queue.add(runnable);
			}
		};
	}

	public static TaskQueue ofConcurrent() {
		return new TaskQueue() {
			private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();
			@Override
			public void run() {
				Runnable current;
				while ((current = queue.poll()) != null) {
					current.run();
				}
			}

			@Override
			public void add(Runnable runnable) {
				queue.add(runnable);
			}
		};
	}

	public void run();

	public void add(Runnable runnable);
}
