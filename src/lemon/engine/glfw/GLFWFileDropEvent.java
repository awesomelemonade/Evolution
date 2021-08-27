package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.FileDropEvent;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;

public class GLFWFileDropEvent implements FileDropEvent, GLFWEvent {
	private GLFWWindow window;
	private String[] files;

	public GLFWFileDropEvent(GLFWWindow window, int count, long files) {
		this.window = window;
		this.files = new String[count];
		PointerBuffer nameBuffer = MemoryUtil.memPointerBuffer(files, count);
		for (int i = 0; i < count; ++i) {
			this.files[i] = MemoryUtil.memUTF8(MemoryUtil.memByteBufferNT1(nameBuffer.get(i)));
		}
	}

	@Override
	public GLFWWindow getWindow() {
		return window;
	}

	@Override
	public String[] getFiles() {
		return files;
	}
}
