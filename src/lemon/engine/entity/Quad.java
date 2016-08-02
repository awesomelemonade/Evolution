package lemon.engine.entity;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lemon.engine.control.Initializable;
import lemon.engine.render.Renderable;
import lemon.engine.render.VertexArray;
import lemon.engine.toolbox.Toolbox;

public enum Quad implements Renderable, Initializable {
	TEXTURED{
		private final FloatBuffer VERTICES = Toolbox.toFloatBuffer(
				-1f, 1f, 0f, 0f, 1f,
				-1f, -1f, 0f, 0f, 0f,
				1f, 1f, 0f, 1f, 1f,
				1f, -1f, 0f, 1f, 0f
		);
		private VertexArray vertexArray;
		@Override
		public void init() {
			vertexArray = new VertexArray();
			GL30.glBindVertexArray(vertexArray.getId());
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
		public void render(){
			GL30.glBindVertexArray(vertexArray.getId());
			GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
			GL30.glBindVertexArray(0);
		}
	},
	COLORED{
		private final FloatBuffer VERTICES = Toolbox.toFloatBuffer(
				-1f, 1f, 0f, 1f, 1f, 1f, 1f,
				-1f, -1f, 0f, 1f, 1f, 1f, 1f,
				1f, 1f, 0f, 1f, 1f, 1f, 1f,
				1f, -1f, 0f, 1f, 1f, 1f, 1f
		);
		private VertexArray vertexArray;
		@Override
		public void init() {
			vertexArray = new VertexArray();
			GL30.glBindVertexArray(vertexArray.getId());
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexArray.generateVbo().getId());
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, VERTICES, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 7*4, 0);
			GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 7*4, 3*4);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
			GL30.glBindVertexArray(0);
		}
		@Override
		public void render(){
			GL30.glBindVertexArray(vertexArray.getId());
			GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
			GL30.glBindVertexArray(0);
		}
	};
}
