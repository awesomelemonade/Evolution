package lemon.engine.draw;

import java.nio.FloatBuffer;

import lemon.engine.font.CharData;
import lemon.engine.font.Font;
import lemon.engine.render.VertexBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import lemon.engine.render.VertexArray;

public class TextModel implements Drawable {
	private VertexArray vertexArray;
	private VertexBuffer vertexBuffer;
	private Font font;
	private CharSequence text;
	private int bufferSize;
	private int hint;

	public TextModel(Font font, CharSequence text) {
		this(font, text, GL15.GL_STATIC_DRAW);
	}
	public TextModel(Font font, CharSequence text, int hint) {
		if (text.length() == 0) {
			throw new IllegalStateException("Text cannot be empty");
		}
		this.font = font;
		this.text = text;
		this.hint = hint;
		vertexArray = new VertexArray();
		vertexArray.bind(vao -> {
			vertexBuffer = new VertexBuffer();
			vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
				FloatBuffer buffer = this.getFloatBuffer(text);
				bufferSize = buffer.capacity();
				GL15.glBufferData(target, buffer, hint);
				GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 4 * 4, 0);
				GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 4 * 4, 2 * 4);
			});
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
		});
	}
	private FloatBuffer buffer;
	private FloatBuffer getFloatBuffer(CharSequence text) {
		int newCapacity = text.length() * 4 * 6;
		if (buffer == null || buffer.capacity() < newCapacity) {
			buffer = BufferUtils.createFloatBuffer(newCapacity);
		} else {
			buffer.clear();
		}
		char prevChar = '\0';
		int cursor = 0;
		for (int i = 0; i < text.length(); ++i) {
			char currentChar = text.charAt(i);
			CharData data = font.getCharData(currentChar);
			int kerning = font.getKerning(prevChar, currentChar);
			putChar(buffer, data, cursor + kerning);
			cursor += (data.getXAdvance() + kerning);
			prevChar = currentChar;
		}
		buffer.flip();
		return buffer;
	}
	private void putChar(FloatBuffer buffer, CharData data, int cursor) {
		float scaleWidth = font.getScaleWidth();
		float scaleHeight = font.getScaleHeight();
		float textureX = data.getX() / scaleWidth;
		float textureY = data.getY() / scaleHeight;
		float textureWidth = data.getWidth() / scaleWidth;
		float textureHeight = data.getHeight() / scaleHeight;
		float x = cursor + data.getXOffset();
		float y = font.getLineHeight() - data.getYOffset() - font.getBase() / 2f;
		float width = data.getWidth();
		float height = data.getHeight();

		put(buffer, x, y, textureX, textureY);
		put(buffer, x + width, y, textureX + textureWidth, textureY);
		put(buffer, x + width, y - height, textureX + textureWidth, textureY + textureHeight);
		put(buffer, x, y, textureX, textureY);
		put(buffer, x, y - height, textureX, textureY + textureHeight);
		put(buffer, x + width, y - height, textureX + textureWidth, textureY + textureHeight);
	}
	private void put(FloatBuffer buffer, float... floats) {
		buffer.put(floats);
	}
	@Override
	public void draw() {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		font.getTexture().bind(GL11.GL_TEXTURE_2D, () -> {
			vertexArray.bind(vao -> {
				GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, text.length() * 6);
			});
		});
		GL11.glDisable(GL11.GL_BLEND);
	}
	public void setText(CharSequence text) {
		this.text = text;
		vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
			FloatBuffer newBuffer = this.getFloatBuffer(text);
			int newBufferSize = newBuffer.capacity();
			if (newBufferSize > bufferSize) {
				bufferSize = newBufferSize;
				GL15.glBufferData(target, newBuffer, hint);
			} else {
				GL15.glBufferSubData(target, 0, newBuffer);
			}
		});
	}
	public CharSequence getText() {
		return text;
	}
}
