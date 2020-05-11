package lemon.engine.model;

import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

import lemon.engine.render.Renderable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import lemon.engine.render.VertexArray;
import lemon.engine.render.VertexBuffer;

public class LineGraph implements Renderable {
	private Deque<Float> values;
	private int size;
	private float max;
	private VertexArray vertexArray;
	private VertexBuffer vertexBuffer;

	public LineGraph(int size, float max, float... values) {
		this.size = size;
		this.max = max;
		this.values = new ArrayDeque<Float>();
		for (float f : values) {
			this.values.add(f);
		}
		ensureCapacity();
		vertexArray = new VertexArray();
		vertexArray.bind(vao -> {
			vertexBuffer = new VertexBuffer();
			vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
				GL15.glBufferData(target, getDataBuffer(), GL15.GL_STREAM_DRAW);
				GL20.glVertexAttribPointer(0, 1, GL11.GL_FLOAT, false, 2 * 4, 0);
				GL20.glVertexAttribPointer(1, 1, GL11.GL_FLOAT, false, 2 * 4, 1 * 4);
			});
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
		});
	}
	public void add(float f) {
		values.removeFirst();
		values.add(f);
		updateVbo();
	}
	public float getLast() {
		return values.getLast();
	}
	private FloatBuffer getDataBuffer() {
		FloatBuffer dataBuffer = BufferUtils.createFloatBuffer(size * 2);
		int n = 0;
		for (float f : values) {
			dataBuffer.put(n++);
			dataBuffer.put((f / max * 2f) - 1f);
		}
		dataBuffer.flip();
		return dataBuffer;
	}
	private void updateVbo() {
		vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
			GL15.glBufferSubData(target, 0, getDataBuffer());
		});
	}
	private void ensureCapacity() {
		while (this.values.size() > size) {
			this.values.removeFirst();
		}
		while (this.values.size() < size) {
			this.values.add(0f);
		}
	}
	@Override
	public void render() {
		vertexArray.bind(vao -> {
			GL11.glDrawArrays(GL11.GL_LINE_STRIP, 0, this.values.size());
		});
	}
	public int getSize() {
		return size;
	}
}
