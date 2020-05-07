package lemon.engine.draw;

import java.nio.FloatBuffer;

import lemon.engine.math.Vector;
import lemon.engine.render.VertexArray;
import lemon.engine.render.VertexBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

public class DynamicIndexedDrawable implements Drawable {
	private VertexArray vertexArray;
	private Vector[][] vertices;
	private int[] indices;
	private int stride;
	private int drawMode;
	private VertexBuffer indexBuffer;
	private int indexBufferSize;
	private VertexBuffer vertexBuffer;
	private int vertexBufferSize;
	private int hint;

	public DynamicIndexedDrawable(Vector[][] vertices, int[] indices) {
		this(vertices, indices, GL11.GL_TRIANGLES, GL15.GL_DYNAMIC_DRAW);
	}
	public DynamicIndexedDrawable(Vector[][] vertices, int[] indices, int drawMode, int hint) {
		this.vertices = vertices;
		this.indices = indices;
		this.drawMode = drawMode;
		this.hint = hint;
		this.stride = getStride(vertices);
		vertexArray = new VertexArray();
		vertexArray.bind(vao -> {
			indexBuffer = new VertexBuffer();
			indexBuffer.bind(GL15.GL_ELEMENT_ARRAY_BUFFER, (target, vbo) -> {
				this.indexBufferSize = indices.length;
				GL15.glBufferData(target, indices, hint);
			}, false);
			vertexBuffer = new VertexBuffer();
			vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
				FloatBuffer buffer = this.getFloatBuffer();
				this.vertexBufferSize = buffer.capacity();
				GL15.glBufferData(target, buffer, hint);
				int offset = 0;
				for (int i = 0; i < vertices.length; i++) {
					if (vertices[i].length > 0) {
						int dimensions = vertices[i][0].getDimensions();
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
	private int getStride(Vector[][] vertices) {
		int stride = 0;
		for (int i = 0; i < vertices.length; i++) {
			if (vertices[i].length > 0) {
				stride += vertices[i][0].getDimensions();
			}
		}
		return stride;
	}
	private FloatBuffer getFloatBuffer() {
		int numVertices = vertices[0].length;
		FloatBuffer buffer = BufferUtils.createFloatBuffer(numVertices * stride);
		for (int i = 0; i < numVertices; i++) {
			for (int j = 0; j < vertices.length; j++) {
				for (int k = 0; k < vertices[j][i].getDimensions(); k++) {
					buffer.put(vertices[j][i].get(k));
				}
			}
		}
		buffer.flip();
		return buffer;
	}
	public void setData(Vector[][] vertices, int[] indices) {
		this.vertices = vertices;
		this.indices = indices;
		indexBuffer.bind(GL15.GL_ELEMENT_ARRAY_BUFFER, (target, vbo) -> {
			if (indices.length > indexBufferSize) {
				indexBufferSize = indices.length;
				GL15.glBufferData(target, indices, hint);
			} else {
				GL15.glBufferSubData(target, 0, indices);
			}
		});
		vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
			FloatBuffer newBuffer = this.getFloatBuffer();
			int newBufferSize = newBuffer.capacity();
			if (newBufferSize != 0 && getStride(vertices) != this.stride) {
				throw new IllegalArgumentException("Data Stride Mismatch");
			}
			if (newBufferSize > vertexBufferSize) {
				vertexBufferSize = newBufferSize;
				GL15.glBufferData(target, newBuffer, hint);
			} else {
				GL15.glBufferSubData(target, 0, newBuffer);
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
