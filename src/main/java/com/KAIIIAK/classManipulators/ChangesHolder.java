package com.KAIIIAK.classManipulators;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.*;

import static com.KAIIIAK.classManipulators.SomeUtil.copyListInstrs;

public class ChangesHolder {
	
	//annotation data start
	public int[] matchIndex;
	public boolean isMandatory;
	public boolean ignoreLines;
	public boolean ignoreLabels;
	public int priority;
	//annotation data end
	
	public List<AbstractInsnNode> from;
	public List<AbstractInsnNode> to;
	
	public String containerClass;
	public String containerMethod;
	
	public Type targetClass;
	public String targetMethodName;
	public Type[] targetMethodParams;
	public Type targetMethodReturn;
	
	public ChangesHolder(Type targetClass, String methodName) {
		this.targetClass = targetClass;
		this.targetMethodName = methodName;
	}
	
	public ChangesHolder copy() {
		ChangesHolder ret = new ChangesHolder(this.targetClass, this.targetMethodName);
		ret.matchIndex = this.matchIndex == null ? null : this.matchIndex.clone();
		ret.isMandatory = this.isMandatory;
		ret.ignoreLines = this.ignoreLines;
		ret.ignoreLabels = this.ignoreLabels;
		ret.priority = this.priority;
		ret.from = copyListInstrs(this.from);// after copy it never == null
		ret.to = copyListInstrs(this.to);// after copy it never == null
		ret.containerClass = this.containerClass;
		ret.containerMethod = this.containerMethod;
		ret.targetMethodParams = this.targetMethodParams == null ? null : this.targetMethodParams.clone();
		ret.targetMethodReturn = this.targetMethodReturn;
		return ret;
	}
	
	@Override
	public String toString() {
		return "HookReplacer: " +
				targetClass.getClassName() + '#' + targetMethodName +
				Type.getMethodDescriptor(targetMethodReturn, targetMethodParams) +
				" -> " +
				containerClass + '#' + containerMethod +
				", matchIndex = " + Arrays.toString(matchIndex) +
				", priority = " + priority;
	}
}