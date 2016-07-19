package lemon.engine.entity;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import lemon.engine.render.Renderable;
import lemon.engine.render.VertexArray;
import lemon.engine.render.VertexBuffer;

public class HeightMap implements Renderable {
	private VertexArray vertexArray;
	private VertexBuffer dataBuffer;
	private float[][] map;
	private float tileSize;
	public HeightMap(float[][] map, float tileSize){
		this.map = new float[map.length][map[0].length];
		this.tileSize = tileSize;
		for(int i=0;i<map.length;++i){
			for(int j=0;j<map[0].length;++j){
				this.map[i][j] = map[i][j];
			}
		}
		vertexArray = new VertexArray();
		GL30.glBindVertexArray(vertexArray.getId());
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, (dataBuffer=vertexArray.generateVbo()).getId());
		FloatBuffer dataBuffer = BufferUtils.createFloatBuffer((6*(map.length-1)*(map[0].length-1))*7);
		for(int i=0;i<map.length-1;++i){
			for(int j=0;j<map[0].length-1;++j){
				float avgHeight = (map[i][j]+map[i+1][j]+map[i+1][j+1])/3;
				float avgHeight2 = (map[i][j]+map[i][j+1]+map[i+1][j+1])/3;
				plotVertex(dataBuffer, i, j, 0, 0, avgHeight);
				plotVertex(dataBuffer, i, j, 1, 1, avgHeight);
				plotVertex(dataBuffer, i, j, 1, 0, avgHeight);
				plotVertex(dataBuffer, i, j, 0, 0, avgHeight2);
				plotVertex(dataBuffer, i, j, 0, 1, avgHeight2);
				plotVertex(dataBuffer, i, j, 1, 1, avgHeight2);
			}
		}
		dataBuffer.flip();
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, dataBuffer, GL15.GL_DYNAMIC_DRAW);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 7*4, 0);
		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 7*4, 3*4);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
	}
	public void update(){
		GL30.glBindVertexArray(vertexArray.getId());
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, dataBuffer.getId());
		FloatBuffer dataBuffer = BufferUtils.createFloatBuffer((6*(map.length-1)*(map[0].length-1))*7);
		for(int i=0;i<map.length-1;++i){
			for(int j=0;j<map[0].length-1;++j){
				float avgHeight = (map[i][j]+map[i+1][j]+map[i+1][j+1])/3;
				float avgHeight2 = (map[i][j]+map[i][j+1]+map[i+1][j+1])/3;
				plotVertex(dataBuffer, i, j, 0, 0, avgHeight);
				plotVertex(dataBuffer, i, j, 1, 0, avgHeight);
				plotVertex(dataBuffer, i, j, 1, 1, avgHeight);
				plotVertex(dataBuffer, i, j, 0, 0, avgHeight2);
				plotVertex(dataBuffer, i, j, 0, 1, avgHeight2);
				plotVertex(dataBuffer, i, j, 1, 1, avgHeight2);
			}
		}
		dataBuffer.flip();
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, dataBuffer, GL15.GL_DYNAMIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
	}
	public float get(int x, int y){
		return map[x][y];
	}
	public void set(int x, int y, float value){
		map[x][y] = value;
	}
	public int getWidth(){
		return map.length;
	}
	public int getHeight(){
		return map[0].length;
	}
	private void plotVertex(FloatBuffer dataBuffer, int i, int j, int offsetX, int offsetZ, float avgHeight){
		dataBuffer.put((i+offsetX)*tileSize-(map.length*tileSize/2)+tileSize/2);
		dataBuffer.put(map[i+offsetX][j+offsetZ]);
		dataBuffer.put((j+offsetZ)*tileSize-(map[0].length*tileSize/2)+tileSize/2);
		//dataBuffer.put(avgHeight/20f+0.2f);
		//dataBuffer.put(avgHeight/5f+2f);
		//dataBuffer.put(1-(avgHeight)-5f);
		dataBuffer.put((avgHeight+12f)/20f);
		dataBuffer.put((avgHeight+12f)/20f);
		dataBuffer.put((avgHeight+12f)/20f);
		//dataBuffer.put(avgHeight);
		//dataBuffer.put(avgHeight);
		dataBuffer.put(1f);
	}
	@Override
	public void render(){
		GL30.glBindVertexArray(vertexArray.getId());
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6*(map.length-1)*(map[0].length-1));
		GL30.glBindVertexArray(0);
	}
}
