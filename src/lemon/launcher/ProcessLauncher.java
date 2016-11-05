package lemon.launcher;

@FunctionalInterface
public interface ProcessLauncher {
	public void launchProcess(String... args);
}
