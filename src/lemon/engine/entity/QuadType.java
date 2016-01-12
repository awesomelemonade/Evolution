package lemon.engine.entity;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lemon.engine.event.Listener;
import lemon.engine.render.VertexArray;
import lemon.engine.render.VertexArrayWatcher;
import lemon.engine.toolbox.Toolbox;

public enum QuadType implements EntityType, Listener, VertexArrayWatcher {
	TEXTURED{
		private VertexArray vertexArray;
		@Override
		public void init(){
			vertexArray = new VertexArray();
			setupQuad(vertexArray, Toolbox.toFloatBuffer(
					-1f, 1f, 0f, 0f, 1f,
					-1f, -1f, 0f, 0f, 0f,
					1f, 1f, 0f, 1f, 1f,
					1f, -1f, 0f, 1f, 0f
			), 2);
		}
		@Override
		public VertexArray getVertexArray(){
			return vertexArray;
		}
		@Override
		public int getVertexCount(){
			return 6;
		}
	},
	COLORED{
		private VertexArray vertexArray;
		@Override
		public void init(){
			vertexArray = new VertexArray();
			setupQuad(vertexArray, Toolbox.toFloatBuffer(
					-1f, 1f, 0f, 0f, 1f,
					-1f, -1f, 0f, 0f, 0f,
					1f, 1f, 0f, 1f, 1f,
					1f, -1f, 0f, 1f, 0f
			), 3);
		}
		@Override
		public VertexArray getVertexArray(){
			return vertexArray;
		}
		@Override
		public int getVertexCount(){
			return 6;
		}
	};
	private static void setupQuad(VertexArray vertexArray, FloatBuffer data, int size){
		GL30.glBindVertexArray(vertexArray.getId());
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vertexArray.generateVbo().getId());
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, Toolbox.toIntBuffer(
				0, 1, 2, 1, 2, 3
		), GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexArray.generateVbo().getId());
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, (3+size)*4, 0);
		GL20.glVertexAttribPointer(1, size, GL11.GL_FLOAT, false, (3+size)*4, 3*4);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
	}
	@Override
	public void init() {
		
	}
	@Override
	public VertexArray getVertexArray(){
		return null;
	}
	public int getVertexCount(){
		return 0;
	}
	@Override
	public String getName(){
		return this.name();
	}
}
