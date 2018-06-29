package lemon.engine.physics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

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
		Collidable a;
		Collidable b;
		int counterA;
		int counterB;
		float progression;
		PotentialCollision(Collidable a, Collidable b, int counterA, int counterB, float progression){
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
	
	public float getCollisionProgression(Collidable a, Collidable b) {
		//TODO
	}
	
	public void update() {
		
		PriorityQueue<PotentialCollision> queue; // queues collisions it calculates
		//each collision has:
			//object a, object b, object a's counter, object b's counter, progression (priority = progression)
		//each object has:
			//collision counter
		
		Map<DynamicCollidable, Integer> counters = new HashMap<DynamicCollidable, Integer>(); //Tracks DynamicCollidables
		Map<DynamicCollidable, Float> progressions = new HashMap<DynamicCollidable, Float>(); //Tracks progression of Collidables
		
		for (int i = 0; i < dynamicCollidables.size(); ++i) {
			DynamicCollidable a = dynamicCollidables.get(i);
			for (int j = i + 1; j < dynamicCollidables.size(); ++j) {
				DynamicCollidable b = dynamicCollidables.get(j);
				queue.add(new PotentialCollision(a, b, counters.getOrDefault(a, 0), counters.getOrDefault(b, 0), getCollisionProgression(a, b)));
			}
			for (Collidable b: staticCollidables) {
				queue.add(new PotentialCollision(a, b, counters.getOrDefault(a, 0), -1, getCollisionProgression(a, b)));
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
			// Resolve collision
			
			// Increase counters of a and b if they are DynamicCollidable
			// (and) Recalculate Potential Collisions
			if (collision.getA() instanceof DynamicCollidable) {
				int newCounter = counters.getOrDefault(collision.getA(), 0) + 1;
				counters.put((DynamicCollidable)collision.getA(), newCounter);
				for (DynamicCollidable collidable: dynamicCollidables) {
					queue.add(new PotentialCollision(collision.getA(), collidable,
							newCounter, counters.getOrDefault(collidable, 0),
							getCollisionProgression(collision.getA(), collidable)));
				}
				for (Collidable collidable: staticCollidables) {
					queue.add(new PotentialCollision(collision.getA(), collidable,
							newCounter, -1, getCollisionProgression(collision.getA(), collidable)));
				}
			}
			if (collision.getB() instanceof DynamicCollidable) {
				int newCounter = counters.getOrDefault(collision.getB(), 0) + 1;
				counters.put((DynamicCollidable)collision.getB(), newCounter);
				for (DynamicCollidable collidable: dynamicCollidables) {
					queue.add(new PotentialCollision(collision.getB(), collidable,
							newCounter, counters.getOrDefault(collidable, 0),
							getCollisionProgression(collision.getB(), collidable)));
				}
				for (Collidable collidable: staticCollidables) {
					queue.add(new PotentialCollision(collision.getB(), collidable,
							newCounter, -1, getCollisionProgression(collision.getB(), collidable)));
				}
			}
		}
		// Finish unfinished progressions
		for (DynamicCollidable collidable: dynamicCollidables) {
			collidable.getPosition().selfAdd(collidable.getVelocity().multiply(1f - progressions.getOrDefault(collidable, 0f)));
		}
		//Legacy
		Queue<CollisionEffect> queue = new ArrayDeque<CollisionEffect>();
		for (DynamicCollidable dynamicCollidable : dynamicCollidables) {
			queue.add(new CollisionEffect(dynamicCollidable));
		}
		while (!queue.isEmpty()) {
			CollisionEffect effect = queue.poll();
			float lowestProgression = Float.MAX_VALUE;
			Collidable lowestCollidable = null;
			for (Collidable collidable : collidables) {
				if (collidable.equals(effect.getCollidable())) { // You can't collide with yourself!
					continue;
				}
				float progression = collidable.getIntersection(effect);
				if (progression < lowestProgression) {
					lowestProgression = progression;
					lowestCollidable = collidable;
				}
			}
			if (lowestCollidable == null || lowestProgression >= effect.getRemainingProgression()) { // No Collision Calculated; epsilon?
				// move position with frame's velocity
				effect.getCollidable().getPosition().selfAdd(effect.getCollidable().getVelocity().multiply(effect.getRemainingProgression()));
			} else {
				// move effect's collidable to the collision point
				effect.getCollidable().getPosition().selfAdd(effect.getCollidable().getVelocity().multiply(lowestProgression));
				effect.addProgression(lowestProgression);
				// apply response - changing the overall velocity of both the collidable and the dynamicCollidable
				
				// queue back in
				if (effect.getRemainingProgression() > 0) {
					
				}
				//
			}

			/*
			 * if(lowestCollidable!=null){ lowestCollidable.applyResponse();
			 * if(effect.hasMotion()){ queue.add(effect); } }
			 */
		}
	}
}
