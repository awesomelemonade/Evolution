package lemon.engine.draw;

import lemon.engine.font.CharData;
import lemon.engine.font.Font;
import lemon.engine.render.VertexArray;
import lemon.engine.render.VertexBuffer;
import lemon.engine.toolbox.Disposable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class TextModel implements Drawable, Disposable {
	private VertexArray vertexArray;
	private VertexBuffer vertexBuffer;
	private Font font;
	private CharSequence text;
	private int bufferSize;
	private int hint;
	private int padding;

	private FloatBuffer buffer;
	private int width;
	private int height;

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
		this.padding = 8;
		vertexArray = new VertexArray();
		vertexArray.bind(vao -> {
			vertexBuffer = new VertexBuffer();
			vertexBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
				this.calculateData(text);
				bufferSize = buffer.capacity();
				GL15.glBufferData(target, buffer, hint);
				GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 4 * 4, 0);
				GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 4 * 4, 2 * 4);
			});
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
		});
	}

	private void calculateData(CharSequence text) {
		int newCapacity = text.length() * 4 * 6;
		if (buffer == null || buffer.capacity() < newCapacity) {
			buffer = BufferUtils.createFloatBuffer(newCapacity);
		} else {
			buffer.clear();
		}
		char prevChar = '\0';
		int cursorX = padding;
		int cursorY = padding;
		int kerning = 0;
		CharData currentData = null;
		for (int i = 0; i < text.length(); ++i) {
			char currentChar = text.charAt(i);
			currentData = font.getCharData(currentChar);
			kerning = font.getKerning(prevChar, currentChar);
			putChar(buffer, currentData, cursorX + kerning, cursorY);
			cursorX += currentData.xAdvance() + font.getAdditionalKerning();
			prevChar = currentChar;
		}
		width = 2 * padding + (currentData == null ? 0 : (cursorX - currentData.xAdvance() + font.getAdditionalKerning() + currentData.width() + kerning));
		height = 2 * padding + font.getBase();
		buffer.flip();
	}

	private void putChar(FloatBuffer buffer, CharData data, int offsetX, int offsetY) {
		float scaleWidth = font.getScaleWidth();
		float scaleHeight = font.getScaleHeight();
		float textureX = data.x() / scaleWidth;
		float textureY = data.y() / scaleHeight;
		float textureWidth = data.width() / scaleWidth;
		float textureHeight = data.height() / scaleHeight;
		float x = offsetX; // offsetX + data.xOffset()
		float y = offsetY + font.getBase() - data.yOffset();
		float width = data.width();
		float height = data.height();

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
			this.calculateData(text);
			int newBufferSize = buffer.capacity();
			if (newBufferSize > bufferSize) {
				bufferSize = newBufferSize;
				GL15.glBufferData(target, buffer, hint);
			} else {
				GL15.glBufferSubData(target, 0, buffer);
			}
		});
	}

	public CharSequence getText() {
		return text;
	}

	public int width() {
		return width;
	}

	public int height() {
		return height;
	}

	@Override
	public void dispose() {
		vertexBuffer.dispose();
		vertexArray.dispose();
	}
}
