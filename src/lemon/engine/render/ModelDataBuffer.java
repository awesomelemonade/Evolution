package lemon.engine.render;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.BufferUtils;

public class ModelDataBuffer implements ModelData {
	private List<DataArray> dataArrays;
	private IntBuffer indices;
	private int indicesSize;
	public ModelDataBuffer(int indicesSize){
		dataArrays = new ArrayList<DataArray>();
		indices = BufferUtils.createIntBuffer(indicesSize);
		this.indicesSize = indicesSize;
	}
	public void addDataArray(DataArray array) {
		dataArrays.add(array);
	}
	public void addIndices(int... indices) {
		this.indices.put(indices);
	}
	public void flip(){
		for(DataArray array: dataArrays){
			array.getFloatBuffer().flip();
		}
		indices.flip();
	}
	@Override
	public IntBuffer getIndicesBuffer() {
		return indices;
	}
	@Override
	public List<DataArray> getDataArrays() {
		return Collections.unmodifiableList(dataArrays);
	}
	@Override
	public int getVertexCount() {
		return indicesSize;
	}
}
