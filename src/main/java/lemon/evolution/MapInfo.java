package lemon.evolution;

public interface MapInfo {
	public String mapName();
	public String csvPath();
	public SkyboxInfo skyboxInfo();
	public float playerSpawnRadius();
	public float itemDropSpawnRadius();
	public float worldRadius();
}
