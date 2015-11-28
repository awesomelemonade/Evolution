package lemon.engine.event;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lemon.engine.event.Event;
import lemon.engine.reflection.ReflectionUtil;

public enum EventManager {
	INSTANCE;
	private Map<Class<? extends Event>, List<ListenerMethod>> methods;
	private Map<Class<? extends Event>, List<ListenerMethod>> preloaded;
	private EventManager(){
		methods = new HashMap<Class<? extends Event>, List<ListenerMethod>>();
		preloaded = new HashMap<Class<? extends Event>, List<ListenerMethod>>();
	}
	@SuppressWarnings("unchecked") //It's actually checked!
	public void registerListener(Listener listener){
		for(Method method: listener.getClass().getMethods()){
			if(!Modifier.isStatic(method.getModifiers())){
				if(method.getAnnotation(Subscribe.class)!=null){
					if(method.getParameterTypes().length==1){
						if(Event.class.isAssignableFrom(method.getParameterTypes()[0])){
							Class<?> parameter = method.getParameterTypes()[0];
							if(methods.get(parameter)==null){
								methods.put((Class<? extends Event>) parameter, new ArrayList<ListenerMethod>());
							}
							ListenerMethod lm = new ListenerMethod(listener, method);
							methods.get(parameter).add(lm);
							for(Class<? extends Event> clazz: preloaded.keySet()){
								if(clazz.isAssignableFrom(parameter)){
									 preloaded.get(clazz).add(lm);
								}
							}
						}
					}
				}
			}
		}
	}
	public void preload(Class<? extends Event> clazz){
		preloaded.put(clazz, new ArrayList<ListenerMethod>());
		for(Class<? extends Event> c: methods.keySet()){
			if(c.isAssignableFrom(clazz)){
				for(ListenerMethod method: methods.get(c)){
					preloaded.get(clazz).add(method);
				}
			}
		}
	}
	public void callListeners(Event event){
		//System.out.println("Calling Event Type: "+event.getClass().getCanonicalName());
		if(!preloaded.containsKey(event.getClass())){
			preload(event.getClass());
		}
		for(ListenerMethod lm: preloaded.get(event.getClass())){
			ReflectionUtil.invokePrivateMethod(lm.getMethod(), lm.getListener(), event);
		}
	}
	private class ListenerMethod{
		private final Listener listener;
		private final Method method;
		public ListenerMethod(Listener listener, Method method){
			this.listener = listener;
			this.method = method;
		}
		public Listener getListener() {
			return listener;
		}
		public Method getMethod() {
			return method;
		}
	}
}
