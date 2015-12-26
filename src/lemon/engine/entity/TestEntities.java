package lemon.engine.entity;

import lemon.engine.render.RawModel;

public enum TestEntities implements EntityType {
	TEST;
	@Override
	public RawModel getModel() {
		return null;
	}
}
