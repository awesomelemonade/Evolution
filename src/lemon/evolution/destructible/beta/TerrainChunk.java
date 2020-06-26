package lemon.evolution.destructible.beta;

import lemon.engine.draw.DynamicIndexedDrawable;
import lemon.engine.math.MathUtil;
import lemon.engine.math.Matrix;
import lemon.engine.math.Vector3D;
import lemon.engine.model.ColoredModel;
import lemon.engine.toolbox.Color;

import java.util.Optional;
import java.util.function.Supplier;

public class TerrainChunk {
	public static final int SIZE = 16;
	private int chunkX;
	private int chunkY;
	private int chunkZ;
	private float[][][] data;
	private volatile boolean generated;
	private MarchingCube marchingCube;
	private DynamicIndexedDrawable drawable;
	private Matrix transformationMatrix;
	private Color color;
	private boolean queuedForUpdate;
	public TerrainChunk(int chunkX, int chunkY, int chunkZ, BoundedScalarGrid3D scalarGrid, Vector3D size, Vector3D scalar) {
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.chunkZ = chunkZ;
		this.data = new float[SIZE][SIZE][SIZE];
		this.generated = false;
		this.marchingCube = new MarchingCube(scalarGrid, size, 0f);
		Vector3D translation = new Vector3D(
				scalar.getX() * chunkX * TerrainChunk.SIZE,
				scalar.getY() * chunkY * TerrainChunk.SIZE,
				scalar.getZ() * chunkZ * TerrainChunk.SIZE
		);
		this.transformationMatrix = MathUtil.getTranslation(translation)
				.multiply(MathUtil.getScalar(scalar));
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
	public ColoredModel getColoredModel() {
		return marchingCube.getColoredModel(color);
	}
	public Optional<DynamicIndexedDrawable> getDrawableOrSet(Supplier<Optional<DynamicIndexedDrawable>> supplier) {
		if (drawable != null) {
			return Optional.of(drawable);
		}
		supplier.get().ifPresent(drawable -> {
			this.drawable = drawable;
		});
		return Optional.ofNullable(drawable);
	}
	public Optional<DynamicIndexedDrawable> getDrawable() {
		return Optional.ofNullable(drawable);
	}
	public void setQueuedForUpdate(boolean queuedForUpdate) {
		this.queuedForUpdate = queuedForUpdate;
	}
	public boolean isQueuedForUpdate() {
		return queuedForUpdate;
	}
	public Matrix getTransformationMatrix() {
		return transformationMatrix;
	}
}
