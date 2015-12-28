package lemon.engine.entity;

import lemon.engine.render.VertexArray;

public class Quad implements Entity {
	@Override
	public void render() {
		
	}
	@Override
	public VertexArray getVertexArray() {
		return null;
	}
	@Override
	public EntityType getType() {
		return TestEntities.QUAD;
	}	
}
