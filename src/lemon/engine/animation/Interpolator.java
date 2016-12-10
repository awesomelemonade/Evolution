package lemon.engine.animation;

import lemon.engine.math.Vector;

public interface Interpolator {
	public void setVector(Vector vector);
	public Vector getVector();
	public void update(long time);
}
