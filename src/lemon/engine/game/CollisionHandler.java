package lemon.engine.game;

import java.util.ArrayList;
import java.util.List;

import lemon.engine.control.UpdateEvent;

public class CollisionHandler {
	private List<Collidable> collidables;
	public CollisionHandler(){
		collidables = new ArrayList<Collidable>();
	}
	public void addCollidable(Collidable collidable){
		collidables.add(collidable);
	}
	public void update(UpdateEvent event){
		for(int i=0;i<collidables.size();++i){
			for(int j=i+1;j<collidables.size();++j){
				collidables.get(i).getVelocity().set(collidables.get(j).collide(collidables.get(i), event));
				collidables.get(j).getVelocity().set(collidables.get(i).collide(collidables.get(j), event));
			}
		}
	}
}
