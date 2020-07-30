package lemon.evolution.destructible.beta;

import lemon.engine.model.ModelBuilder;
import lemon.engine.math.Vector3D;
import lemon.evolution.pool.VectorPool;

import java.util.Arrays;

public class MarchingCube {
	private float[] offsets; // [offsetX, offsetY, offsetZ]
	private float[] strides; // [strideX, strideY, strideZ]
	private float threshold;
	private BoundedScalarGrid3D grid;
	public MarchingCube(BoundedScalarGrid3D grid, Vector3D size, float threshold) {
		this.grid = grid;
		this.threshold = threshold;
		this.offsets = new float[] {0f, 0f, 0f};
		this.strides = new float[] {
				size.getX() / grid.getSizeX(),
				size.getY() / grid.getSizeY(),
				size.getZ() / grid.getSizeZ()
		};
	}
	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}
	public float getThreshold() {
		return threshold;
	}
	public void addVertices(ModelBuilder builder) {
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
								try (var a = VectorPool.of(offsets[0] + strides[0] * (i + o[0]),
										offsets[1] + strides[1] * (j + o[1]), offsets[2] + strides[2] * (k + o[2]))) {
									Vector3D b = new Vector3D(offsets[0] + strides[0] * (i + o[3]),
											offsets[1] + strides[1] * (j + o[4]), offsets[2] + strides[2] * (k + o[5]));
									float dataA = grid.get(i + o[0], j + o[1], k + o[2]);
									float dataB = grid.get(i + o[3], j + o[4], k + o[5]);
									edgeIndices[x][y][z][w] = builder.getVertices().size();
									builder.addVertices(interpolate(a, b, (threshold - dataA) / (dataB - dataA)));
								}
							}
							vectorIndices[l] = edgeIndices[x][y][z][w];
						}
					}
					int[] triangles = MarchingCubeConstants.TRIANGLE_TABLE[index];
					for (int l = 0; l < triangles.length; l++) {
						builder.addIndices(vectorIndices[triangles[l]]);
					}
				}
			}
		}
	}
	private Vector3D interpolate(Vector3D a, Vector3D b, float percentage) {
		return b.subtract(a).multiply(percentage).add(a);
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
