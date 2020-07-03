package lemon.evolution.destructible.beta;

import lemon.engine.draw.DynamicIndexedDrawable;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;
import lemon.engine.model.ColoredModel;
import lemon.engine.toolbox.Color;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.pool.VectorPool;

public class TerrainChunk {
	public static final int SIZE = 16;
	public static final Vector3D MARCHING_CUBE_SIZE = new Vector3D(SIZE + 1, SIZE + 1, SIZE + 1);
	private int chunkX;
	private int chunkY;
	private int chunkZ;
	private float[][][] data;
	private volatile boolean generated;
	private MarchingCube marchingCube;
	private ColoredModel model;
	private DynamicIndexedDrawable drawable;
	private Matrix transformationMatrix;
	private Color color;
	private boolean queuedForUpdate;
	private boolean queuedForConstruction;
	public TerrainChunk(int chunkX, int chunkY, int chunkZ, BoundedScalarGrid3D scalarGrid, Vector3D scalar) {
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.chunkZ = chunkZ;
		this.data = new float[SIZE][SIZE][SIZE];
		this.generated = false;
		this.marchingCube = new MarchingCube(scalarGrid, MARCHING_CUBE_SIZE, 0f);
		this.transformationMatrix = new Matrix(4);
		try (var translationMatrix = MatrixPool.ofTranslation(
				scalar.getX() * chunkX * TerrainChunk.SIZE,
				scalar.getY() * chunkY * TerrainChunk.SIZE,
				scalar.getZ() * chunkZ * TerrainChunk.SIZE);
			 var scalarMatrix = MatrixPool.ofScalar(scalar);) {
			Matrix.multiply(transformationMatrix, translationMatrix, scalarMatrix);
		}
		this.color = Color.randomOpaque();
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
	public void generateColoredModel() {
		model = marchingCube.getColoredModel(color);
	}
	public ColoredModel getColoredModel() {
		return model;
	}
	public void setDrawable(DynamicIndexedDrawable drawable) {
		this.drawable = drawable;
	}
	public DynamicIndexedDrawable getDrawable() {
		return drawable;
	}
	public void setQueuedForUpdate(boolean queuedForUpdate) {
		this.queuedForUpdate = queuedForUpdate;
	}
	public boolean isQueuedForUpdate() {
		return queuedForUpdate;
	}
	public void setQueuedForConstruction(boolean queuedForConstruction) {
		this.queuedForConstruction = queuedForConstruction;
	}
	public boolean isQueuedForConstruction() {
		return queuedForConstruction;
	}
	public Matrix getTransformationMatrix() {
		return transformationMatrix;
	}
}
