package lemon.engine.render;

public class AttributePointer {
	private int index;
	private int size;
	private int stride;
	private long pointerOffset;
	public AttributePointer(int index, int size, int stride, long pointerOffset){
		this.index = index;
		this.size = size;
		this.stride = stride;
		this.pointerOffset = pointerOffset;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getStride() {
		return stride;
	}
	public void setStride(int stride) {
		this.stride = stride;
	}
	public long getPointerOffset() {
		return pointerOffset;
	}
	public void setPointerOffset(long pointerOffset) {
		this.pointerOffset = pointerOffset;
	}
}
