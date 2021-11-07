package lemon.engine.draw;

import lemon.engine.math.FloatData;
import lemon.engine.render.VertexArray;
import lemon.engine.render.VertexBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class DynamicIndexedDrawable implements Drawable {
	private VertexArray vertexArray;
	private FloatData[][] vertices;
	private int[] indices;
	private int stride;
	private int drawMode;
	private VertexBuffer indexBuffer;
	private int indexBufferSize;
	private VertexBuffer vertexBuffer;
	private int vertexBufferSize;
	private int hint;

	public DynamicIndexedDrawable(int[] indices, FloatData[][] vertices) {
		this(indices, vertices, GL11.GL_TRIANGLES, GL15.GL_DYNAMIC_DRAW);
	}

	public DynamicIndexedDrawable(int[] indices, FloatData[][] vertices, int drawMode, int hint) {
		this.vertices = vertices;
		this.indices = indices;
		this.drawMode = drawMode;
		this.hint = hint;
		this.stride = Drawable.getStride(vertices);
		vertexArray = new VertexArray();
		vertexArray.bind(vao -> {
			indexBuffer = new VertexBuffer();
			indexBuffer.bind(GL15.GL_ELEMENT_ARRAY_BUFFER, (target, vbo) -> {
				this.indexBufferSize = indices.length;
				GL15.glBufferData(target, indices, hint);
			}, false);
			vertexBuffer = new VertexBuffer();
			vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
				FloatBuffer buffer = Drawable.getFloatBuffer(vertices, stride);
				this.vertexBufferSize = buffer.capacity();
				GL15.glBufferData(target, buffer, hint);
				int offset = 0;
				for (int i = 0; i < vertices.length; i++) {
					if (vertices[i].length > 0) {
						int dimensions = vertices[i][0].numDimensions();
						if (dimensions <= 0 || dimensions > 4) {
							throw new IllegalArgumentException("Dimensions can only be 1, 2, 3, or 4");
						}
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

	public void setData(int[] indices, FloatData[][] vertices) {
		this.vertices = vertices;
		this.indices = indices;
		indexBuffer.bind(GL15.GL_ELEMENT_ARRAY_BUFFER, (target, vbo) -> {
			if (indices.length > 0) {
				if (indices.length > indexBufferSize) {
					indexBufferSize = indices.length;
					GL15.glBufferData(target, indices, hint);
				} else {
					GL15.glBufferSubData(target, 0, indices);
				}
			}
		});
		vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
			int oldStride = stride;
			stride = Drawable.getStride(vertices);
			FloatBuffer newBuffer = Drawable.getFloatBuffer(vertices, stride);
			int newBufferSize = newBuffer.capacity();
			if (newBufferSize > 0) {
				if (newBufferSize > vertexBufferSize) {
					vertexBufferSize = newBufferSize;
					GL15.glBufferData(target, newBuffer, hint);
				} else {
					GL15.glBufferSubData(target, 0, newBuffer);
				}
			}
			if (stride != 0 && stride != oldStride) {
				vertexArray.bind(vao -> {
					int offset = 0;
					for (int i = 0; i < vertices.length; i++) {
						if (vertices[i].length > 0) {
							int dimensions = vertices[i][0].numDimensions();
							GL20.glVertexAttribPointer(i, dimensions, GL11.GL_FLOAT, false,
									stride * BYTES_PER_FLOAT, offset * BYTES_PER_FLOAT);
							offset += dimensions;
						}
					}
				});
			}
		});
	}

	@Override
	public void draw() {
		if (indices.length > 0) {
			vertexArray.bind(vao -> {
				GL11.glDrawElements(drawMode, indices.length, GL11.GL_UNSIGNED_INT, 0);
			});
		}
	}
}
