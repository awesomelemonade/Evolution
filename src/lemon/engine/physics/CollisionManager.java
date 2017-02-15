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
			queue.add(new CollisionEffect(dynamicCollidable));
		}
		while(!queue.isEmpty()){
			CollisionEffect effect = queue.poll();
			float lowestDistance = Float.MAX_VALUE;
			Collidable lowestCollidable = null;
			for(Collidable collidable: collidables){
				if(collidable.equals(effect.getCollidable())){ //You can't collide with yourself!
					continue;
				}
				float distance = collidable.getIntersection(effect);
				if(distance<lowestDistance){
					lowestDistance = distance;
					lowestCollidable = collidable;
				}
			}
			if(lowestCollidable!=null){
				lowestCollidable.applyResponse();
				if(effect.hasMotion()){
					queue.add(effect);
				}
			}
		}
	}
}
