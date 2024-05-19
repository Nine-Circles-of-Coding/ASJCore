package com.KAIIIAK.ignorer;

@FunctionalInterface
public interface RunnableExcP<T> {
	T run() throws Throwable;
}
