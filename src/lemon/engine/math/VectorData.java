package lemon.engine.math;

import lemon.engine.toolbox.Lazy;

import java.util.Arrays;

public interface VectorData {
	public Lazy<float[]> dataArray();
	public default float[] data() {
		var dataArray = constantData();
		return Arrays.copyOf(dataArray, dataArray.length);
	}
	public default float[] constantData() {
		return dataArray().get();
	}
	public default int numDimensions() {
		return constantData().length;
	}
}
