package lemon.evolution.destructible.beta;

public class TerrainChunk {
	public static final int SIZE = 16;
	private float[][][] data;
	public TerrainChunk() {
		this.data = new float[SIZE][SIZE][SIZE];
	}
	public float get(int x, int y, int z) {
		return data[x][y][z];
	}
	public float[][][] getData() {
		return data;
	}
}
