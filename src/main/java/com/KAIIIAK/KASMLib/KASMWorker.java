package com.KAIIIAK.KASMLib;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public abstract class KASMWorker {
	
	public long changes = 0; // have to increment everytime you made changes
	public boolean withRecalc = false;
	public String className;
	public String transformedClassName;
	
	public void workDataStart() {
		changes = 0;
	}
	
	public void workDataEnd() {
		
	}
	
	// return true to DELETE class/element in any of following:
	
	public boolean workClass(ClassNode classNode) {
		return false;
	}
	
	public boolean workClassAttribute(ClassNode classNode, Attribute attribute) {
		return false;
	}
	
	public boolean workFieldAttribute(ClassNode classNode, Attribute attribute) {
		return false;
	}
	
	public boolean workMethodAttribute(ClassNode classNode, MethodNode methodNode, Attribute attribute) {
		return false;
	}
	
	public boolean workInnerClassNode(ClassNode classNode, InnerClassNode innerClassNode) {
		return false;
	}
	
	public boolean workClassVisibleAnnotation(ClassNode classNode, AnnotationNode annotationNode) {
		return false;
	}
	
	public boolean workClassVisibleTypeAnnotation(ClassNode classNode, TypeAnnotationNode typeAnnotationNode) {
		return false;
	}
	
	public boolean workClassInvisibleAnnotation(ClassNode classNode, AnnotationNode annotationNode) {
		return false;
	}
	
	public boolean workClassInvisibleTypeAnnotation(ClassNode classNode, TypeAnnotationNode typeAnnotationNode) {
		return false;
	}
	
	public boolean workField(ClassNode classNode, FieldNode fieldNode) {
		return false;
	}
	
	public boolean workMethod(ClassNode classNode, MethodNode methodNode) {
		return false;
	}
	
	public boolean workMethodLocalVariable(ClassNode classNode, MethodNode methodNode, LocalVariableNode localVariableNode) {
		return false;
	}
	
	public boolean workInstNode(ClassNode classNode, MethodNode methodNode, TableSwitchInsnNode tableSwitchInsnNode) {
		return false;
	}
	
	public boolean workInstNode(ClassNode classNode, MethodNode methodNode, MultiANewArrayInsnNode multiANewArrayInsnNode) {
		return false;
	}
	
	public boolean workInstNode(ClassNode classNode, MethodNode methodNode, LookupSwitchInsnNode lookupSwitchInsnNode) {
		return false;
	}
	
	public boolean workInstNode(ClassNode classNode, MethodNode methodNode, LdcInsnNode ldcInsnNode) {
		return false;
	}
	
	public boolean workInstNode(ClassNode classNode, MethodNode methodNode, InvokeDynamicInsnNode invokeDynamicInsnNode) {
		return false;
	}
	
	public boolean workInstNode(ClassNode classNode, MethodNode methodNode, IntInsnNode intInsnNode) {
		return false;
	}
	
	public boolean workInstNode(ClassNode classNode, MethodNode methodNode, IincInsnNode iincInsnNode) {
		return false;
	}
	
	public boolean workInstNode(ClassNode classNode, MethodNode methodNode, FrameNode frameNode) {
		return false;
	}
	
	public boolean workInstNode(ClassNode classNode, MethodNode methodNode, TypeInsnNode typeInsnNode) {
		return false;
	}
	
	public boolean workInstNode(ClassNode classNode, MethodNode methodNode, LabelNode labelNode) {
		return false;
	}
	
	public boolean workInstNode(ClassNode classNode, MethodNode methodNode, LineNumberNode lineNumberNode) {
		return false;
	}
	
	public boolean workInstNode(ClassNode classNode, MethodNode methodNode, FieldInsnNode fieldInsnNode) {
		return false;
	}
	
	public boolean workInstNode(ClassNode classNode, MethodNode methodNode, JumpInsnNode jumpInsnNode) {
		return false;
	}
	
	public boolean workInstNode(ClassNode classNode, MethodNode methodNode, VarInsnNode varInsnNode) {
		return false;
	}
	
	public boolean workInstNode(ClassNode classNode, MethodNode methodNode, MethodInsnNode methodInsnNode) {
		return false;
	}
	
	public boolean workInstNode(ClassNode classNode, MethodNode methodNode, InsnNode insnNode) {
		return false;
	}
	
	public boolean workMethodVisibleLocalVariableAnnotations(ClassNode classNode, MethodNode methodNode, LocalVariableAnnotationNode localVariableAnnotationNode) {
		return false;
	}
	
	public boolean workMethodInvisibleLocalVariableAnnotations(ClassNode classNode, MethodNode methodNode, LocalVariableAnnotationNode localVariableAnnotationNode) {
		return false;
	}
	
	public boolean workMethodParameters(ClassNode classNode, MethodNode methodNode, ParameterNode parameterNode) {
		return false;
	}
	
	public boolean workMethodInvisibleAnnotations(ClassNode classNode, MethodNode methodNode, AnnotationNode annotationNode) {
		return false;
	}
	
	public boolean workMethodVisibleAnnotations(ClassNode classNode, MethodNode methodNode, AnnotationNode annotationNode) {
		return false;
	}
	
	public boolean workMethodInvisibleTypeAnnotations(ClassNode classNode, MethodNode methodNode, TypeAnnotationNode typeAnnotationNode) {
		return false;
	}
	
	public boolean workMethodVisibleTypeAnnotations(ClassNode classNode, MethodNode methodNode, TypeAnnotationNode typeAnnotationNode) {
		return false;
	}
	
	public boolean workMethodTryCatchBlocks(ClassNode classNode, MethodNode methodNode, TryCatchBlockNode tryCatchBlockNode) {
		return false;
	}
	
	public boolean workMethodInvisibleParameterAnnotations(ClassNode classNode, MethodNode methodNode, AnnotationNode annotationNode) {
		return false;
	}
	
	public boolean workMethodVisibleParameterAnnotations(ClassNode classNode, MethodNode methodNode, AnnotationNode annotationNode) {
		return false;
	}
	
	public boolean workInterface(ClassNode classNode, AtomicReference<String> interfaceA) {
		return false;
	}
	
	public boolean workMethodArg(ClassNode classNode, MethodNode methodNode, AtomicReference<Type> type) {
		return false;
	}
	
	public Type workMethodRet(ClassNode classNode, MethodNode methodNode, Type returnType) {
		return returnType;
	}
	
	public boolean workFieldInvisibleAnnotations(ClassNode classNode, FieldNode fieldNode, AnnotationNode annotation) {
		return false;
	}
	
	public boolean workFieldInvisibleTypeAnnotations(ClassNode classNode, FieldNode fieldNode, TypeAnnotationNode annotation) {
		return false;
	}
	
	public boolean workFieldVisibleAnnotations(ClassNode classNode, FieldNode fieldNode, AnnotationNode annotation) {
		return false;
	}
	
	public boolean workFieldVisibleTypeAnnotations(ClassNode classNode, FieldNode fieldNode, TypeAnnotationNode annotation) {
		return false;
	}
}
