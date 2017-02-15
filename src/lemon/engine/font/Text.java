package lemon.engine.font;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lemon.engine.font.Font.CharData;
import lemon.engine.render.Renderable;
import lemon.engine.render.VertexArray;

public class Text implements Renderable {
	private VertexArray vertexArray;
	private Font font;
	private String text;
	public Text(Font font, String text){
		if(text.isEmpty()){
			throw new IllegalStateException("Text is empty!");
		}
		this.font = font;
		this.text = text;
		vertexArray = new VertexArray();
		GL30.glBindVertexArray(vertexArray.getId());
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexArray.generateVbo().getId());
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.getFloatBuffer(), GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 4*4, 0);
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 4*4, 2*4);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
	}
	private FloatBuffer getFloatBuffer(){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(text.length()*4*6);
		char prevChar = text.charAt(0);
		int cursor = putChar(buffer, font.getCharData(prevChar), 0, 0);
		char currentChar;
		for(int i=1;i<text.length();++i){
			currentChar = text.charAt(i);
			cursor+=putChar(buffer, font.getCharData(currentChar), font.getKerning(prevChar, currentChar), cursor);
			prevChar = currentChar;
		}
		buffer.flip();
		return buffer;
	}
	private int putChar(FloatBuffer buffer, CharData data, int kerning, int cursor){
		float scaleWidth = font.getScaleWidth();
		float scaleHeight = font.getScaleHeight();
		float x = data.getX()/scaleWidth;
		float y = data.getY()/scaleHeight;
		float width = data.getWidth()/scaleWidth;
		float height = data.getHeight()/scaleHeight;
		put(buffer, cursor+data.getXOffset()+kerning, font.getLineHeight()-data.getYOffset(), x, y);
		put(buffer, cursor+data.getXOffset()+kerning+data.getWidth(), font.getLineHeight()-data.getYOffset(), x+width, y);
		put(buffer, cursor+data.getXOffset()+kerning+data.getWidth(), font.getLineHeight()-(data.getYOffset()+data.getHeight()), x+width, y+height);
		put(buffer, cursor+data.getXOffset()+kerning, font.getLineHeight()-data.getYOffset(), x, y);
		put(buffer, cursor+data.getXOffset()+kerning, font.getLineHeight()-(data.getYOffset()+data.getHeight()), x, y+height);
		put(buffer, cursor+data.getXOffset()+kerning+data.getWidth(), font.getLineHeight()-(data.getYOffset()+data.getHeight()), x+width, y+height);
		return data.getXAdvance()+kerning;
	}
	private void put(FloatBuffer buffer, float... floats){
		buffer.put(floats);
	}
	@Override
	public void render() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, font.getTexture().getId());
		GL30.glBindVertexArray(vertexArray.getId());
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, text.length()*6);
		GL30.glBindVertexArray(0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}
}
