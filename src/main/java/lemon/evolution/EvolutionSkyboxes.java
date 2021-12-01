package lemon.evolution;

public enum EvolutionSkyboxes implements SkyboxInfo {
	AME_ASH("ame_ash"),
	AME_BLUEFREEZE("ame_bluefreeze"),
	AME_COTTON("ame_cotton"),
	AME_DARKGLOOM("ame_darkgloom"),
	AME_DESERT("ame_desert"),
	AME_FLATROCK("ame_flatrock"),
	AME_ICEFLATS("ame_iceflats"),
	AME_NEBULA("ame_nebula"),
	AME_SIEGE("ame_siege"),
	AME_STARFIELD("ame_starfield"),
	MP_AMH("mp_amh"),
	MP_CRIMMIND("mp_crimmind"),
	MP_DEVILTOOTH("mp_deviltooth"),
	MP_DRUIDCOVE("mp_druidcove"),
	MP_ORGANIC("mp_organic"),
	LMCITY("lmcity"),
	DARKSKIES("darkskies"),
	HW_SPIRES("hw_spires");

	private final String directoryPath;

	private EvolutionSkyboxes(String directoryPath) {
		this.directoryPath = directoryPath;
	}

	@Override
	public String directoryPath() {
		return directoryPath;
	}
}
