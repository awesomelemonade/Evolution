package lemon.evolution.destructible.beta;

import lemon.engine.draw.DynamicIndexedDrawable;
import lemon.engine.math.Matrix;
import lemon.engine.math.Triangle;
import lemon.engine.math.Vector3D;
import lemon.engine.model.Model;
import lemon.engine.model.ModelBuilder;
import lemon.engine.toolbox.Color;
import lemon.evolution.pool.MatrixPool;
import lemon.evolution.pool.VectorPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TerrainChunk {
	public static final int SIZE = 16;
	public static final Vector3D MARCHING_CUBE_SIZE = new Vector3D(SIZE + 1, SIZE + 1, SIZE + 1);
	private int chunkX;
	private int chunkY;
	private int chunkZ;
	private float[][][] data;
	private volatile boolean generated;
	private MarchingCube marchingCube;
	private Model model;
	private DynamicIndexedDrawable drawable;
	private Matrix transformationMatrix;
	private Color color;
	private boolean queuedForUpdate;
	private boolean queuedForConstruction;
	private boolean hasBeenConstructed;
	private List<Triangle> triangles;
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
			 var scalarMatrix = MatrixPool.ofScalar(scalar)) {
			Matrix.multiply(transformationMatrix, translationMatrix, scalarMatrix);
		}
		this.color = Color.randomOpaque();
		this.triangles = new ArrayList<>();
		this.queuedForUpdate = false;
		this.queuedForConstruction = false;
		this.hasBeenConstructed = false;
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
	public void generateModel() {
		ModelBuilder builder = new ModelBuilder();
		marchingCube.addVertices(builder);
		model = builder.build((indices, vertices) -> {
			List<Triangle> triangles = new ArrayList<>();
			Vector3D[] transformed = new Vector3D[vertices.length];

			try (var x = VectorPool.of(5f * chunkX * TerrainChunk.SIZE,
					5f * chunkY * TerrainChunk.SIZE, 5f * chunkZ * TerrainChunk.SIZE)) {
				for (int i = 0; i < vertices.length; i++) {
					transformed[i] = vertices[i].copy().multiply(5f).add(x);
				}
			}

			Vector3D[] normals = new Vector3D[vertices.length];
			for (int i = 0; i < normals.length; i++) {
				normals[i] = Vector3D.ZERO.copy();
			}
			for (int i = 0; i < indices.length; i += 3) {
				Vector3D a = transformed[indices[i]];
				Vector3D b = transformed[indices[i + 2]];
				Vector3D c = transformed[indices[i + 1]];
				Triangle triangle = new Triangle(a, b, c);
				float area = triangle.getArea();
				if (area > 0f) {
					float weight = 1f / area;
					try (var scaledNormal = VectorPool.of(triangle.getNormal(), x -> x.multiply(weight))) {
						normals[indices[i]].add(scaledNormal);
						normals[indices[i + 1]].add(scaledNormal);
						normals[indices[i + 2]].add(scaledNormal);
					}
					triangles.add(triangle);
				}
			}
			for (int i = 0; i < normals.length; i++) {
				normals[i].normalize();
			}
			this.triangles = triangles;
			Color[] colors = new Color[vertices.length];
			Arrays.fill(colors, color);
			return new Model(indices, vertices, colors, normals);
		});
	}
	public Model getModel() {
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
		this.hasBeenConstructed = this.hasBeenConstructed || queuedForConstruction;
	}
	public boolean isQueuedForConstruction() {
		return queuedForConstruction;
	}
	public boolean hasBeenConstructed() {
		return hasBeenConstructed;
	}
	public Matrix getTransformationMatrix() {
		return transformationMatrix;
	}
	public List<Triangle> getTriangles() {
		return triangles;
	}
}
