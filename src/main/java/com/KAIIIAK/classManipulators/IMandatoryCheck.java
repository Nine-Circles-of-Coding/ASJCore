package com.KAIIIAK.classManipulators;

public interface IMandatoryCheck {
	
	CheckState check();
	
	default boolean isGroup() {
		return false;
	}
	
	enum CheckState {
		APPLIED,
		WAITING,
		FAILED
	}
}
