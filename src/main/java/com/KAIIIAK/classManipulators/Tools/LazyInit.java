package com.KAIIIAK.classManipulators.Tools;

import java.util.function.Predicate;

public class LazyInit<T> {
	
	private T value;
	private Predicate<T> failSetCondition;
	private String failSetMSG = "Fail set condition fired on trying to set value!";
	
	public LazyInit() {}
	
	public LazyInit(T o) {
		set(o);
	}
	
	public void setFailSetCondition(Predicate<T> failSetCondition) {
		this.failSetCondition = failSetCondition;
	}
	
	public void setFailSetMSG(String failSetMSG) {
		this.failSetMSG = failSetMSG;
	}
	
	public void set(T o) {
		if (failSetCondition != null && failSetCondition.test(o)) throw new RuntimeException(failSetMSG);
		value = o;
	}
	
	public void setIfAbsent(T value) {
		if (!initialized()) set(value);
	}
	
	public T get() {
		if (!initialized()) throw new RuntimeException("Trying to get unitialized value...");
		return getNullable();
	}
	
	public T getNullable() {
		return value;
	}
	
	public boolean initialized() {
		return value != null;
	}
}
