package com.KAIIIAK.ignorer;

public class Ign {
	public static <T> T it(RunnableExcP<T> func) {
		try {
			return func.run();
		} catch (Throwable ignor) {
			return null;
		}
	}
	
	public static void it(RunnableExc func) {
		try {
			func.run();
		} catch (Throwable ignor) {}
	}
}
