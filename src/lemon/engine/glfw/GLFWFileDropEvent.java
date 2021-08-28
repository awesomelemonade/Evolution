package lemon.engine.glfw;

import lemon.engine.control.GLFWWindow;
import lemon.engine.input.FileDropEvent;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;

public record GLFWFileDropEvent(GLFWWindow glfwWindow, String[] files) implements FileDropEvent, GLFWEvent {
	public GLFWFileDropEvent(GLFWWindow window, int count, long address) {
		this(window, getFiles(count, address));
	}
	private static String[] getFiles(int count, long address) {
		var files = new String[count];
		PointerBuffer nameBuffer = MemoryUtil.memPointerBuffer(address, count);
		for (int i = 0; i < count; ++i) {
			files[i] = MemoryUtil.memUTF8(MemoryUtil.memByteBufferNT1(nameBuffer.get(i)));
		}
		return files;
	}
}
