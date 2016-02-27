package lemon.engine.entity;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lemon.engine.math.Vector;
import lemon.engine.render.Renderable;
import lemon.engine.render.VertexArray;

public class Segment implements Renderable {
	private VertexArray vertexArray;
	public Segment(Vector point, Vector point2){
		vertexArray = new VertexArray();
		GL30.glBindVertexArray(vertexArray.getId());
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vertexArray.generateVbo().getId());
		int vertices = 2;
		IntBuffer indicesBuffer = BufferUtils.createIntBuffer(vertices);
		for(int i=0;i<vertices;++i){
			indicesBuffer.put(i);
		}
		indicesBuffer.flip();
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexArray.generateVbo().getId());
		FloatBuffer dataBuffer = BufferUtils.createFloatBuffer(vertices*7);
		plotVertex(dataBuffer, point);
		plotVertex(dataBuffer, point2);
		dataBuffer.flip();
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, dataBuffer, GL15.GL_DYNAMIC_DRAW);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 7*4, 0);
		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 7*4, 3*4);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
	}
	private void plotVertex(FloatBuffer dataBuffer, Vector vector){
		dataBuffer.put(vector.getX());
		dataBuffer.put(vector.getY());
		dataBuffer.put(vector.getZ());
		dataBuffer.put((float)(Math.random()));
		dataBuffer.put((float)(Math.random()));
		dataBuffer.put((float)(Math.random()));
		dataBuffer.put(1f);
	}
	@Override
	public VertexArray getVertexArray() {
		return vertexArray;
	}
	@Override
	public int getIndices() {
		return 2;
	}
}
