package lemon.evolution.destructible.beta;

import lemon.engine.control.Loader;
import lemon.engine.math.Percentage;
import lemon.engine.math.Vector3D;
import lemon.engine.math.VectorData;
import lemon.engine.thread.ThreadManager;

public interface ScalarField<T extends VectorData> {
	public float get(T vector);
	public static Loader getLoader(ScalarField<Vector3D> scalarField, Vector3D offset, Vector3D resolution, float[][][] data) {
		float[] offsets = new float[] {
				-(data.length - 1f) / 2f * resolution.x() + offset.x(),
				-(data[0].length - 1f) / 2f * resolution.y() + offset.y(),
				-(data[1].length - 1f) / 2f * resolution.z() + offset.z()
		};
		float[] strides = new float[] {resolution.x(), resolution.y(), resolution.z()};
		Percentage percentage = new Percentage(data.length * data[0].length * data[0][0].length);
		return new Loader() {
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
}
