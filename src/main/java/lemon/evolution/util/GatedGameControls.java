package lemon.evolution.util;

import lemon.evolution.GameControls;

public class GatedGameControls<T> implements GameControls<T> {
	private final GameControls<T> controls;
	private boolean enabled = false;

	public GatedGameControls(GameControls<T> controls) {
		this.controls = controls;
	}

	public void enable() {
		enabled = true;
	}

	public void disable() {
		enabled = false;
	}

	@Override
	public boolean isActivated(T control) {
		return enabled && controls.isActivated(control);
	}
}
