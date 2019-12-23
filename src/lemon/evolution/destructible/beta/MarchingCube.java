package lemon.evolution.destructible.beta;

import lemon.engine.control.Loader;
import lemon.engine.entity.ModelBuilder;
import lemon.engine.entity.RenderableColoredModel;
import lemon.engine.math.Percentage;
import lemon.engine.math.Vector3D;
import lemon.engine.thread.ThreadManager;
import lemon.engine.toolbox.Color;

public class MarchingCube {
	private float[] offsets; // [offsetX, offsetY, offsetZ]
	private float[] strides; // [strideX, strideY, strideZ]
	private float resolution;
	private float threshold;
	private float[][][] data;
	private ScalarField<Vector3D> scalarField;
	public MarchingCube(ScalarField<Vector3D> scalarField, Vector3D size, float resolution, float threshold) {
		int ceilX = (int) Math.ceil(size.getX() / resolution);
		int ceilY = (int) Math.ceil(size.getY() / resolution);
		int ceilZ = (int) Math.ceil(size.getZ() / resolution);
		if (ceilX < 0 || ceilY < 0 || ceilZ < 0) {
			throw new IllegalArgumentException("Calculated size cannot be less than 0");
		}
		this.scalarField = scalarField;
		this.data = new float[ceilX][ceilY][ceilZ];
		this.resolution = resolution;
		this.threshold = threshold;
		this.offsets = new float[] {
				-(ceilX - 1f) / 2f * resolution,
				-(ceilY - 1f) / 2f * resolution,
				-(ceilZ - 1f) / 2f * resolution
		};
		this.strides = new float[] {resolution, resolution, resolution};
	}
	public Loader getLoader() {
		float[][][] data = this.data;
		return new Loader() {
			Percentage percentage = new Percentage(data.length * data[0].length * data[0][0].length);
			@Override
			public void load() {
				ThreadManager.INSTANCE.addThread(new Thread(() -> {
					for (int i = 0; i < data.length; i++) {
						for (int j = 0; j < data[0].length; j++) {
							for (int k = 0; k < data[0][0].length; k++) {
								data[i][j][k] = scalarField.get(
										new Vector3D(offsets[0] + strides[0] * i,
												offsets[1] + strides[1] * j, offsets[2] + strides[2] * k));
								percentage.setPart(percentage.getPart() + 1);
							}
						}
					}
				})).start();
			}
			@Override
			public Percentage getPercentage() {
				return percentage;
			}
		};
	}
	public RenderableColoredModel getColoredModel() {
		ModelBuilder<RenderableColoredModel> builder =
				new ModelBuilder<>(RenderableColoredModel::new);
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
