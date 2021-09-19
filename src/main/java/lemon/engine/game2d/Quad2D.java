package lemon.engine.game2d;

import lemon.engine.draw.Drawable;
import lemon.engine.math.Box2D;
import lemon.engine.render.Renderable;
import lemon.engine.render.VertexArray;
import lemon.engine.render.VertexBuffer;
import lemon.engine.toolbox.Color;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class Quad2D implements Drawable {
	private final VertexArray vertexArray;
	private final Box2D box;
	private final Color[] colors;

	public Quad2D(Box2D box, Color... colors) {
		this.colors = new Color[4];
		for (int i = 0; i < 4; ++i) {
			this.colors[i] = colors[i % colors.length];
		}
		this.box = box;
		vertexArray = new VertexArray();
		vertexArray.bind(vao -> {
			new VertexBuffer().bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
				GL15.glBufferData(target, getFloatBuffer(), GL15.GL_STATIC_DRAW);
				GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 6 * 4, 0);
				GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 6 * 4, 2 * 4);
			});
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
		});
	}

	@Override
	public void draw() {
		vertexArray.bind(vao -> {
			GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		});
	}

	private FloatBuffer getFloatBuffer() {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(24);
		for (int i = 0; i <= 1; ++i) {
			for (int j = 0; j <= 1; ++j) {
				buffer.put(box.x() + box.width() * i);
				buffer.put(box.y() + box.height() * j);
				colors[j * 2 + i].putInBuffer(buffer);
			}
		}
		buffer.flip();
		return buffer;
	}

	public Box2D getBox2D() {
		return box;
	}
}
