package lemon.engine.time;

public class LemonBenchmarkEvent implements BenchmarkEvent {
	private Benchmark benchmark;
	public LemonBenchmarkEvent(Benchmark benchmark){
		this.benchmark = benchmark;
	}
	@Override
	public Benchmark getBenchmark() {
		return benchmark;
	}
}
