package lemon.engine.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

public class ReflectionUtil {
	private ReflectionUtil(){}
	
	public static Field getPrivateField(Class<?> clazz, String fieldName) throws NoSuchFieldException, SecurityException{
		return clazz.getDeclaredField(fieldName);
	}
	public static Object getPrivateField(Class<?> clazz, String fieldName, Object object) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		Field field = getPrivateField(clazz, fieldName);
		try {
			field.setAccessible(true);
			return field.get(object);
		} finally {
			field.setAccessible(false);
		}
	}
	public static Field getPublicField(Class<?> clazz, String fieldName) throws NoSuchFieldException, SecurityException{
		return clazz.getField(fieldName);
	}
	public static Object getPublicField(Class<?> clazz, String fieldName, Object object) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		Field field = getPublicField(clazz, fieldName);
		return field.get(object);
	}
	public static void setPublicField(Object object, String fieldName, Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		ReflectionUtil.getPublicField(object.getClass(), fieldName).set(object, value);
	}
	public static boolean setPrivateField(Object object, String fieldName, Object value) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		Field field = ReflectionUtil.getPrivateField(object.getClass(), fieldName);
		field.setAccessible(true);
		try {
			field.set(object, value);
			return true;
		}finally {
			field.setAccessible(false);
		}
	}
	public static Method getPublicMethod(Class<?> clazz, String name, Class<?>... args) throws NoSuchMethodException, SecurityException{
		return clazz.getMethod(name, args);
	}
	public static Method getPrivateMethod(Class<?> clazz, String name, Class<?>... args) throws NoSuchMethodException, SecurityException{
		return clazz.getDeclaredMethod(name, args);
	}
	public static Object invokePublicMethod(Method method, Object object, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		return method.invoke(object, args);
	}
	public static Object invokePrivateMethod(Method method, Object object, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		try {
			method.setAccessible(true);
			return method.invoke(object, args);
		} finally {
			method.setAccessible(false);
		}
	}
	public static <T> T getInstance(Class<T> clazz) throws InstantiationException, IllegalAccessException{
		return clazz.newInstance();
	}
	public static Object getInstance(Class<?> clazz, Object... args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		int numOfParams = 0;
		if(args!=null){
			numOfParams = args.length;
		}
		for(Constructor<?> constructor: clazz.getConstructors()){
			if(constructor.getParameterTypes().length==numOfParams){
				return constructor.newInstance(args);
			}
		}
		return null;
	}
	public static Class<?> getClass(String packageName, String className) throws ClassNotFoundException{
		return Class.forName(packageName+"."+className);
	}
	public static Class<?> getClass(String packageName, String className, URLClassLoader loader) throws ClassNotFoundException{
		return Class.forName(packageName+"."+className, true, loader);
	}
	public static <T> T castObject(Class<T> clazz, Object object){
		return clazz.cast(object);
	}
}

