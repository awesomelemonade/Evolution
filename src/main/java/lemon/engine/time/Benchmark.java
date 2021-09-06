package lemon.engine.time;

import lemon.engine.control.GLFWWindow;

public record Benchmark(GLFWWindow window, float[] data) {
	public static Benchmark of(GLFWWindow window, float... data) {
		return new Benchmark(window, data);
	}
}
