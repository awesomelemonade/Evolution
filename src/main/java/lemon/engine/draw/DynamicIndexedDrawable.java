package lemon.engine.draw;

import lemon.engine.render.VertexArray;
import lemon.engine.render.VertexBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class DynamicIndexedDrawable implements Drawable {
	private VertexArray vertexArray = null;
	private int numIndices;
	private int currentStride;
	private int drawMode;
	private VertexBuffer indexBuffer;
	private int indexBufferSize;
	private VertexBuffer vertexBuffer;
	private int vertexBufferSize;
	private int hint;

	public DynamicIndexedDrawable(DrawableData data) {
		this(data, GL11.GL_TRIANGLES, GL15.GL_DYNAMIC_DRAW);
	}

	public DynamicIndexedDrawable(DrawableData data, int drawMode, int hint) {
		this.drawMode = drawMode;
		this.hint = hint;
		initVertexArray(data);
	}

	private void initVertexArray(DrawableData data) {
		this.numIndices = data.indices().length;
		if (numIndices > 0) {
			currentStride = data.stride();
			vertexArray = new VertexArray();
			vertexArray.bind(vao -> {
				indexBuffer = new VertexBuffer();
				indexBuffer.bind(GL15.GL_ELEMENT_ARRAY_BUFFER, (target, vbo) -> {
					this.indexBufferSize = numIndices;
					GL15.glBufferData(target, data.indices(), hint);
				}, false);
				var vertices = data.vertices();
				vertexBuffer = new VertexBuffer();
				vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
					FloatBuffer buffer = Drawable.getFloatBuffer(vertices, currentStride);
					this.vertexBufferSize = buffer.capacity();
					GL15.glBufferData(target, buffer, hint);
					long offset = 0;
					for (int i = 0; i < vertices.length; i++) {
						if (vertices[i].length > 0) {
							int dimensions = vertices[i][0].numDimensions();
							if (dimensions <= 0 || dimensions > 4) {
								throw new IllegalArgumentException("Dimensions can only be 1, 2, 3, or 4");
							}
							GL20.glVertexAttribPointer(i, dimensions, GL11.GL_FLOAT, false,
									currentStride * BYTES_PER_FLOAT, offset * BYTES_PER_FLOAT);
							offset += dimensions;
						}
					}
				});
				for (int i = 0; i < vertices.length; i++) {
					GL20.glEnableVertexAttribArray(i);
				}
			});
		}
	}

	public void setData(DrawableData data) {
		// Technically we need to compare more than just the stride to adjust glVertexAttribPointer
		// Also maybe need more glEnableVertexAttribArray
		if (vertexArray == null) {
			initVertexArray(data);
		} else {
			this.numIndices = data.indices().length;
			if (numIndices > 0) {
				var oldStride = currentStride;
				currentStride = data.stride();
				indexBuffer.bind(GL15.GL_ELEMENT_ARRAY_BUFFER, (target, vbo) -> {
					if (numIndices > indexBufferSize) {
						indexBufferSize = numIndices;
						GL15.glBufferData(target, data.indices(), hint);
					} else {
						GL15.glBufferSubData(target, 0, data.indices());
					}
				});
				vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
					var newBuffer = data.floatBuffer();
					int newBufferSize = newBuffer.capacity();
					if (newBufferSize > 0) {
						if (newBufferSize > vertexBufferSize) {
							vertexBufferSize = newBufferSize;
							GL15.glBufferData(target, newBuffer, hint);
						} else {
							GL15.glBufferSubData(target, 0, newBuffer);
						}
					}
					if (currentStride != 0 && currentStride != oldStride) {
						vertexArray.bind(vao -> {
							long offset = 0;
							var vertices = data.vertices();
							for (int i = 0; i < vertices.length; i++) {
								if (vertices[i].length > 0) {
									int dimensions = vertices[i][0].numDimensions();
									GL20.glVertexAttribPointer(i, dimensions, GL11.GL_FLOAT, false,
											currentStride * BYTES_PER_FLOAT, offset * BYTES_PER_FLOAT);
									offset += dimensions;
								}
							}
						});
					}
				});
			}
		}
	}

	@Override
	public void draw() {
		if (numIndices > 0) {
			vertexArray.bind(vao -> {
				GL11.glDrawElements(drawMode, numIndices, GL11.GL_UNSIGNED_INT, 0);
			});
		}
	}
}
