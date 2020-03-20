package lemon.engine.splash;

import java.nio.FloatBuffer;

import lemon.engine.render.Renderable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import lemon.engine.math.Box2D;
import lemon.engine.math.Percentage;
import lemon.engine.render.VertexArray;
import lemon.engine.render.VertexBuffer;
import lemon.engine.toolbox.Color;

public class LoadingBar implements Renderable {
	private VertexArray vertexArray;
	private VertexBuffer vertexBuffer;
	private Percentage percentage;
	private Color[] colors;
	private Box2D box;

	public LoadingBar(Percentage percentage, Box2D box, Color... colors) {
		vertexArray = new VertexArray();
		this.percentage = percentage;
		this.colors = new Color[4];
		for (int i = 0; i < this.colors.length; ++i) {
			this.colors[i] = colors[i % colors.length];
		}
		this.box = box;
		vertexArray.bind(vao -> {
			vertexBuffer = new VertexBuffer();
			vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
				GL15.glBufferData(target, this.getFloatBuffer(), GL15.GL_DYNAMIC_DRAW);
				GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 6 * 4, 0);
				GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 6 * 4, 2 * 4);
			});
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
		});
	}
	@Override
	public void render() {
		updateVbo();
		vertexArray.bind(vao -> {
			GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		});
	}
	public void updateVbo() {
		vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
			GL15.glBufferSubData(target, 0, this.getFloatBuffer());
		});
	}
	private FloatBuffer getFloatBuffer() {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(24);
		for (int i = 0; i <= 1; ++i) {
			for (int j = 0; j <= 1; ++j) {
				this.addPositionBuffer(buffer, box.getX() + box.getWidth() * i * percentage.getPercentage(),
						box.getY() + box.getHeight() * j);
				this.addColorBuffer(buffer, colors[j * 2 + i]);
			}
		}
		buffer.flip();
		return buffer;
	}
	private void addPositionBuffer(FloatBuffer buffer, float x, float y) {
		buffer.put(x);
		buffer.put(y);
	}
	private void addColorBuffer(FloatBuffer buffer, Color color) {
		buffer.put(color.getRed());
		buffer.put(color.getGreen());
		buffer.put(color.getBlue());
		buffer.put(color.getAlpha());
	}
	public void setPercentage(Percentage percentage) {
		this.percentage = percentage;
	}
}
