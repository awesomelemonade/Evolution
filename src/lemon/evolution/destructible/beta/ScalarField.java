package lemon.evolution.destructible.beta;

import lemon.engine.control.Loader;
import lemon.engine.math.Percentage;
import lemon.engine.math.Vector;
import lemon.engine.math.Vector3D;
import lemon.engine.thread.ThreadManager;

public interface ScalarField<T extends Vector> {
	public float get(T vector);
	public static Loader getLoader(ScalarField<Vector3D> scalarField, Vector3D offset, Vector3D resolution, float[][][] data) {
		float[] offsets = new float[] {
				-(data.length - 1f) / 2f * resolution.getX() + offset.getX(),
				-(data[0].length - 1f) / 2f * resolution.getY() + offset.getY(),
				-(data[1].length - 1f) / 2f * resolution.getZ() + offset.getZ()
		};
		float[] strides = new float[] {resolution.getX(), resolution.getY(), resolution.getZ()};
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
