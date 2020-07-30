package lemon.engine.draw;

import java.nio.FloatBuffer;

import lemon.engine.math.Vector;
import lemon.engine.render.VertexArray;
import lemon.engine.render.VertexBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

public class IndexedDrawable implements Drawable {
	private VertexArray vertexArray;
	private Vector[][] vertices;
	private int[] indices;
	private int stride;
	private int drawMode;

	public IndexedDrawable(int[] indices, Vector[][] vertices) {
		this(indices, vertices, GL11.GL_TRIANGLES);
	}
	public IndexedDrawable(int[] indices, Vector[][] vertices, int drawMode) {
		this.vertices = vertices;
		this.indices = indices;
		this.drawMode = drawMode;
		this.stride = getStride(vertices);
		vertexArray = new VertexArray();
		vertexArray.bind(vao -> {
			new VertexBuffer().bind(GL15.GL_ELEMENT_ARRAY_BUFFER, (target, vbo) -> {
				GL15.glBufferData(target, indices, GL15.GL_STATIC_DRAW);
			}, false);
			new VertexBuffer().bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
				GL15.glBufferData(target, getFloatBuffer(), GL15.GL_STATIC_DRAW);
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
	@Override
	public void draw() {
		vertexArray.bind(vao -> {
			GL11.glDrawElements(drawMode, indices.length, GL11.GL_UNSIGNED_INT, 0);
		});
	}
}
