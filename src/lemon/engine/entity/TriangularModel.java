package lemon.engine.entity;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lemon.engine.control.Initializable;
import lemon.engine.render.Renderable;
import lemon.engine.render.VertexArray;

public class TriangularModel implements Initializable, Renderable {
	private VertexArray vertexArray;
	private FloatBuffer data;
	private int count;
	public TriangularModel(FloatBuffer data, int count){
		this.data = data;
		this.count = count;
	}
	@Override
	public void init() {
		vertexArray = new VertexArray();
		GL30.glBindVertexArray(vertexArray.getId());
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexArray.generateVbo().getId());
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 8*4, 0);
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 8*4, 3*4);
		GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 8*4, 5*4);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
	}
	@Override
	public void render() {
		GL30.glBindVertexArray(vertexArray.getId());
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, count);
		GL30.glBindVertexArray(0);
	}
}
