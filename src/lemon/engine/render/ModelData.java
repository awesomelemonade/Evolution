package lemon.engine.render;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public interface ModelData {
	public void addData(float... data);
	public void addIndices(int... indices);
	public IntBuffer getIndicesBuffer();
	public FloatBuffer getDataBuffer();
	public int getVertexCount();
}
