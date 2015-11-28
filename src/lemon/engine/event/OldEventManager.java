package lemon.engine.event;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lemon.engine.reflection.ReflectionUtil;

public class OldEventManager {
	private static Map<Listener, Map<Class<? extends Event>, List<Method>>> listeners;
	static{
		listeners = new HashMap<Listener, Map<Class<? extends Event>, List<Method>>>();
	}
	public static void registerListener(Listener listener){
		Map<Class<? extends Event>, List<Method>> methods = new HashMap<Class<? extends Event>, List<Method>>();
		for(Method method: listener.getClass().getMethods()){
			if(!Modifier.isStatic(method.getModifiers())){
				if(method.getAnnotation(Subscribe.class)!=null){
					if(method.getParameterTypes().length==1){
						if(Event.class.isAssignableFrom(method.getParameterTypes()[0])){
							@SuppressWarnings("unchecked") //It's actually checked!
							Class<? extends Event> parameter = (Class<? extends Event>) method.getParameterTypes()[0];
							List<Method> temp = methods.get(parameter);
							if(temp==null){
								temp = new ArrayList<Method>();
							}
							temp.add(method);
							methods.put(parameter, temp);
						}
					}
				}
			}
		}
		listeners.put(listener, methods);
	}
	public static void unregisterListener(Listener listener){
		listeners.remove(listener);
	}
	public static void callListeners(Event event){
		for(Listener listener: listeners.keySet()){
			for(Class<? extends Event> clazz: listeners.get(listener).keySet()){
				if(clazz.isAssignableFrom(event.getClass())){
					if(listeners.get(listener).get(clazz)!=null){
						for(Method method: listeners.get(listener).get(clazz)){
							ReflectionUtil.invokePrivateMethod(method, listener, event);
						}
					}
				}
			}
		}
	}
}
