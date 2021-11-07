package lemon.evolution.destructible.beta;

import lemon.engine.math.Vector3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarchingCube {
	private final float[] offsets; // [offsetX, offsetY, offsetZ]
	private final float[] strides; // [strideX, strideY, strideZ]
	private final float threshold;
	private final BoundedScalarGrid3D grid;
	private final BoundedGrid3D<float[]> textureWeightsGrid;

	public MarchingCube(BoundedScalarGrid3D grid, BoundedGrid3D<float[]> textureWeightsGrid, Vector3D size, float threshold) {
		this.grid = grid;
		this.textureWeightsGrid = textureWeightsGrid;
		this.threshold = threshold;
		this.offsets = new float[] {0f, 0f, 0f};
		this.strides = new float[] {
				size.x() / grid.getSizeX(),
				size.y() / grid.getSizeY(),
				size.z() / grid.getSizeZ()
		};
	}

	public MarchingCubeMesh generateMesh() {
		List<Integer> indices = new ArrayList<>();
		List<Vector3D> vertices = new ArrayList<>();
		List<float[]> textureWeights = new ArrayList<>();
		List<Integer> prenormalHashes = new ArrayList<>();
		Map<Long, Integer> edgeIndices = new HashMap<>();
		List<TripleIndex> triangleCoords = new ArrayList<>();
		int[] vectorIndices = new int[12];
		for (int i = 0; i < grid.getSizeX() - 1; i++) {
			for (int j = 0; j < grid.getSizeY() - 1; j++) {
				for (int k = 0; k < grid.getSizeZ() - 1; k++) {
					int index = getIndex(i, j, k);
					int edges = MarchingCubeConstants.EDGE_TABLE[index];
					for (int l = 0; l < 12; l++) {
						if (((edges >> l) & 0b1) == 1) {
							int[] cacheOffsets = MarchingCubeConstants.VECTOR_CACHE_OFFSETS[l];
							int x = i + cacheOffsets[0];
							int y = j + cacheOffsets[1];
							int z = k + cacheOffsets[2];
							int w = cacheOffsets[3];
							int finalI = i;
							int finalJ = j;
							int finalK = k;
							int finalL = l;
							vectorIndices[l] = edgeIndices.computeIfAbsent(hashEdgeIndex(x, y, z, w), hashed -> {
								int[] o = MarchingCubeConstants.INTERPOLATE_OFFSETS[finalL];
								var vertexA = Vector3D.of(offsets[0] + strides[0] * (finalI + o[0]),
										offsets[1] + strides[1] * (finalJ + o[1]), offsets[2] + strides[2] * (finalK + o[2]));
								var vertexB = Vector3D.of(offsets[0] + strides[0] * (finalI + o[3]),
										offsets[1] + strides[1] * (finalJ + o[4]), offsets[2] + strides[2] * (finalK + o[5]));
								var aX = finalI + o[0];
								var aY = finalJ + o[1];
								var aZ = finalK + o[2];
								var bX = finalI + o[3];
								var bY = finalJ + o[4];
								var bZ = finalK + o[5];
								var dataA = grid.get(aX, aY, aZ);
								var dataB = grid.get(bX, bY, bZ);
								var weightsA = textureWeightsGrid.get(aX, aY, aZ);
								var weightsB = textureWeightsGrid.get(bX, bY, bZ);
								var edgeIndex = vertices.size();
								float percentage = (threshold - dataA) / (dataB - dataA);
								vertices.add(interpolate(vertexA, vertexB, percentage));
								textureWeights.add(interpolate(weightsA, weightsB, percentage));
								prenormalHashes.add(PreNormals.hash(x, y, z, w));
								return edgeIndex;
							});
						}
					}
					int[] triangles = MarchingCubeConstants.TRIANGLE_TABLE[index];
					for (int l = 0; l < triangles.length; l += 3) {
						indices.add(vectorIndices[triangles[l]]);
						indices.add(vectorIndices[triangles[l + 1]]);
						indices.add(vectorIndices[triangles[l + 2]]);
						triangleCoords.add(new TripleIndex(i, j, k));
					}
				}
			}
		}
		return new MarchingCubeMesh(indices.stream().mapToInt(Integer::intValue).toArray(),
				vertices.toArray(Vector3D[]::new), textureWeights.toArray(float[][]::new),
				prenormalHashes.stream().mapToInt(Integer::intValue).toArray(),
				triangleCoords.toArray(TripleIndex[]::new));
	}

	private long hashEdgeIndex(long x, long y, long z, long w) {
		// 16 bits per dimension - 2^16 = 65536
		return (x << 48) | (y << 32) | (z << 16) | w;
	}

	private Vector3D interpolate(Vector3D a, Vector3D b, float percentage) {
		return b.subtract(a).multiply(percentage).add(a);
	}

	private float[] interpolate(float[] a, float[] b, float percentage) {
		int n = a.length;
		float[] result = new float[n];
		for (int i = n; --i >= 0;) {
			result[i] = ((b[i] - a[i]) * percentage) + a[i];
		}
		return result;
	}

	private int getIndex(int i, int j, int k) {
		int index = 0;
		if (grid.get(i, j, k) <= threshold) {
			index |= 1;
		}
		if (grid.get(i + 1, j, k) <= threshold) {
			index |= 2;
		}
		if (grid.get(i + 1, j, k + 1) <= threshold) {
			index |= 4;
		}
		if (grid.get(i, j, k + 1) <= threshold) {
			index |= 8;
		}
		if (grid.get(i, j + 1, k) <= threshold) {
			index |= 16;
		}
		if (grid.get(i + 1, j + 1, k) <= threshold) {
			index |= 32;
		}
		if (grid.get(i + 1, j + 1, k + 1) <= threshold) {
			index |= 64;
		}
		if (grid.get(i, j + 1, k + 1) <= threshold) {
			index |= 128;
		}
		return index;
	}
}
