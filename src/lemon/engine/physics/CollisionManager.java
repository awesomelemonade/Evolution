package lemon.engine.physics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class CollisionManager {
	private List<DynamicCollidable> dynamicCollidables;
	private List<Collidable> collidables;
	public CollisionManager(){
		dynamicCollidables = new ArrayList<DynamicCollidable>();
		collidables = new ArrayList<Collidable>();
	}
	public void addCollidable(Collidable collidable){
		if(collidable instanceof DynamicCollidable){
			dynamicCollidables.add((DynamicCollidable)collidable);
		}
		collidables.add(collidable);
	}
	public void update(){
		Queue<CollisionEffect> queue = new ArrayDeque<CollisionEffect>();
		for(DynamicCollidable dynamicCollidable: dynamicCollidables){
			queue.add(new CollisionEffect(dynamicCollidable.getPosition(), dynamicCollidable.getVelocity(), dynamicCollidable.getVelocity()));
		}
		while(!queue.isEmpty()){
			CollisionEffect effect = queue.poll();
			float lowestDistance = Float.MAX_VALUE;
			Collidable lowestCollidable = null;
			for(Collidable collidable: collidables){
				float distance = collidable.getIntersection(effect);
				if(distance<lowestDistance){
					lowestDistance = distance;
					lowestCollidable = collidable;
				}
			}
			if(lowestCollidable!=null){
				CollisionEffect newEffect = lowestCollidable.applyResponse();
				if(newEffect.hasMotion()){
					queue.add(newEffect);
				}
			}
		}
	}
}
