package lemon.engine.toolbox;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Scheduler implements Disposable {
	private final PriorityQueue<Task> queue = new PriorityQueue<>(Comparator.comparing(task -> task.executionTime));

	public Task add(Instant executionTime, Runnable runnable) {
		var task = new Task(executionTime, runnable);
		queue.add(task);
		return task;
	}

	public Task add(Duration future, Runnable runnable) {
		return add(Instant.now().plus(future), runnable);
	}

	public void run() {
		var now = Instant.now();
		while ((!queue.isEmpty()) && queue.peek().executionTime.isBefore(now)) {
			var task = queue.poll();
			if (!task.cancelled) {
				task.runnable.run();
			}
		}
	}

	@Override
	public void dispose() {
		queue.clear();
	}

	public static class Task implements Disposable {
		private final Instant executionTime;
		private final Runnable runnable;
		private boolean cancelled;
		public Task(Instant executionTime, Runnable runnable) {
			this.executionTime = executionTime;
			this.runnable = runnable;
			this.cancelled = false;
		}

		public Instant executionTime() {
			return executionTime;
		}

		@Override
		public void dispose() {
			this.cancelled = true;
		}
	}
}
