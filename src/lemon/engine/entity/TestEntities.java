package lemon.engine.entity;

import lemon.engine.control.UpdateEvent;
import lemon.engine.event.EventManager;
import lemon.engine.event.Listener;
import lemon.engine.event.Subscribe;
import lemon.engine.render.VertexArray;

public enum TestEntities implements EntityType, Listener {
	TEST{
		@Override
		public void init(){
			EventManager.INSTANCE.registerListener(this);
		}
		@Override
		public void render() {
			
		}
		//Some Random Event for Demonstration Purposes
		@Subscribe
		public void update(UpdateEvent event){
			
		}
	};
	private VertexArray vertexArray;
	
	@Override
	public void init() {
		
	}
	@Override
	public void render() {
		
	}
	@Override
	public VertexArray getVertexArray() {
		return vertexArray;
	}
}
