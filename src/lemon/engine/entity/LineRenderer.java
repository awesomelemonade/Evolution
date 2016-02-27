package lemon.engine.entity;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import lemon.engine.render.Renderable;
import lemon.engine.render.Renderer;

public class LineRenderer implements Renderer {
	@Override
	public void render(Renderable renderable) {
		GL30.glBindVertexArray(renderable.getVertexArray().getId());
		GL11.glDrawElements(GL11.GL_LINES, renderable.getIndices(), GL11.GL_UNSIGNED_INT, 0);
		GL30.glBindVertexArray(0);
	}
}
