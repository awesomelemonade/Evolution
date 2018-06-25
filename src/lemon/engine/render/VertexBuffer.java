package lemon.engine.render;

import org.lwjgl.opengl.GL15;

public class VertexBuffer {
	private int id;

	public VertexBuffer() {
		id = GL15.glGenBuffers();
	}
	public int getId() {
		return id;
	}
}
