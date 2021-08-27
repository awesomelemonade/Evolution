package lemon.engine.control;

public interface GLFWWindowSettings {
	public long createWindow();
	public default int getTargetFrameRate() {
		return 60;
	}
}
