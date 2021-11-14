package lemon.evolution.destructible.beta;

import com.google.common.collect.ImmutableList;
import lemon.engine.draw.DrawableData;
import lemon.engine.draw.DynamicIndexedDrawable;
import lemon.engine.event.Computable;
import lemon.engine.math.FloatData;
import lemon.engine.math.Matrix;
import lemon.engine.math.MutableVector3D;
import lemon.engine.math.Triangle;
import lemon.engine.math.Vector3D;
import lemon.evolution.pool.MatrixPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TerrainChunk {
	public static final int SIZE = 32;
	public static final int TRIANGLES_SUBDIVISION_SIZE = 8;
	public static final int TRIANGLE_COORDS_TO_SUBDIVISION_COORDS = SIZE / TRIANGLES_SUBDIVISION_SIZE;
	public static final Vector3D MARCHING_CUBE_SIZE = Vector3D.of(SIZE + 1, SIZE + 1, SIZE + 1);
	public static final int NUM_TEXTURES = 48;
	private static final float[] ZERO_TEXTURE_WEIGHTS = new float[NUM_TEXTURES];
	private final Terrain terrain;
	private final int chunkX;
	private final int chunkY;
	private final int chunkZ;
	private final MarchingCube marchingCube;
	private final Matrix transformationMatrix;
	private final Computable<float[][][]> data;
	private final Computable<SparseGrid3D<float[]>> textureData;
	private static final int[] MESH_PREREQUISITE_CHUNK_OFFSET_X = {1, 0, 0, 1, 0, 1, 1};
	private static final int[] MESH_PREREQUISITE_CHUNK_OFFSET_Y = {0, 1, 0, 1, 1, 0, 1};
	private static final int[] MESH_PREREQUISITE_CHUNK_OFFSET_Z = {0, 0, 1, 0, 1, 1, 1};
	private final Computable<MarchingCubeMesh> mesh;
	private final Computable<MarchingCubeModel> model;
	private static final int[] NORMALS_PREREQUISITE_CHUNK_OFFSET_X = {-1,  0,  0,  0,  1, -1, -1, -1,  0, 0,  1, 1, 1, -1,  0, 0, 0, 1};
	private static final int[] NORMALS_PREREQUISITE_CHUNK_OFFSET_Y = { 0, -1,  0,  1,  0, -1,  0,  1, -1, 1, -1, 0, 1,  0, -1, 0, 1, 0};
	private static final int[] NORMALS_PREREQUISITE_CHUNK_OFFSET_Z = {-1, -1, -1, -1, -1,  0,  0,  0,  0, 0,  0, 0, 0,  1,  1, 1, 1, 1};
	private final Computable<MarchingCubeNormals> normals;
	private final Computable<DrawableData> drawableData;
	private final Computable<DynamicIndexedDrawable> drawable;
	private final Executor poolExecutor;

	public TerrainChunk(Terrain terrain,
						int chunkX,
						int chunkY,
						int chunkZ,
						BoundedScalarGrid3D scalarGrid,
						BoundedGrid3D<float[]> textureWeightsGrid,
						TerrainGenerator generator,
						Executor poolExecutor,
						Executor mainThreadExecutor) {
		this.poolExecutor = poolExecutor;
		var scalar = terrain.scalar();
		this.terrain = terrain;
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.chunkZ = chunkZ;
		this.marchingCube = new MarchingCube(scalarGrid, textureWeightsGrid, MARCHING_CUBE_SIZE, 0f);
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
		this.textureData = new Computable<>(computable -> {
			computable.compute(new SparseGrid3D<>(TerrainChunk.SIZE, TerrainChunk.SIZE, TerrainChunk.SIZE, () -> new float[NUM_TEXTURES]));
		});
		this.mesh = Computable.all(poolExecutor, () -> {
			// this.data computable + 7 additional neighbors
			return Stream.concat(Stream.of(this),
					IntStream.range(0, MESH_PREREQUISITE_CHUNK_OFFSET_X.length)
							.mapToObj(i -> getNeighboringChunk(MESH_PREREQUISITE_CHUNK_OFFSET_X[i],
									MESH_PREREQUISITE_CHUNK_OFFSET_Y[i], MESH_PREREQUISITE_CHUNK_OFFSET_Z[i])))
					.<Computable<?>>mapMulti((chunk, consumer) -> {
						consumer.accept(chunk.textureData());
						consumer.accept(chunk.data());
					}).toList();
		}, computable -> {
			computable.compute(marchingCube.generateMesh());
		});
		this.model = this.mesh.then(poolExecutor, (computable, mesh) -> {
			var vertices = mesh.vertices();
			var textureWeights = mesh.textureWeights();
			var indices = mesh.indices();
			var hashes = mesh.prenormalHashes();
			var triangleCoords = mesh.triangleCoords();
			var preNormals = new PreNormals();
			SparseGrid3D<List<Triangle>> triangles = new SparseGrid3D<>(TRIANGLES_SUBDIVISION_SIZE, TRIANGLES_SUBDIVISION_SIZE, TRIANGLES_SUBDIVISION_SIZE, ArrayList::new);
			Vector3D[] transformed = new Vector3D[vertices.length];
			var x = Vector3D.of(chunkX * TerrainChunk.SIZE, chunkY * TerrainChunk.SIZE, chunkZ * TerrainChunk.SIZE);
			for (int i = 0; i < vertices.length; i++) {
				transformed[i] = vertices[i].add(x).multiply(scalar);
			}
			for (int i = 0; i < indices.length; i += 3) {
				Vector3D a = transformed[indices[i]];
				Vector3D b = transformed[indices[i + 2]];
				Vector3D c = transformed[indices[i + 1]];
				Triangle triangle = Triangle.of(a, b, c);
				float area = triangle.area();
				if (area > 0f) {
					float weight = 1f / area;
					var scaledNormal = triangle.normal().multiply(weight);
					preNormals.addNormal(hashes[indices[i]], scaledNormal);
					preNormals.addNormal(hashes[indices[i + 1]], scaledNormal);
					preNormals.addNormal(hashes[indices[i + 2]], scaledNormal);
					var coords = triangleCoords[i / 3];
					triangles.compute(coords.x() / TRIANGLE_COORDS_TO_SUBDIVISION_COORDS,
							coords.y() / TRIANGLE_COORDS_TO_SUBDIVISION_COORDS,
							coords.z() / TRIANGLE_COORDS_TO_SUBDIVISION_COORDS).add(triangle);
				}
			}
			computable.compute(new MarchingCubeModel(indices, vertices, textureWeights, hashes, preNormals, triangles));
		});
		this.normals = Computable.all(() -> {
			// this.model computable + 18 additional neighbors
			return Stream.concat(Stream.of(this),
					IntStream.range(0, NORMALS_PREREQUISITE_CHUNK_OFFSET_X.length)
							.mapToObj(i -> getNeighboringChunk(NORMALS_PREREQUISITE_CHUNK_OFFSET_X[i],
									NORMALS_PREREQUISITE_CHUNK_OFFSET_Y[i], NORMALS_PREREQUISITE_CHUNK_OFFSET_Z[i])))
					.map(TerrainChunk::model).toList();
		}, computable -> {
			var model = this.model.getValueOrThrow();
			var preNormals = model.preNormals();
			var normals = Arrays.stream(model.hashes()).mapToObj(hash -> {
				var preNormal = preNormals.getNormal(hash);
				return getBorderingPreNormal(hash).map(preNormal::add).orElse(preNormal);
			}).map(Vector3D::normalize).toArray(Vector3D[]::new);
			computable.compute(new MarchingCubeNormals(model, normals));
		});
		this.drawableData = this.normals.then(poolExecutor, (computable, normals) -> {
			var model = normals.model(); // Normals MUST be the same as the model
			// (cannot use this.model.getValueOrThrow() because model could have changed already and desync with normals)
			var indices = model.indices();
			var vertices = model.vertices();
			var textureWeights = model.textureWeights();
			var numVec4s = NUM_TEXTURES / 4;
			var weights = new FloatData[numVec4s][];
			for (int i = 0; i < numVec4s; i++) {
				int finalI = i;
				weights[i] = Arrays.stream(textureWeights)
						.map(array -> FloatData.of(array, 4 * finalI, 4))
						.toArray(FloatData[]::new);
			}
			var vertexData = new FloatData[2 + numVec4s][];
			vertexData[0] = vertices;
			vertexData[1] = normals.normals();
			for (int i = 0; i < numVec4s; i++) {
				vertexData[2 + i] = weights[i];
			}
			computable.compute(new DrawableData(indices, vertexData));
		});
		this.drawable = this.drawableData.then((computable, data) -> {
			mainThreadExecutor.execute(() -> {
				computable.compute(drawable -> {
					drawable.setData(data);
					return drawable;
				}, () -> new DynamicIndexedDrawable(data));
			});
		});
	}

	private static final int mask = 0b11111111;
	private Optional<Vector3D> getBorderingPreNormal(int hash) {
		var x = (hash >>> 24) & mask;
		var y = (hash >>> 16) & mask;
		var z = (hash >>> 8) & mask;
		var w = hash & mask;
		if (w == 0) {
			MutableVector3D sum = MutableVector3D.ofZero();
			boolean borderY = (y == 0 || y == SIZE);
			boolean borderZ = (z == 0 || z == SIZE);
			int chunkOffsetY = y == 0 ? -1 : 1;
			int chunkOffsetZ = z == 0 ? -1 : 1;
			if (borderY) {
				sum.add(getNeighboringChunk(0, chunkOffsetY, 0).model().getValueOrThrow().preNormals().getNormal(x, SIZE - y, z, w));
			}
			if (borderZ) {
				sum.add(getNeighboringChunk(0, 0, chunkOffsetZ).model().getValueOrThrow().preNormals().getNormal(x, y, SIZE - z, w));
			}
			if (borderY && borderZ) {
				sum.add(getNeighboringChunk(0, chunkOffsetY, chunkOffsetZ).model().getValueOrThrow().preNormals().getNormal(x, SIZE - y, SIZE - z, w));
			}
			return Optional.of(sum.asImmutable());
		}
		if (w == 1) {
			MutableVector3D sum = MutableVector3D.ofZero();
			boolean borderX = (x == 0 || x == SIZE);
			boolean borderZ = (z == 0 || z == SIZE);
			int chunkOffsetX = x == 0 ? -1 : 1;
			int chunkOffsetZ = z == 0 ? -1 : 1;
			if (borderX) {
				sum.add(getNeighboringChunk(chunkOffsetX, 0, 0).model().getValueOrThrow().preNormals().getNormal(SIZE - x, y, z, w));
			}
			if (borderZ) {
				sum.add(getNeighboringChunk(0, 0, chunkOffsetZ).model().getValueOrThrow().preNormals().getNormal(x, y, SIZE - z, w));
			}
			if (borderX && borderZ) {
				sum.add(getNeighboringChunk(chunkOffsetX, 0, chunkOffsetZ).model().getValueOrThrow().preNormals().getNormal(SIZE - x, y, SIZE - z, w));
			}
			return Optional.of(sum.asImmutable());
		}
		if (w == 2) {
			MutableVector3D sum = MutableVector3D.ofZero();
			boolean borderX = (x == 0 || x == SIZE);
			boolean borderY = (y == 0 || y == SIZE);
			int chunkOffsetX = x == 0 ? -1 : 1;
			int chunkOffsetY = y == 0 ? -1 : 1;
			if (borderX) {
				sum.add(getNeighboringChunk(chunkOffsetX, 0, 0).model().getValueOrThrow().preNormals().getNormal(SIZE - x, y, z, w));
			}
			if (borderY) {
				sum.add(getNeighboringChunk(0, chunkOffsetY, 0).model().getValueOrThrow().preNormals().getNormal(x, SIZE - y, z, w));
			}
			if (borderX && borderY) {
				sum.add(getNeighboringChunk(chunkOffsetX, chunkOffsetY, 0).model().getValueOrThrow().preNormals().getNormal(SIZE - x, SIZE - y, z, w));
			}
			return Optional.of(sum.asImmutable());
		}
		return Optional.empty();
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

	public float[] getTextureWeights(int x, int y, int z) {
		return textureData.getValueOrThrow(() -> new IllegalStateException("TextureData has not been computed for " + this)).getOrDefault(x, y, z, ZERO_TEXTURE_WEIGHTS);
	}

	public void updateData(Consumer<float[][][]> updater) {
		data.compute(updater);
	}

	public void updateTextureData(Consumer<SparseGrid3D<float[]>> updater) {
		textureData.compute(updater);
	}

	public void updateAllData(BiConsumer<float[][][], SparseGrid3D<float[]>> updater) {
		poolExecutor.execute(() -> {
			this.data.compute(c -> {
				this.textureData.compute(d -> {
					updater.accept(c, d);
				});
			});
		});
	}

	public Computable<DynamicIndexedDrawable> drawable() {
		return drawable;
	}

	public Computable<MarchingCubeModel> model() {
		return model;
	}

	public Computable<float[][][]> data() {
		return data;
	}

	public Computable<SparseGrid3D<float[]>> textureData() {
		return textureData;
	}

	public Matrix getTransformationMatrix() {
		return transformationMatrix;
	}

	public List<Triangle> getTriangles(int x, int y, int z) {
		return model.getValue().map(model -> model.triangles().getOrDefault(x, y, z, ImmutableList.of())).orElse(ImmutableList.of());
	}

	public TerrainChunk getNeighboringChunk(int offsetX, int offsetY, int offsetZ) {
		return terrain.getChunk(chunkX + offsetX, chunkY + offsetY, chunkZ + offsetZ);
	}

	@Override
	public String toString() {
		return String.format("TerrainChunk[%d, %d, %d]", chunkX, chunkY, chunkZ);
	}
}
