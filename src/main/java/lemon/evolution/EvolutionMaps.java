package lemon.evolution;

public enum EvolutionMaps implements MapInfo {
	SKULL_ISLAND("Skull Island", "SkullIsland.csv", EvolutionSkyboxes.MP_DEVILTOOTH, 25f, 50f),
	CASTLE("Castle", "castle.csv", EvolutionSkyboxes.AME_SIEGE, 25f, 50f),
	CLOUDSKY("Cloud Sky", "cloudsky.csv", EvolutionSkyboxes.AME_NEBULA, 25f, 50f),
	HAGIA_SOPHIA("Hagia Sophia", "hagiasophia.csv", EvolutionSkyboxes.MP_DRUIDCOVE, 25f, 50f),
	POND("Pond", "pond.csv", EvolutionSkyboxes.MP_ORGANIC, 7.5f, 15f);

	private final String mapName;
	private final String csvPath;
	private final SkyboxInfo skyboxInfo;
	private final float playerSpawnRadius;
	private final float itemDropSpawnRadius;
	private EvolutionMaps(String mapName, String csvPath, SkyboxInfo skyboxInfo,
						  float playerSpawnRadius, float itemDropSpawnRadius) {
		this.mapName = mapName;
		this.csvPath = csvPath;
		this.skyboxInfo = skyboxInfo;
		this.playerSpawnRadius = playerSpawnRadius;
		this.itemDropSpawnRadius = itemDropSpawnRadius;
	}

	@Override
	public String mapName() {
		return mapName;
	}

	@Override
	public String csvPath() {
		return csvPath;
	}

	@Override
	public SkyboxInfo skyboxInfo() {
		return skyboxInfo;
	}

	@Override
	public float playerSpawnRadius() {
		return playerSpawnRadius;
	}

	@Override
	public float itemDropSpawnRadius() {
		return itemDropSpawnRadius;
	}
}
