package lemon.evolution.destructible.beta;

import lemon.engine.model.AbstractColoredModel;
import lemon.engine.model.ColoredModel;
import lemon.engine.model.ModelBuilder;
import lemon.engine.math.Vector3D;
import lemon.engine.toolbox.Color;

import java.util.Arrays;

public class MarchingCube {
	private float[] offsets; // [offsetX, offsetY, offsetZ]
	private float[] strides; // [strideX, strideY, strideZ]
	private float threshold;
	private float[][][] data;
	public MarchingCube(float[][][] data, Vector3D size, float threshold) {
		this.data = data;
		this.threshold = threshold;
		this.offsets = new float[] {-size.getX() / 2f, -size.getY() / 2f, -size.getZ() / 2f};
		this.strides = new float[] {
				size.getX() / data.length,
				size.getY() / data[0].length,
				size.getZ() / data[0][0].length
		};
	}
	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}
	public float getThreshold() {
		return threshold;
	}
	public ColoredModel getColoredModel() {
		ModelBuilder<ColoredModel> builder =
				new ModelBuilder<>((vertices, indices) -> {
					Color[] colors = new Color[indices.length];
					Arrays.fill(colors, Color.BLUE);
					return new AbstractColoredModel(vertices, Color.randomOpaque(indices.length), indices);
				});
		// TODO: some way to cache edge to rendered vertex
		for (int i = 0; i < data.length - 1; i++) {
			for (int j = 0; j < data[0].length - 1; j++) {
				for (int k = 0; k < data[0][0].length - 1; k++) {
					int index = getIndex(i, j, k);
					int edges = MarchingCubeConstants.EDGE_TABLE[index];
					Vector3D[] vectors = new Vector3D[12];
					int[] vectorIndices = new int[12];
					for (int l = 0; l < 12; l++) {
						vectorIndices[l] = -1;
						if (((edges >> l) & 0b1) == 1) {
							int[] o = MarchingCubeConstants.INTERPOLATE_OFFSETS[l];
							Vector3D a = new Vector3D(offsets[0] + strides[0] * (i + o[0]),
									offsets[1] + strides[1] * (j + o[1]), offsets[2] + strides[2] * (k + o[2]));
							Vector3D b = new Vector3D(offsets[0] + strides[0] * (i + o[3]),
									offsets[1] + strides[1] * (j + o[4]), offsets[2] + strides[2] * (k + o[5]));
							float dataA = data[i + o[0]][j + o[1]][k + o[2]];
							float dataB = data[i + o[3]][j + o[4]][k + o[5]];
							vectors[l] = interpolate(a, b, (threshold - dataA) / (dataB - dataA));
							vectorIndices[l] = builder.getVertices().size();
							builder.addVertices(vectors[l]);
						}
					}
					int[] triangles = MarchingCubeConstants.TRIANGLE_TABLE[index];
					for (int l = 0; l < triangles.length; l++) {
						builder.addIndices(vectorIndices[triangles[l]]);
					}
				}
			}
		}
		return builder.build();
	}
	public Vector3D interpolate(Vector3D a, Vector3D b, float percentage) {
		return b.subtract(a).multiply(percentage).add(a);
	}
	public int getIndex(int i, int j, int k) {
		int index = 0;
		if (data[i][j][k] < threshold) {
			index |= 1;
		}
		if (data[i + 1][j][k] < threshold) {
			index |= 2;
		}
		if (data[i + 1][j][k + 1] < threshold) {
			index |= 4;
		}
		if (data[i][j][k + 1] < threshold) {
			index |= 8;
		}
		if (data[i][j + 1][k] < threshold) {
			index |= 16;
		}
		if (data[i + 1][j + 1][k] < threshold) {
			index |= 32;
		}
		if (data[i + 1][j + 1][k + 1] < threshold) {
			index |= 64;
		}
		if (data[i][j + 1][k + 1] < threshold) {
			index |= 128;
		}
		return index;
	}
}
