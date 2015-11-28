package lemon.engine.render;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.BufferUtils;

public class DataArray {
	private FloatBuffer buffer;
	private List<AttributePointer> pointers;
	public DataArray(int size, AttributePointer... pointers){
		this.buffer = BufferUtils.createFloatBuffer(size);
		this.pointers = new ArrayList<AttributePointer>();
		for(AttributePointer pointer: pointers){
			this.pointers.add(pointer);
		}
	}
	public void addData(float... data){
		buffer.put(data);
	}
	public FloatBuffer getFloatBuffer(){
		return buffer;
	}
	public List<AttributePointer> getAttributePointers(){
		return Collections.unmodifiableList(pointers);
	}
}
