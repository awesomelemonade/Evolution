package lemon.engine.physics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import lemon.engine.math.MathUtil;

public class CollisionManager {
	private List<DynamicCollidable> dynamicCollidables;
	private List<Collidable> staticCollidables;

	public CollisionManager() {
		dynamicCollidables = new ArrayList<DynamicCollidable>();
		staticCollidables = new ArrayList<Collidable>();
	}
	public void addCollidable(Collidable collidable) {
		if (collidable instanceof DynamicCollidable) {
			dynamicCollidables.add((DynamicCollidable) collidable);
		} else {
			staticCollidables.add(collidable);
		}
	}
	class PotentialCollision implements Comparable<PotentialCollision> {
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
	
	public float getCollisionProgression(Collidable a, Collidable b, float progressionA, float progressionB) {
		// TODO
		return -1f;
	}
	
	public void handlePotentialCollision(PriorityQueue<PotentialCollision> queue, Map<DynamicCollidable, Integer> counters, Map<DynamicCollidable, Float> progressions, Collidable a, Collidable b) {
		float progression = getCollisionProgression(a, b, progressions.getOrDefault(a, 0f), progressions.getOrDefault(b, 0f));
		if (MathUtil.inRange(progression, 0f, 1f)) {
			queue.add(new PotentialCollision(a, b, counters.getOrDefault(a, 0),
					(b instanceof DynamicCollidable) ? counters.getOrDefault(b, 0) : -1, progression));
		}
	}
	
	public void resolveCollision(Collidable a, Collidable b) {
		// TODO
	}
	
	public void update() {
		// queues collisions it calculates
		PriorityQueue<PotentialCollision> queue = new PriorityQueue<PotentialCollision>();
		//each collision has:
			//object a, object b, object a's counter, object b's counter, progression (priority = progression)
		//each object has:
			//collision counter
		
		Map<DynamicCollidable, Integer> counters = new HashMap<DynamicCollidable, Integer>(); //Tracks DynamicCollidables
		Map<DynamicCollidable, Float> progressions = new HashMap<DynamicCollidable, Float>(); //Tracks progression of Collidables
		
		for (int i = 0; i < dynamicCollidables.size(); ++i) {
			DynamicCollidable a = dynamicCollidables.get(i);
			for (int j = i + 1; j < dynamicCollidables.size(); ++j) {
				handlePotentialCollision(queue, counters, progressions, a, dynamicCollidables.get(j));
			}
			for (Collidable b: staticCollidables) {
				handlePotentialCollision(queue, counters, progressions, a, b);
			}
		}
		
		while (!queue.isEmpty()) {
			PotentialCollision collision = queue.poll();
			// Verify collision is still legit (only applies to DynamicCollidable)
			if (collision.getA() instanceof DynamicCollidable &&
					(collision.getCounterA() != counters.get(collision.getA()))) {
				continue;
			}
			if (collision.getB() instanceof DynamicCollidable &&
					(collision.getCounterB() != counters.get(collision.getB()))) {
				continue;
			}
			// Move to collision's progression
			if (collision.getA() instanceof DynamicCollidable) {
				((DynamicCollidable)collision.getA()).getPosition().selfAdd(
						((DynamicCollidable)collision.getA()).getVelocity()
						.multiply(collision.getProgression() - progressions.getOrDefault(collision.getA(), 0f)));
			}
			if (collision.getB() instanceof DynamicCollidable) {
				((DynamicCollidable)collision.getB()).getPosition().selfAdd(
						((DynamicCollidable)collision.getB()).getVelocity()
						.multiply(collision.getProgression() - progressions.getOrDefault(collision.getB(), 0f)));
			}
			//Resolve Collision
			resolveCollision(collision.getA(), collision.getB());
			// Increase counters of a and b if they are DynamicCollidable
			// (and) Recalculate Potential Collisions
			if (collision.getA() instanceof DynamicCollidable) {
				counters.put((DynamicCollidable)collision.getA(), counters.getOrDefault(collision.getA(), 0) + 1);
				for (DynamicCollidable collidable: dynamicCollidables) {
					if (!collidable.equals(collision.getB())) { // Potential Collision between A and B (if they're both dynamic) is handled later
						handlePotentialCollision(queue, counters, progressions, collision.getA(), collidable);
					}
				}
				for (Collidable collidable: staticCollidables) {
					handlePotentialCollision(queue, counters, progressions, collision.getA(), collidable);
				}
			}
			if (collision.getB() instanceof DynamicCollidable) {
				counters.put((DynamicCollidable)collision.getB(), counters.getOrDefault(collision.getB(), 0) + 1);
				for (DynamicCollidable collidable: dynamicCollidables) {
					handlePotentialCollision(queue, counters, progressions, collision.getB(), collidable);
				}
				for (Collidable collidable: staticCollidables) {
					handlePotentialCollision(queue, counters, progressions, collision.getB(), collidable);
				}
			}
		}
		// Finish unfinished progressions
		for (DynamicCollidable collidable: dynamicCollidables) {
			collidable.getPosition().selfAdd(collidable.getVelocity().multiply(1f - progressions.getOrDefault(collidable, 0f)));
		}
	}
}
