package lemon.engine.draw;

import lemon.engine.math.VectorData;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public interface Drawable {
	public static final int BYTES_PER_FLOAT = 4;
	public void draw();
	public static int getStride(VectorData[][] vertices) {
		int stride = 0;
		for (VectorData[] data : vertices) {
			if (data.length > 0) {
				stride += data[0].numDimensions();
			}
		}
		return stride;
	}
	public static FloatBuffer getFloatBuffer(VectorData[][] vertices, int stride) {
		int numVertices = vertices[0].length;
		FloatBuffer buffer = BufferUtils.createFloatBuffer(numVertices * stride);
		for (int i = 0; i < numVertices; i++) {
			for (int j = 0; j < vertices.length; j++) {
				buffer.put(vertices[j][i].constantData());
			}
		}
		buffer.flip();
		return buffer;
	}
}
