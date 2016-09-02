package lemon.engine.font;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lemon.engine.font.Font.CharData;
import lemon.engine.game2d.Box2D;
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
	public FloatBuffer getFloatBuffer(){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(text.length()*4*6);
		float cursor = 0f;
		for(int i=0;i<text.length();++i){
			char c = text.charAt(i);
			Box2D box = font.getCharBox(c);
			CharData data = font.getCharData(c);
			put(buffer, cursor+data.getXOffset(), -data.getYOffset()-data.getHeight(), box.getX(), box.getY()+box.getHeight());
			put(buffer, cursor+data.getXOffset(), -data.getYOffset(), box.getX(), box.getY());
			put(buffer, cursor+data.getXOffset()+data.getWidth(), -data.getYOffset()-data.getHeight(), box.getX()+box.getWidth(), box.getY()+box.getHeight());
			put(buffer, cursor+data.getXOffset(), -data.getYOffset(), box.getX(), box.getY());
			put(buffer, cursor+data.getXOffset()+data.getWidth(), -data.getYOffset()-data.getHeight(), box.getX()+box.getWidth(), box.getY()+box.getHeight());
			put(buffer, cursor+data.getXOffset()+data.getWidth(), -data.getYOffset(), box.getX()+box.getWidth(), box.getY());
			cursor+=data.getXAdvance();
		}
		buffer.flip();
		return buffer;
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
