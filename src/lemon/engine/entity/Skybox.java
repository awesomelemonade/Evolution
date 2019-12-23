package lemon.engine.entity;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import lemon.engine.render.Renderable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lemon.engine.control.Initializable;
import lemon.engine.render.VertexArray;
import lemon.engine.toolbox.Toolbox;

public enum Skybox implements Renderable, Initializable {
	INSTANCE {
		private VertexArray vertexArray;

		@Override
		public void init() {
			vertexArray = new VertexArray();
			vertexArray.bind(vao -> {
				vao.generateVbo().bind(GL15.GL_ELEMENT_ARRAY_BUFFER, (target, vbo) -> {
					GL15.glBufferData(target, INDICES, GL15.GL_STATIC_DRAW);
				}, false);
				vao.generateVbo().bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
					GL15.glBufferData(target, VERTICES, GL15.GL_STATIC_DRAW);
					GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * 4, 0);
				});
				GL20.glEnableVertexAttribArray(0);
			});
		}
		@Override
		public void render() {
			vertexArray.bind(vao -> {
				GL11.glDrawElements(GL11.GL_TRIANGLES, INDICES_COUNT, GL11.GL_UNSIGNED_INT, 0);
			});
		}
	};
	private static final FloatBuffer VERTICES = Toolbox.toFloatBuffer(-1, -1, -1, -1, -1, 1, -1, 1, -1, -1, 1, 1, 1, -1,
			-1, 1, -1, 1, 1, 1, -1, 1, 1, 1);
	private static final IntBuffer INDICES = Toolbox.toIntBuffer(2, 0, 4, 4, 6, 2, 1, 0, 2, 2, 3, 1, 4, 5, 7, 7, 6, 4,
			1, 3, 7, 7, 5, 1, 2, 6, 7, 7, 3, 2, 0, 1, 4, 4, 1, 5);
	private static final int INDICES_COUNT = 36;
}
