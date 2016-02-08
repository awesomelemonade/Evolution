package lemon.engine.entity;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lemon.engine.render.VertexArray;
import lemon.engine.toolbox.Toolbox;

public class Quad {
	private static final VertexArray VERTEX_ARRAY;
	private static final Renderable RENDER_COMPONENT;
	private static final FloatBuffer VERTICES = Toolbox.toFloatBuffer(
			-1f, 1f, 0f, 0f, 1f,
			-1f, -1f, 0f, 0f, 0f,
			1f, 1f, 0f, 1f, 1f,
			1f, -1f, 0f, 1f, 0f
	);
	private static final IntBuffer INDICES = Toolbox.toIntBuffer(0, 1, 2, 1, 2, 3);
	private static final int INDICES_COUNT = 6;
	static{
		VERTEX_ARRAY = new VertexArray();
		GL30.glBindVertexArray(VERTEX_ARRAY.getId());
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, VERTEX_ARRAY.generateVbo().getId());
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, INDICES, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, VERTEX_ARRAY.generateVbo().getId());
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, VERTICES, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 5*4, 0);
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 5*4, 3*4);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
		RENDER_COMPONENT = new Renderable(){
			@Override
			public VertexArray getVertexArray() {
				return VERTEX_ARRAY;
			}
			@Override
			public int getVertices() {
				return INDICES_COUNT;
			}
		};
	}
	public Renderable getRenderComponent(){
		return RENDER_COMPONENT;
	}
}
