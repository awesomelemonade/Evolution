package lemon.engine.game;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerControls<K, V> {
	private Map<K, List<V>> controls;
	private Set<K> keyStates;
	public PlayerControls(){
		controls = new HashMap<K, List<V>>();
		keyStates = new HashSet<K>();
	}
	public synchronized void setKeyState(K key, boolean state){
		if(state){
			keyStates.add(key);
		}else{
			keyStates.remove(key);
		}
	}
	public synchronized void bindKey(K key, V value){
		if(!controls.containsKey(key)){
			controls.put(key, new ArrayList<V>());
		}
		controls.get(key).add(value);
	}
	public synchronized boolean getState(V value){
		for(K key: keyStates){
			if(controls.get(key)!=null){
					if(controls.get(key).contains(value)){
					return true;
				}
			}
		}
		return false;
	}
	public boolean hasStates(){
		return !keyStates.isEmpty();
	}
}