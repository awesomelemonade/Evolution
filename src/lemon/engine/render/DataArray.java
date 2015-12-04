package lemon.engine.render;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

public class DataArray {
	private FloatBuffer buffer;
	private int usage;
	private List<AttributePointer> pointers;
	public DataArray(int size, AttributePointer... pointers){
		this(size, GL15.GL_STATIC_DRAW, pointers);
	}
	public DataArray(int size, int usage, AttributePointer... pointers){
		this.buffer = BufferUtils.createFloatBuffer(size);
		this.usage = usage;
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
	public int getUsage(){
		return usage;
	}
	public List<AttributePointer> getAttributePointers(){
		return Collections.unmodifiableList(pointers);
	}
}
