package com.KAIIIAK.superwrapper;

import org.objectweb.asm.Type;

import java.util.List;

public class SuperWrapperTransformerContainer {
	
	public String containerClassName;
	public String targetClass;
	public String targetClassInternal;
	public String methodName;
	public Type[] targetMethodArgs;
	public Type targetMethodRet;
	public String prefixForInsetMethod = "";
	public String postfixForInsetMethod = "";
	public String signatureForInsetMethod = null;
	public String[] exceptionsForInsetMethod = null;
	public boolean callThis = true;
	public boolean isInterface = false;
	
	public String desc = "";
	public String targetMethod = "";
	public boolean checkcast = true;
	
	public SuperWrapperTransformerContainer(String containerClassName, String targetInternal, String methodName, Type[] targetMethodArgs, Type targetMethodRet) {
		this.containerClassName = containerClassName;
		this.targetClass = targetInternal.replace('/', '.');
		this.targetClassInternal = targetInternal;
		this.methodName = methodName;
		this.targetMethodArgs = targetMethodArgs;
		this.targetMethodRet = targetMethodRet;
	}
	
	public void setPostfixForInsertMethod(String postfixForInsetMethod) {
		this.postfixForInsetMethod = postfixForInsetMethod;
	}
	
	public void setPrefixForInsertMethod(String prefixForInsetMethod) {
		this.prefixForInsetMethod = prefixForInsetMethod;
	}
	
	public void setSignatureForInsertMethod(String signatureForInsetMethod) {
		this.signatureForInsetMethod = signatureForInsetMethod;
	}
	
	public void setExceptionsForInsetMethod(List<String> exceptionsForInsetMethod) {
		setExceptionsForInsetMethod(exceptionsForInsetMethod.toArray(new String[0]));
	}
	
	public void setExceptionsForInsetMethod(String[] exceptionsForInsetMethod) {
		this.exceptionsForInsetMethod = exceptionsForInsetMethod;
	}
	
	public void setCallThis(boolean callThis) {
		this.callThis = callThis;
	}
	
	public void setIsInterface(boolean isInterface) {
		this.isInterface = isInterface;
	}
	
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public String getInsertMethodName() {
		return prefixForInsetMethod + methodName + postfixForInsetMethod;
	}
	
	public String getInsertMethodDesc() {
		return Type.getMethodDescriptor(targetMethodRet, targetMethodArgs);
	}
	
	public void setTargetMethod(String targetMethod) {
		this.targetMethod = targetMethod;
	}
	
	public void setCheckcast(boolean checkcast) {
		this.checkcast = checkcast;
	}
}
