package lemon.engine.render;

import java.nio.IntBuffer;
import java.util.List;

public interface ModelData {
	public IntBuffer getIndicesBuffer();
	public List<DataArray> getDataArrays();
	public int getVertexCount();
}
