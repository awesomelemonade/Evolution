package lemon.engine.physics;


public class CollisionEffect {
	private DynamicCollidable collidable;
	private float progression;

	public CollisionEffect(DynamicCollidable collidable) {
		this.collidable = collidable;
	}
	public DynamicCollidable getCollidable() {
		return collidable;
	}
	public void addProgression(float progression) {
		this.progression += progression;
	}
	public float getProgression() {
		return progression;
	}
	public float getRemainingProgression() {
		return 1.0f - progression;
	}
}
