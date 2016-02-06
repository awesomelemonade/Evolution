package lemon.engine.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Entity {
	private List<Component> components;
	public Entity(){
		components = new ArrayList<Component>();
	}
	public void addComponent(Component component){
		components.add(component);
	}
	public List<Component> getComponents(){
		return Collections.unmodifiableList(components);
	}
}
