package lemon.engine.draw;

import lemon.engine.math.FloatData;
import lemon.engine.render.VertexArray;
import lemon.engine.render.VertexBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

public class IndexedDrawable implements Drawable {
	private VertexArray vertexArray;
	private FloatData[][] vertices;
	private int[] indices;
	private int stride;
	private int drawMode;

	public IndexedDrawable(int[] indices, FloatData[][] vertices) {
		this(indices, vertices, GL11.GL_TRIANGLES);
	}

	public IndexedDrawable(int[] indices, FloatData[][] vertices, int drawMode) {
		this.vertices = vertices;
		this.indices = indices;
		this.drawMode = drawMode;
		this.stride = Drawable.getStride(vertices);
		vertexArray = new VertexArray();
		vertexArray.bind(vao -> {
			new VertexBuffer().bind(GL15.GL_ELEMENT_ARRAY_BUFFER, (target, vbo) -> {
				GL15.glBufferData(target, indices, GL15.GL_STATIC_DRAW);
			}, false);
			new VertexBuffer().bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
				GL15.glBufferData(target, Drawable.getFloatBuffer(vertices, stride), GL15.GL_STATIC_DRAW);
				long offset = 0;
				for (int i = 0; i < vertices.length; i++) {
					if (vertices[i].length > 0) {
						int dimensions = vertices[i][0].numDimensions();
						GL20.glVertexAttribPointer(i, dimensions, GL11.GL_FLOAT, false,
								stride * BYTES_PER_FLOAT, offset * BYTES_PER_FLOAT);
						offset += dimensions;
					}
				}
			});
			for (int i = 0; i < vertices.length; i++) {
				GL20.glEnableVertexAttribArray(i);
			}
		});
	}

	@Override
	public void draw() {
		vertexArray.bind(vao -> {
			GL11.glDrawElements(drawMode, indices.length, GL11.GL_UNSIGNED_INT, 0);
		});
	}
}
