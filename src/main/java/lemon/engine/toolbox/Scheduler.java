package lemon.engine.toolbox;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Scheduler {
	private final PriorityQueue<Task> queue = new PriorityQueue<>(Comparator.comparing(Task::executionTime));

	public void add(Instant executionTime, Runnable runnable) {
		queue.add(new Task(executionTime, runnable));
	}

	public void add(Duration future, Runnable runnable) {
		add(Instant.now().plus(future), runnable);
	}

	public void run() {
		var now = Instant.now();
		while ((!queue.isEmpty()) && queue.peek().executionTime().isBefore(now)) {
			queue.poll().runnable().run();
		}
	}

	public static record Task(Instant executionTime, Runnable runnable) {}
}
