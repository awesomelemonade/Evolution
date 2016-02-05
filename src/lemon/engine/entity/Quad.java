package lemon.engine.entity;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lemon.engine.render.VertexArray;
import lemon.engine.toolbox.Toolbox;

public class Quad implements Entity {
	private static VertexArray vertexArray;
	private static final FloatBuffer VERTICES = Toolbox.toFloatBuffer(
			-1f, 1f, 0f, 0f, 1f,
			-1f, -1f, 0f, 0f, 0f,
			1f, 1f, 0f, 1f, 1f,
			1f, -1f, 0f, 1f, 0f
	);
	private static final IntBuffer INDICES = Toolbox.toIntBuffer(0, 1, 2, 1, 2, 3);
	private static final int INDICES_COUNT = 6;
	public static void init(){
		vertexArray = new VertexArray();
		GL30.glBindVertexArray(vertexArray.getId());
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vertexArray.generateVbo().getId());
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, INDICES, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexArray.generateVbo().getId());
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, VERTICES, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 5*4, 0);
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 5*4, 3*4);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
	}
	@Override
	public void render() {
		GL30.glBindVertexArray(vertexArray.getId());
		GL11.glDrawElements(GL11.GL_TRIANGLES, INDICES_COUNT, GL11.GL_UNSIGNED_INT, 0);
		GL30.glBindVertexArray(0);
	}
	@Override
	public VertexArray getVertexArray() {
		return vertexArray;
	}
}
