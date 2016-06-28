package lemon.engine.time;

import lemon.engine.control.GLFWWindow;

public class Benchmark {
	private GLFWWindow window;
	private float[] data;
	public Benchmark(GLFWWindow window, float... data){
		this.window = window;
		this.data = data;
	}
	public GLFWWindow getGLFWWindow(){
		return window;
	}
	public float[] getData(){
		return data;
	}
}
