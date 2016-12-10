package lemon.engine.animation;

public interface TargetedInterpolator extends Interpolator {
	public void setTarget(KeyState target);
	public KeyState getTarget();
}
