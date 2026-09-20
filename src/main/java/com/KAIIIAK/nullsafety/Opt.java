package com.KAIIIAK.nullsafety;

import java.util.*;
import java.util.function.Consumer;

public class Opt {
	
	public static <E extends Iterable<T>, T> Itrbl<E, T> it(E it) {
		return new Itrbl<>(it);
	}
	
	public static <T> Itrbl<List<T>, T> it(T[] it) {
		if (it == null)
			return new Itrbl<>(null);
		return it(Arrays.asList(it));
	}
	
	public static <E extends Iterator<T>, T> Itrtr<E, T> it(E it) {
		return new Itrtr<>(it);
	}
	
	public static <T> void it(T obj, Consumer<? super T> consumer) {
		if (obj != null) consumer.accept(obj);
	}
}
