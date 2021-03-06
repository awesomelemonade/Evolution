package lemon.engine.game2d;

import java.nio.FloatBuffer;

import lemon.engine.render.Renderable;
import lemon.engine.render.VertexBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lemon.engine.math.Box2D;
import lemon.engine.render.VertexArray;
import lemon.engine.toolbox.Color;

public class Quad2D implements Renderable {
	private VertexArray vertexArray;
	private Box2D box;
	private Color[] colors;

	public Quad2D(Box2D box, Color... colors) {
		this.colors = new Color[4];
		for (int i = 0; i < 4; ++i) {
			this.colors[i] = colors[i % colors.length];
		}
		this.box = box;
		vertexArray = new VertexArray();
		GL30.glBindVertexArray(vertexArray.getId());
		new VertexBuffer().bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
			GL15.glBufferData(target, getFloatBuffer(), GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 6 * 4, 0);
			GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 6 * 4, 2 * 4);
		});
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
	}
	@Override
	public void render() {
		GL30.glBindVertexArray(vertexArray.getId());
		GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		GL30.glBindVertexArray(0);
	}
	private FloatBuffer getFloatBuffer() {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(24);
		for (int i = 0; i <= 1; ++i) {
			for (int j = 0; j <= 1; ++j) {
				buffer.put(box.getX() + box.getWidth() * i);
				buffer.put(box.getY() + box.getHeight() * j);
				putColorBuffer(buffer, colors[j * 2 + i]);
			}
		}
		buffer.flip();
		return buffer;
	}
	private void putColorBuffer(FloatBuffer buffer, Color color) {
		buffer.put(color.getRed());
		buffer.put(color.getGreen());
		buffer.put(color.getBlue());
		buffer.put(color.getAlpha());
	}
	public Box2D getBox2D() {
		return box;
	}
}
