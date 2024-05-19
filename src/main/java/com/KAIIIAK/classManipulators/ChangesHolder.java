package com.KAIIIAK.classManipulators;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.*;

public class ChangesHolder {
	
	public Type clazz;
	public String methodName;
	public Type[] methodParams;
	public Type methodReturn;
	
	public Map<List<AbstractInsnNode>, List<AbstractInsnNode>> instToReplace = new HashMap<>();//from, to
	
	public ChangesHolder(Type clazz, String methodName) {
		this.clazz = clazz;
		this.methodName = methodName;
	}
}
