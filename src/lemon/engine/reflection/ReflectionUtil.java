package lemon.engine.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

public class ReflectionUtil {
	private ReflectionUtil(){}
	
	public static Field getPrivateField(Class<?> clazz, String fieldName){
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException | SecurityException e) {
			return null;
		}
	}
	public static Object getPrivateField(Class<?> clazz, String fieldName, Object object){
		Field field = getPrivateField(clazz, fieldName);
		try {
			field.setAccessible(true);
			return field.get(object);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return null;
		} finally {
			field.setAccessible(false);
		}
	}
	public static Field getPublicField(Class<?> clazz, String fieldName){
		try {
			return clazz.getField(fieldName);
		} catch (NoSuchFieldException | SecurityException e) {
			return null;
		}
	}
	public static Object getPublicField(Class<?> clazz, String fieldName, Object object){
		Field field = getPublicField(clazz, fieldName);
		try {
			return field.get(object);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return null;
		}
	}
	public static boolean setPublicField(Object object, String fieldName, Object value){
		Field field = ReflectionUtil.getPublicField(object.getClass(), fieldName);
		try {
			field.set(object, value);
			return true;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return false;
		}
	}
	public static boolean setPrivateField(Object object, String fieldName, Object value){
		Field field = ReflectionUtil.getPrivateField(object.getClass(), fieldName);
		field.setAccessible(true);
		try {
			field.set(object, value);
			return true;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return false;
		} finally {
			field.setAccessible(false);
		}
	}
	public static Method getPublicMethod(Class<?> clazz, String name, Class<?>... args){
		try {
			return clazz.getMethod(name, args);
		} catch (NoSuchMethodException | SecurityException e) {
			return null;
		}
	}
	public static Method getPrivateMethod(Class<?> clazz, String name, Class<?>... args){
		try {
			return clazz.getDeclaredMethod(name, args);
		} catch (NoSuchMethodException | SecurityException e) {
			return null;
		}
	}
	public static Object invokePublicMethod(Method method, Object object, Object... args){
		try {
			return method.invoke(object, args);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			return null;
		}
	}
	public static Object invokePrivateMethod(Method method, Object object, Object... args){
		try {
			method.setAccessible(true);
			return method.invoke(object, args);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			return null;
		} finally {
			method.setAccessible(false);
		}
	}
	public static <T> T getInstance(Class<T> clazz){
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			return null;
		}
	}
	public static Object getInstance(Class<?> clazz, Object... args){
		int numOfParams = 0;
		if(args!=null){
			numOfParams = args.length;
		}
		for(Constructor<?> constructor: clazz.getConstructors()){
			if(constructor.getParameterTypes().length==numOfParams){
				try {
					return constructor.newInstance(args);
				} catch (InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException e) {
					return null;
				}
			}
		}
		return null;
	}
	public static Class<?> getClass(String packageName, String className){
		try {
			return Class.forName(packageName+"."+className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	public static Class<?> getClass(String packageName, String className, URLClassLoader loader){
		try {
			return Class.forName(packageName+"."+className, true, loader);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	public static <T> T castObject(Class<T> clazz, Object object){
		return clazz.cast(object);
	}
}

