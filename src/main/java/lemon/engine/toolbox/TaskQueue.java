package lemon.engine.toolbox;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface TaskQueue {
	public static TaskQueue ofSingleThreaded() {
		return new TaskQueue() {
			private final Deque<Runnable> queue = new ArrayDeque<>();
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

			@Override
			public int size() {
				return queue.size();
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

			@Override
			public int size() {
				return queue.size();
			}
		};
	}

	public void run();

	public void add(Runnable runnable);

	public int size();
}
