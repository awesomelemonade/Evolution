package lemon.engine.splash;

import lemon.engine.math.Box2D;
import lemon.engine.math.Percentage;
import lemon.engine.render.Renderable;
import lemon.engine.render.VertexArray;
import lemon.engine.render.VertexBuffer;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class LoadingBar implements Renderable, Disposable {
	private VertexArray vertexArray;
	private VertexBuffer vertexBuffer;
	private Color[] colors;
	private Box2D box;
	private float progress;

	public LoadingBar(Box2D box, Color... colors) {
		vertexArray = new VertexArray();
		this.progress = 0f;
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
		vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
			GL15.glBufferSubData(target, 0, this.getFloatBuffer());
		});
		vertexArray.bind(vao -> {
			GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		});
	}

	private FloatBuffer getFloatBuffer() {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(24);
		for (int i = 0; i <= 1; ++i) {
			for (int j = 0; j <= 1; ++j) {
				this.addPositionBuffer(buffer, box.x() + box.width() * i * progress,
						box.y() + box.height() * j);
				colors[j * 2 + i].putInBuffer(buffer);
			}
		}
		buffer.flip();
		return buffer;
	}

	private void addPositionBuffer(FloatBuffer buffer, float x, float y) {
		buffer.put(x);
		buffer.put(y);
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}

	@Override
	public void dispose() {
		vertexBuffer.dispose();
		vertexArray.dispose();
	}
}
