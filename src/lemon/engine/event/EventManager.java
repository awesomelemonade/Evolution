package lemon.engine.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import lemon.engine.event.Event;
import lemon.engine.reflection.ReflectionUtil;

public enum EventManager {
	INSTANCE;
	private static final Logger logger = Logger.getLogger(EventManager.class.getName());
	private Map<Class<? extends Event>, List<ListenerMethod>> methods;
	private Map<Class<? extends Event>, List<ListenerMethod>> preloaded;
	private Set<Listener> listeners;

	private EventManager() {
		methods = new HashMap<Class<? extends Event>, List<ListenerMethod>>();
		preloaded = new HashMap<Class<? extends Event>, List<ListenerMethod>>();
		listeners = new HashSet<Listener>();
	}
	public void registerListener(Listener listener) {
		internalRegisterListener(listener);
	}
	public void unregisterListener(Listener listener) {
		internalUnregisterListener(listener);
	}
	public void preload(Class<? extends Event> clazz) {
		preloaded.put(clazz, new CopyOnWriteArrayList<ListenerMethod>());
		for (Class<? extends Event> c : methods.keySet()) {
			if (c.isAssignableFrom(clazz)) {
				for (ListenerMethod method : methods.get(c)) {
					preloaded.get(clazz).add(method);
				}
			}
		}
	}
	public void callListeners(Event event) {
		if (!preloaded.containsKey(event.getClass())) {
			preload(event.getClass());
		}
		for (ListenerMethod lm : preloaded.get(event.getClass())) {
			try {
				ReflectionUtil.invokePrivateMethod(lm.getMethod(), lm.getListener(), event);
			} catch (IllegalAccessException | IllegalArgumentException ex) {
				logger.log(Level.SEVERE, ex.getMessage(), ex);
			} catch (InvocationTargetException ex) {
				logger.log(Level.WARNING, ex.getCause().getMessage(), ex.getCause());
			}
		}
	}
	@SuppressWarnings("unchecked") // It's actually checked!
	private void internalRegisterListener(Listener listener) {
		if (listeners.contains(listener)) {
			throw new IllegalArgumentException(
					String.format("Cannot Register %s:%s twice", listener.getClass().toString(), listener.toString()));
		}
		boolean registered = false;
		for (Method method : listener.getClass().getMethods()) {
			if (!Modifier.isStatic(method.getModifiers())) {
				if (method.getAnnotation(Subscribe.class) != null) {
					if (method.getParameterTypes().length == 1) {
						if (Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
							registered = true;
							Class<?> parameter = method.getParameterTypes()[0];
							if (methods.get(parameter) == null) {
								methods.put((Class<? extends Event>) parameter, new ArrayList<ListenerMethod>());
							}
							ListenerMethod lm = new ListenerMethod(listener, method);
							methods.get(parameter).add(lm);
							for (Class<? extends Event> clazz : preloaded.keySet()) {
								if (parameter.isAssignableFrom(clazz)) {
									preloaded.get(clazz).add(lm);
								}
							}
						}
					}
				}
			}
		}
		if (registered) {
			listeners.add(listener);
		} else {
			logger.log(Level.WARNING,
					String.format("Registered listener with no valid methods: %s", listener.getClass().getName()));
		}
		EventManager.INSTANCE.callListeners(new LemonRegisterListenerEvent(listener));
	}
	private void internalUnregisterListener(Listener listener) {
		if (!listeners.contains(listener)) {
			return;
		}
		List<ListenerMethod> unregister = new ArrayList<ListenerMethod>();
		for (List<ListenerMethod> methods : this.methods.values()) {
			for (ListenerMethod method : methods) {
				if (method.getListener().equals(listener)) {
					unregister.add(method);
				}
			}
		}
		for (ListenerMethod method : unregister) {
			for (List<ListenerMethod> methods : this.methods.values()) {
				if (methods.contains(method)) {
					methods.remove(method);
				}
			}
			for (List<ListenerMethod> methods : this.preloaded.values()) {
				if (methods.contains(method)) {
					methods.remove(method);
				}
			}
		}
		listeners.remove(listener);
		EventManager.INSTANCE.callListeners(new LemonUnregisterListenerEvent(listener));
	}

	private class ListenerMethod {
		private final Listener listener;
		private final Method method;

		public ListenerMethod(Listener listener, Method method) {
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
