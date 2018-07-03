package lemon.engine.physics;

public class PotentialCollision implements Comparable<PotentialCollision> {
	private Collidable a;
	private Collidable b;
	private int counterA;
	private int counterB;
	private float progression;
	public PotentialCollision(Collidable a, Collidable b, int counterA, int counterB, float progression){
		this.a = a;
		this.b = b;
		this.counterA = counterA;
		this.counterB = counterB;
		this.progression = progression;
	}
	public Collidable getA() {
		return a;
	}
	public Collidable getB() {
		return b;
	}
	public int getCounterA() {
		return counterA;
	}
	public int getCounterB() {
		return counterB;
	}
	public float getProgression() {
		return progression;
	}
	@Override
	public int compareTo(PotentialCollision collision) {
		return Float.compare(progression, collision.progression);
	}
}