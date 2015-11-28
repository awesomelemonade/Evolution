package lemon.engine.render;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

public class ModelDataBuffer implements ModelData {
	private FloatBuffer data;
	private IntBuffer indices;
	private int dataSize;
	private int indicesSize;
	public ModelDataBuffer(int dataSize, int indicesSize){
		data = BufferUtils.createFloatBuffer(dataSize);
		indices = BufferUtils.createIntBuffer(indicesSize);
		this.dataSize = dataSize;
		this.indicesSize = indicesSize;
	}
	@Override
	public void addData(float... data) {
		this.data.put(data);
	}
	@Override
	public void addIndices(int... indices) {
		this.indices.put(indices);
	}
	public void flip(){
		data.flip();
		indices.flip();
	}
	@Override
	public IntBuffer getIndicesBuffer() {
		return indices;
	}
	@Override
	public FloatBuffer getDataBuffer() {
		return data;
	}
	public int getDataSize(){
		return dataSize;
	}
	@Override
	public int getVertexCount() {
		return indicesSize;
	}
}
