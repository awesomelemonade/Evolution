package lemon.evolution.destructible.beta;

import lemon.engine.math.Vector3D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		List<Integer> hashes = new ArrayList<>();
		int[][][][] edgeIndices = new int[grid.getSizeX()][grid.getSizeY()][grid.getSizeZ()][3];
		for (int[][][] a : edgeIndices) {
			for (int[][] b : a) {
				for (int[] c : b) {
					Arrays.fill(c, -1);
				}
			}
		}
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
							if (edgeIndices[x][y][z][w] == -1) {
								int[] o = MarchingCubeConstants.INTERPOLATE_OFFSETS[l];
								var vertexA = Vector3D.of(offsets[0] + strides[0] * (i + o[0]),
										offsets[1] + strides[1] * (j + o[1]), offsets[2] + strides[2] * (k + o[2]));
								var vertexB = Vector3D.of(offsets[0] + strides[0] * (i + o[3]),
										offsets[1] + strides[1] * (j + o[4]), offsets[2] + strides[2] * (k + o[5]));
								var aX = i + o[0];
								var aY = j + o[1];
								var aZ = k + o[2];
								var bX = i + o[3];
								var bY = j + o[4];
								var bZ = k + o[5];
								var dataA = grid.get(aX, aY, aZ);
								var dataB = grid.get(bX, bY, bZ);
								var weightsA = textureWeightsGrid.get(aX, aY, aZ);
								var weightsB = textureWeightsGrid.get(bX, bY, bZ);
								edgeIndices[x][y][z][w] = vertices.size();
								float percentage = (threshold - dataA) / (dataB - dataA);
								vertices.add(interpolate(vertexA, vertexB, percentage));
								textureWeights.add(interpolate(weightsA, weightsB, percentage));
								hashes.add(PreNormals.hash(x, y, z, w));
							}
							vectorIndices[l] = edgeIndices[x][y][z][w];
						}
					}
					int[] triangles = MarchingCubeConstants.TRIANGLE_TABLE[index];
					for (int l = 0; l < triangles.length; l++) {
						indices.add(vectorIndices[triangles[l]]);
					}
				}
			}
		}
		return new MarchingCubeMesh(indices.stream().mapToInt(Integer::intValue).toArray(),
				vertices.toArray(Vector3D[]::new), textureWeights.toArray(float[][]::new),
				hashes.stream().mapToInt(Integer::intValue).toArray());
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
