package lemon.engine.entity;

import org.lwjgl.opengl.GL30;

import lemon.engine.render.VertexArray;

public class Quad implements Entity {
	@Override
	public void render() {
		GL30.glBindVertexArray(TestEntities.QUAD.getVertexArray().getId());
		GL30.glBindVertexArray(0);
	}
	@Override
	public VertexArray getVertexArray() {
		return TestEntities.QUAD.getVertexArray();
	}
	@Override
	public EntityType getType() {
		return TestEntities.QUAD;
	}	
}
