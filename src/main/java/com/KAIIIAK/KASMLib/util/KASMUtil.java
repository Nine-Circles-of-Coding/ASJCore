package com.KAIIIAK.KASMLib.util;

public class KASMUtil {
	
	public static Class<?> getClass(int i) {
		try {
			return Class.forName(Thread.currentThread().getStackTrace()[i].getClassName());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T inst() {
		try {
			return (T) getClass(3).newInstance();
		} catch (IllegalAccessException | InstantiationException | ClassCastException e) {
			throw new RuntimeException(e);
		}
	}
}
