package lemon.engine.thread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum ThreadManager {
	INSTANCE;
	private List<Thread> threads;

	private ThreadManager() {
		threads = new ArrayList<Thread>();
	}
	public Thread addThread(Thread thread) {
		threads.add(thread);
		return thread;
	}
	public boolean removeThread(Thread thread) {
		return threads.remove(thread);
	}
	public void interrupt() {
		for (Thread thread : threads) {
			thread.interrupt();
		}
	}
	public List<Thread> getThreads() {
		return Collections.unmodifiableList(threads);
	}
}
