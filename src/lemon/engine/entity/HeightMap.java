package lemon.engine.entity;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import lemon.engine.math.Triangle;
import lemon.engine.math.Vector3D;
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

	public HeightMap(float[][] map, float tileSize) {
		this.map = new float[map.length][map[0].length];
		this.tileSize = tileSize;
		for (int i = 0; i < map.length; ++i) {
			for (int j = 0; j < map[0].length; ++j) {
				this.map[i][j] = map[i][j];
			}
		}
		vertexArray = new VertexArray();
		vertexArray.bind(vao -> {
			(dataBuffer = vao.generateVbo()).bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
				FloatBuffer dataBuffer = BufferUtils.createFloatBuffer((6 * (map.length - 1) * (map[0].length - 1)) * 7);
				for (int i = 0; i < map.length - 1; ++i) {
					for (int j = 0; j < map[0].length - 1; ++j) {
						float avgHeight = (map[i][j] + map[i + 1][j] + map[i + 1][j + 1]) / 3;
						float avgHeight2 = (map[i][j] + map[i][j + 1] + map[i + 1][j + 1]) / 3;
						plotVertex(dataBuffer, i, j, 0, 0, avgHeight);
						plotVertex(dataBuffer, i, j, 1, 1, avgHeight);
						plotVertex(dataBuffer, i, j, 1, 0, avgHeight);
						plotVertex(dataBuffer, i, j, 0, 0, avgHeight2);
						plotVertex(dataBuffer, i, j, 0, 1, avgHeight2);
						plotVertex(dataBuffer, i, j, 1, 1, avgHeight2);
					}
				}
				dataBuffer.flip();
				GL15.glBufferData(target, dataBuffer, GL15.GL_DYNAMIC_DRAW);
				GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 7 * 4, 0);
				GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 7 * 4, 3 * 4);
			});
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
		});
	}
	public void update() {
		vertexArray.bind(vao -> {
			dataBuffer.bind(GL15.GL_ARRAY_BUFFER, (target, vbo) -> {
				FloatBuffer dataBuffer = BufferUtils.createFloatBuffer((6 * (map.length - 1) * (map[0].length - 1)) * 7);
				for (int i = 0; i < map.length - 1; ++i) {
					for (int j = 0; j < map[0].length - 1; ++j) {
						float avgHeight = (map[i][j] + map[i + 1][j] + map[i + 1][j + 1]) / 3;
						float avgHeight2 = (map[i][j] + map[i][j + 1] + map[i + 1][j + 1]) / 3;
						plotVertex(dataBuffer, i, j, 0, 0, avgHeight);
						plotVertex(dataBuffer, i, j, 1, 0, avgHeight);
						plotVertex(dataBuffer, i, j, 1, 1, avgHeight);
						plotVertex(dataBuffer, i, j, 0, 0, avgHeight2);
						plotVertex(dataBuffer, i, j, 0, 1, avgHeight2);
						plotVertex(dataBuffer, i, j, 1, 1, avgHeight2);
					}
				}
				dataBuffer.flip();
				GL15.glBufferData(target, dataBuffer, GL15.GL_DYNAMIC_DRAW);
			});
		});
	}
	public float get(int x, int y) {
		return map[x][y];
	}
	public void set(int x, int y, float value) {
		map[x][y] = value;
	}
	public int getWidth() {
		return map.length;
	}
	public int getHeight() {
		return map[0].length;
	}
	private void plotVertex(FloatBuffer dataBuffer, int i, int j, int offsetX, int offsetZ, float avgHeight) {
		dataBuffer.put((i + offsetX) * tileSize - (map.length * tileSize / 2) + tileSize / 2);
		dataBuffer.put(map[i + offsetX][j + offsetZ]);
		dataBuffer.put((j + offsetZ) * tileSize - (map[0].length * tileSize / 2) + tileSize / 2);
		// dataBuffer.put(avgHeight/20f+0.2f);
		// dataBuffer.put(avgHeight/5f+2f);
		// dataBuffer.put(1-(avgHeight)-5f);
		dataBuffer.put((avgHeight + 12f) / 20f);
		dataBuffer.put((avgHeight + 12f) / 20f);
		dataBuffer.put((avgHeight + 12f) / 20f);
		// dataBuffer.put(avgHeight);
		// dataBuffer.put(avgHeight);
		dataBuffer.put(1f);
	}
	public Triangle[][][] getTriangles() {
		Vector3D[][] vectors = new Vector3D[map.length][map[0].length];
		for (int i = 0; i < map.length; ++i) {
			for (int j = 0; j < map[0].length; ++j) {
				float x = i * tileSize - (map.length * tileSize / 2) + tileSize / 2;
				float y = map[i][j];
				float z = j * tileSize - (map[0].length * tileSize / 2) + tileSize / 2;
				vectors[i][j] = new Vector3D(x, y, z);
			}
		}
		Triangle[][][] triangles = new Triangle[map.length - 1][map[0].length - 1][2];
		for (int i = 0; i < map.length - 1; ++i) {
			for (int j = 0; j < map[0].length - 1; ++j) {
				Vector3D a = vectors[i][j];
				Vector3D b = vectors[i + 1][j];
				Vector3D c = vectors[i][j + 1];
				Vector3D d = vectors[i + 1][j + 1];
				triangles[i][j][0] = new Triangle(b, a, d);
				triangles[i][j][1] = new Triangle(a, c, d);
			}
		}
		return triangles;
	}
	@Override
	public void render() {
		vertexArray.bind(vao -> {
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6 * (map.length - 1) * (map[0].length - 1));
		});
	}
}
