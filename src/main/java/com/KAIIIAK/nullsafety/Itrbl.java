package com.KAIIIAK.nullsafety;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Consumer;

public class Itrbl<E extends Iterable<T>, T> implements Iterable<T> {
	
	public E iterable;
	
	public Itrbl(E it) {
		this.iterable = it;
	}
	
	@NotNull
	public Iterator<T> iterator() {
		if (iterable == null) return Opt.it((Iterator<T>) null);
		return Opt.it(iterable.iterator());
	}
	
	public void forEach(Consumer<? super T> action) {
		if (action == null || iterable == null) return;
		for (T t : iterable) {
			action.accept(t);
		}
	}
}

