package lemon.engine.entity;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lemon.engine.render.VertexArray;

public class Quad implements Entity {
	@Override
	public void render() {
		GL30.glBindVertexArray(BasicType.QUAD.getVertexArray().getId());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL11.glDrawElements(GL11.GL_TRIANGLES, BasicType.QUAD.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
	}
	@Override
	public VertexArray getVertexArray() {
		return BasicType.QUAD.getVertexArray();
	}
	@Override
	public EntityType getType() {
		return BasicType.QUAD;
	}	
}
