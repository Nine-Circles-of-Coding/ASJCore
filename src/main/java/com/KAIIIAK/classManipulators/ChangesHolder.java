package com.KAIIIAK.classManipulators;

import com.KAIIIAK.KASMLib.util.KASMUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.*;

import static com.KAIIIAK.classManipulators.SomeUtil.copyListInstrs;

public class ChangesHolder {
	
	//annotation data start
	public int[] onlyNthMatches;
	//public boolean isMandatory;
	public boolean ignoreLines;
	public boolean ignoreLabels;
	public int priority;
	//annotation data end
	
	public Successor successor;// not being copied by copy()
	
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
		successor = new Successor(this);
	}
	
	public ChangesHolder(Type targetClass, String methodName, Successor succesor) // for copy only
	{
		this.targetClass = targetClass;
		this.targetMethodName = methodName;
		this.successor = succesor;
	}
	
	public ChangesHolder copy() {
		ChangesHolder ret = new ChangesHolder(this.targetClass, this.targetMethodName, this.successor);
		ret.onlyNthMatches = this.onlyNthMatches == null ? null : this.onlyNthMatches.clone();
		//ret.isMandatory = this.isMandatory;
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
		return "HookReplacer: " + targetClass.getClassName() + '#' + targetMethodName + Type.getMethodDescriptor(targetMethodReturn, targetMethodParams) + " -> " + containerClass + '#' + containerMethod + ", onlyNthMatches = " + Arrays.toString(onlyNthMatches) + ", priority = " + priority;
	}
	
	public static class Successor implements IMandatoryCheck { // name is Successor via ending "er" is not a mistake
		
		public CheckState state = CheckState.WAITING;
		public ChangesHolder relatedChH;
		
		public Successor(ChangesHolder relatedChH) {
			this.relatedChH = relatedChH;
		}
		
		@Override
		public CheckState check() {
			if (KASMUtil.findLoadedClass(relatedChH.targetClass.getClassName()) != null && state != CheckState.APPLIED)
				state = CheckState.FAILED;
			return state;
		}
	}
	
}