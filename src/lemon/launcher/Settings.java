package lemon.launcher;

import java.util.HashMap;
import java.util.Map;

public class Settings {
	private Map<String, String> defaults;
	private Map<String, String> values;
	public Settings(){
		defaults = new HashMap<String, String>();
		values = new HashMap<String, String>();
	}
	public void setDefaultValue(String key, String value){
		defaults.put(key, value);
	}
	public void setValue(String key, String value){
		values.put(key, value);
	}
	public String getDefaultValue(String key){
		return defaults.get(key);
	}
	public String getValue(String key){
		if(!values.containsKey(key)){
			return defaults.get(key);
		}
		return values.get(key);
	}
}
