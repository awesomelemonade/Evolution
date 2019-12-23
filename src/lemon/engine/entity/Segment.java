package lemon.engine.entity;

import java.nio.FloatBuffer;

import lemon.engine.render.Renderable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lemon.engine.math.Vector3D;
import lemon.engine.render.VertexArray;

public class Segment implements Renderable {
	private VertexArray vertexArray;
	private static final int VERTICES = 2;

	public Segment(Vector3D point, Vector3D point2) {
		vertexArray = new VertexArray();
		GL30.glBindVertexArray(vertexArray.getId());
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexArray.generateVbo().getId());
		FloatBuffer dataBuffer = BufferUtils.createFloatBuffer(VERTICES * 7);
		plotVertex(dataBuffer, point);
		plotVertex(dataBuffer, point2);
		dataBuffer.flip();
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, dataBuffer, GL15.GL_DYNAMIC_DRAW);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 7 * 4, 0);
		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 7 * 4, 3 * 4);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
	}
	private void plotVertex(FloatBuffer dataBuffer, Vector3D vector) {
		dataBuffer.put(vector.getX());
		dataBuffer.put(vector.getY());
		dataBuffer.put(vector.getZ());
		dataBuffer.put((float) (Math.random()));
		dataBuffer.put((float) (Math.random()));
		dataBuffer.put((float) (Math.random()));
		dataBuffer.put(1f);
	}
	@Override
	public void render() {
		GL30.glBindVertexArray(vertexArray.getId());
		GL11.glDrawArrays(GL11.GL_LINES, 0, VERTICES);
		GL30.glBindVertexArray(0);
	}
}
