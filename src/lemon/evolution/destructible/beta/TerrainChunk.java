package lemon.evolution.destructible.beta;

import lemon.engine.draw.DynamicIndexedDrawable;
import lemon.engine.event.Computable;
import lemon.engine.math.HasDataArray;
import lemon.engine.math.Matrix;
import lemon.engine.math.Triangle;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Color;
import lemon.evolution.pool.MatrixPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TerrainChunk {
	public static final int SIZE = 16;
	public static final Vector3D MARCHING_CUBE_SIZE = new Vector3D(SIZE + 1, SIZE + 1, SIZE + 1);
	private final Terrain terrain;
	private final int chunkX;
	private final int chunkY;
	private final int chunkZ;
	private final MarchingCube marchingCube;
	private final Matrix transformationMatrix;
	private final Color color;
	private final Computable<float[][][]> data;
	private static final int[] MESH_PREREQUISITE_CHUNK_OFFSET_X = {1, 0, 0, 1, 0, 1, 1};
	private static final int[] MESH_PREREQUISITE_CHUNK_OFFSET_Y = {0, 1, 0, 1, 1, 0, 1};
	private static final int[] MESH_PREREQUISITE_CHUNK_OFFSET_Z = {0, 0, 1, 0, 1, 1, 1};
	private final Computable<MarchingCubeMesh> mesh;
	private final Computable<MarchingCubeModel> model;
	private final Computable<Vector3D[]> normals;
	private final Computable<DynamicIndexedDrawable> drawable;

	public TerrainChunk(int chunkX, int chunkY, int chunkZ, BoundedScalarGrid3D scalarGrid, Vector3D scalar,
						TerrainGenerator generator, Executor mainThreadExecutor, Terrain terrain) {
		this.terrain = terrain;
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.chunkZ = chunkZ;
		this.color = Color.randomOpaque();
		this.marchingCube = new MarchingCube(scalarGrid, MARCHING_CUBE_SIZE, 0f);
		this.transformationMatrix = new Matrix(4);
		try (var translationMatrix = MatrixPool.ofTranslation(
				scalar.x() * chunkX * TerrainChunk.SIZE,
				scalar.y() * chunkY * TerrainChunk.SIZE,
				scalar.z() * chunkZ * TerrainChunk.SIZE);
			 var scalarMatrix = MatrixPool.ofScalar(scalar)) {
			Matrix.multiply(transformationMatrix, translationMatrix, scalarMatrix);
		}
		this.data = new Computable<>(computable -> {
			generator.queueChunk(TerrainChunk.this, computable::compute);
		});
		this.mesh = Computable.all(() -> {
			// this.data computable + 7 additional neighbors
			return Stream.concat(Stream.of(this),
					IntStream.range(0, MESH_PREREQUISITE_CHUNK_OFFSET_X.length)
					.mapToObj(i -> getNeighboringChunk(MESH_PREREQUISITE_CHUNK_OFFSET_X[i],
							MESH_PREREQUISITE_CHUNK_OFFSET_Y[i], MESH_PREREQUISITE_CHUNK_OFFSET_Z[i])))
					.map(x -> x.data).toList();
		}, computable -> {
			computable.compute(marchingCube.generateMesh());
		});
		this.model = this.mesh.then((computable, mesh) -> {
			Vector3D[] vertices = mesh.getVertices();
			int[] indices = mesh.getIndices();
			int[] hashes = mesh.getHashes();
			PreNormals preNormals = new PreNormals();
			List<Triangle> triangles = new ArrayList<>();
			Vector3D[] transformed = new Vector3D[vertices.length];
			var x = new Vector3D(5f * chunkX * TerrainChunk.SIZE, 5f * chunkY * TerrainChunk.SIZE, 5f * chunkZ * TerrainChunk.SIZE);
			for (int i = 0; i < vertices.length; i++) {
				transformed[i] = vertices[i].multiply(5f).add(x);
			}
			for (int i = 0; i < indices.length; i += 3) {
				Vector3D a = transformed[indices[i]];
				Vector3D b = transformed[indices[i + 2]];
				Vector3D c = transformed[indices[i + 1]];
				Triangle triangle = new Triangle(a, b, c);
				float area = triangle.area();
				if (area > 0f) {
					float weight = 1f / area;
					var scaledNormal = triangle.normal().multiply(weight);
					preNormals.addNormal(hashes[indices[i]], scaledNormal);
					preNormals.addNormal(hashes[indices[i + 1]], scaledNormal);
					preNormals.addNormal(hashes[indices[i + 2]], scaledNormal);
					triangles.add(triangle);
				}
			}
			computable.compute(new MarchingCubeModel(vertices, indices, hashes, preNormals, triangles));
		});
		this.normals = Computable.all(() -> List.of(this.model), computable -> {
			MarchingCubeModel model = this.model.getValueOrThrow();
			PreNormals preNormals = model.getPreNormals();
			computable.compute(Arrays.stream(model.getHashes()).mapToObj(preNormals::getNormal).toArray(Vector3D[]::new));
		});
		this.drawable = this.normals.then((computable, normals) -> {
			MarchingCubeModel model = this.model.getValueOrThrow();
			int[] indices = model.getIndices();
			Vector3D[] vertices = model.getVertices();
			Color[] colors = new Color[vertices.length];
			Arrays.fill(colors, color);
			var vertexData = new HasDataArray[][] {vertices, colors, normals};
			mainThreadExecutor.execute(() -> {
				computable.compute(drawable -> {
					drawable.setData(indices, vertexData);
					return drawable;
				}, () -> new DynamicIndexedDrawable(indices, vertexData));
			});
		});
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
		return data.getValueOrThrow(() -> new IllegalStateException("Data has not been computed for " + this))[x][y][z];
	}

	public void updateData(Consumer<float[][][]> updater) {
		data.compute(updater);
	}

	public Computable<DynamicIndexedDrawable> getDrawable() {
		return drawable;
	}

	public Matrix getTransformationMatrix() {
		return transformationMatrix;
	}

	public Optional<List<Triangle>> getTriangles() {
		return model.getValue().map(MarchingCubeModel::getTriangles);
	}

	public TerrainChunk getNeighboringChunk(int offsetX, int offsetY, int offsetZ) {
		return terrain.getChunk(chunkX + offsetX, chunkY + offsetY, chunkZ + offsetZ);
	}

	@Override
	public String toString() {
		return String.format("TerrainChunk[%d, %d, %d]", chunkX, chunkY, chunkZ);
	}
}
