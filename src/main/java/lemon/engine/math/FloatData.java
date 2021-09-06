package lemon.engine.math;

import java.nio.FloatBuffer;

public interface FloatData {
	public int numDimensions();
	public void putInBuffer(FloatBuffer buffer);
	public void putInArray(float[] array);
}
