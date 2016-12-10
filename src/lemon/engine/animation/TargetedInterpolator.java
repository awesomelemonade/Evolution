package lemon.engine.animation;

import lemon.engine.math.Vector;

public interface TargetedInterpolator extends Interpolator {
	public void setTarget(Vector target);
	public Vector getTarget();
}
