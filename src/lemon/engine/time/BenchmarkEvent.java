package lemon.engine.time;

import lemon.engine.event.Event;

public interface BenchmarkEvent extends Event {
	public Benchmark getBenchmark();
}
