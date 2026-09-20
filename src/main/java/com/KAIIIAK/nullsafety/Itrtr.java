package com.KAIIIAK.nullsafety;

import java.util.Iterator;

public class Itrtr<E extends Iterator<T>, T> implements Iterator<T> {
	
	public E iterator;
	
	public Itrtr(E it) {
		this.iterator = it;
	}
	
	@Override
	public boolean hasNext() {
		return iterator != null && iterator.hasNext();
	}
	
	@Override
	public T next() {
		return iterator != null ? iterator.next() : null;
	}
}
