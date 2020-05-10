package lemon.evolution.destructible.beta;

public class TerrainChunk {
	public static final int SIZE = 16;
	private int chunkX;
	private int chunkY;
	private int chunkZ;
	private float[][][] data;
	private volatile boolean generated;
	public TerrainChunk(int chunkX, int chunkY, int chunkZ) {
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.chunkZ = chunkZ;
		this.data = new float[SIZE][SIZE][SIZE];
		this.generated = false;
	}
	public int getChunkX() {
		return chunkX;
	}
	public int getChunkY() {
		return chunkY;
	}
	public int getChunkZ() {
		return chunkZ;
	}
	public float get(int x, int y, int z) {
		return data[x][y][z];
	}
	public float[][][] getData() {
		return data;
	}
	public void setGenerated(boolean generated) {
		this.generated = generated;
	}
	public boolean isGenerated() {
		return generated;
	}
}
