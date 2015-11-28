package lemon.engine.glfw;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;

import lemon.engine.input.FileDropEvent;

public class GLFWFileDropEvent implements FileDropEvent, GLFWEvent {
	private long window;
	private String[] files;
	public GLFWFileDropEvent(long window, int count, long files){
		this.window = window;
		this.files = new String[count];
		PointerBuffer nameBuffer = MemoryUtil.memPointerBuffer(files, count);
		for(int i=0;i<count;++i){
			this.files[i] = MemoryUtil.memDecodeUTF8(MemoryUtil.memByteBufferNT1(nameBuffer.get(i)));
		}
	}
	@Override
	public long getWindow() {
		return window;
	}
	@Override
	public String[] getFiles() {
		return files;
	}
}
