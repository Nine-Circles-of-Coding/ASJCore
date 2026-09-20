package com.KAIIIAK.KASMLib;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public abstract class KASMWorker {
	
	private static final Set<String> POSSIBLE_CAPABILITIES = Collections.unmodifiableSet(collectBaseMethods());
	public static final Map<Class<?>, Set<String>> AUTO_CAPABILITY = new HashMap<>();
	private static final Object AUTO_CAPABILITY_LOCK = new Object();
	
	public KASMWorker() {
		ensureCapabilitiesComputed(getClass());
	}
	
	private static void ensureCapabilitiesComputed(Class<?> startClass) {
		if (AUTO_CAPABILITY.containsKey(startClass)) {
			return;
		}
		
		synchronized (AUTO_CAPABILITY_LOCK) {
			if (AUTO_CAPABILITY.containsKey(startClass)) {
				return;
			}
			
			List<Class<?>> chain = new ArrayList<>();
			Class<?> clazz = startClass;
			
			while (clazz != null && KASMWorker.class.isAssignableFrom(clazz) && clazz != KASMWorker.class) {
				chain.add(clazz);
				clazz = clazz.getSuperclass();
			}
			
			Collection<String> inherited = Collections.emptySet();
			
			for (int i = chain.size() - 1; i >= 0; i--) {
				Class<?> current = chain.get(i);
				
				Collection<String> cached = AUTO_CAPABILITY.get(current);
				if (cached != null) {
					inherited = cached;
					continue;
				}
				
				Set<String> result = new HashSet<>(inherited);
				
				for (Method method : current.getDeclaredMethods()) {
					String sig = methodToString(method);
					if (POSSIBLE_CAPABILITIES.contains(sig)) {
						result.add(sig);
					}
				}
				
				AUTO_CAPABILITY.put(current, Collections.unmodifiableSet(result));
			}
		}
	}
	
	private static Set<String> collectBaseMethods() {
		Set<String> result = new HashSet<>();
		for (WorkerCapability capability : WorkerCapability.values()) {
			result.add(capability.sig());
		}
		return result;
	}
	
	private static String methodToString(Method method) {
		StringBuilder sb = new StringBuilder();
		sb.append(method.getName()).append('(');
		
		Class<?>[] types = method.getParameterTypes();
		for (int i = 0; i < types.length; i++) {
			sb.append(types[i].getSimpleName());
			if (i + 1 < types.length) {
				sb.append(',');
			}
		}
		
		sb.append(')');
		return sb.toString();
	}
	
	public boolean handles(WorkerCapability cap) {
		Collection<String> set = AUTO_CAPABILITY.get(getClass());
		return set != null && set.contains(cap.sig());
	}
	
	
	public enum WorkerCapability {
		CLASS("workClass(ClassNode)"),
		CLASS_ATTRIBUTE("workClassAttribute(ClassNode,Attribute)"),
		CLASS_VISIBLE_ANNOTATION("workClassVisibleAnnotation(ClassNode,AnnotationNode)"),
		CLASS_INVISIBLE_ANNOTATION("workClassInvisibleAnnotation(ClassNode,AnnotationNode)"),
		CLASS_VISIBLE_TYPE_ANNOTATION("workClassVisibleTypeAnnotation(ClassNode,TypeAnnotationNode)"),
		CLASS_INVISIBLE_TYPE_ANNOTATION("workClassInvisibleTypeAnnotation(ClassNode,TypeAnnotationNode)"),
		
		INNER_CLASS("workInnerClassNode(ClassNode,InnerClassNode)"),
		INTERFACE("workInterface(ClassNode,AtomicReference)"),
		
		FIELD("workField(ClassNode,FieldNode)"),
		FIELD_ATTRIBUTE("workFieldAttribute(ClassNode,Attribute)"),
		FIELD_VISIBLE_ANNOTATION("workFieldVisibleAnnotation(ClassNode,FieldNode,AnnotationNode)"),
		FIELD_VISIBLE_TYPE_ANNOTATION("workFieldVisibleTypeAnnotation(ClassNode,FieldNode,TypeAnnotationNode)"),
		FIELD_INVISIBLE_ANNOTATION("workFieldInvisibleAnnotation(ClassNode,FieldNode,AnnotationNode)"),
		FIELD_INVISIBLE_TYPE_ANNOTATION("workFieldInvisibleTypeAnnotation(ClassNode,FieldNode,TypeAnnotationNode)"),
		
		METHOD("workMethod(ClassNode,MethodNode)"),
		METHOD_ATTRIBUTE("workMethodAttribute(ClassNode,MethodNode,Attribute)"),
		METHOD_LOCAL_VARIABLE("workMethodLocalVariable(ClassNode,MethodNode,LocalVariableNode)"),
		METHOD_ARGS("workMethodArgs(ClassNode,MethodNode,List)"),
		METHOD_RET("workMethodRet(ClassNode,MethodNode,Type)"),
		METHOD_VISIBLE_ANNOTATION("workMethodVisibleAnnotation(ClassNode,MethodNode,AnnotationNode)"),
		METHOD_INVISIBLE_ANNOTATION("workMethodInvisibleAnnotation(ClassNode,MethodNode,AnnotationNode)"),
		METHOD_VISIBLE_TYPE_ANNOTATION("workMethodVisibleTypeAnnotation(ClassNode,MethodNode,TypeAnnotationNode)"),
		METHOD_INVISIBLE_TYPE_ANNOTATION("workMethodInvisibleTypeAnnotation(ClassNode,MethodNode,TypeAnnotationNode)"),
		METHOD_VISIBLE_PARAMETER_ANNOTATION("workMethodVisibleParameterAnnotation(ClassNode,MethodNode,AnnotationNode)"),
		METHOD_INVISIBLE_PARAMETER_ANNOTATION("workMethodInvisibleParameterAnnotation(ClassNode,MethodNode,AnnotationNode)"),
		METHOD_VISIBLE_LOCAL_VARIABLE_ANNOTATION("workMethodVisibleLocalVariableAnnotation(ClassNode,MethodNode,LocalVariableAnnotationNode)"),
		METHOD_INVISIBLE_LOCAL_VARIABLE_ANNOTATION("workMethodInvisibleLocalVariableAnnotation(ClassNode,MethodNode,LocalVariableAnnotationNode)"),
		METHOD_TRY_CATCH_BLOCK("workMethodTryCatchBlock(ClassNode,MethodNode,TryCatchBlockNode)"),
		METHOD_PARAMETER("workMethodParameter(ClassNode,MethodNode,ParameterNode)"),
		
		INST_LDC("workInstNode(ClassNode,MethodNode,LdcInsnNode)"),
		INST_INSN("workInstNode(ClassNode,MethodNode,InsnNode)"),
		INST_FIELD("workInstNode(ClassNode,MethodNode,FieldInsnNode)"),
		INST_METHOD("workInstNode(ClassNode,MethodNode,MethodInsnNode)"),
		INST_INVOKE_DYNAMIC("workInstNode(ClassNode,MethodNode,InvokeDynamicInsnNode)"),
		INST_TYPE("workInstNode(ClassNode,MethodNode,TypeInsnNode)"),
		INST_JUMP("workInstNode(ClassNode,MethodNode,JumpInsnNode)"),
		INST_VAR("workInstNode(ClassNode,MethodNode,VarInsnNode)"),
		INST_INT("workInstNode(ClassNode,MethodNode,IntInsnNode)"),
		INST_IINC("workInstNode(ClassNode,MethodNode,IincInsnNode)"),
		INST_TABLE_SWITCH("workInstNode(ClassNode,MethodNode,TableSwitchInsnNode)"),
		INST_LOOKUP_SWITCH("workInstNode(ClassNode,MethodNode,LookupSwitchInsnNode)"),
		INST_MULTI_NEW_ARRAY("workInstNode(ClassNode,MethodNode,MultiANewArrayInsnNode)"),
		INST_FRAME("workInstNode(ClassNode,MethodNode,FrameNode)"),
		INST_LABEL("workInstNode(ClassNode,MethodNode,LabelNode)"),
		INST_LINE_NUMBER("workInstNode(ClassNode,MethodNode,LineNumberNode)");
		
		private final String sig;
		
		WorkerCapability(String sig) {
			this.sig = sig;
		}
		
		public String sig() {
			return sig;
		}
	}
	
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
	
	public boolean workMethodVisibleLocalVariableAnnotation(ClassNode classNode, MethodNode methodNode, LocalVariableAnnotationNode localVariableAnnotationNode) {
		return false;
	}
	
	public boolean workMethodInvisibleLocalVariableAnnotation(ClassNode classNode, MethodNode methodNode, LocalVariableAnnotationNode localVariableAnnotationNode) {
		return false;
	}
	
	public boolean workMethodParameter(ClassNode classNode, MethodNode methodNode, ParameterNode parameterNode) {
		return false;
	}
	
	public boolean workMethodInvisibleAnnotation(ClassNode classNode, MethodNode methodNode, AnnotationNode annotationNode) {
		return false;
	}
	
	public boolean workMethodVisibleAnnotation(ClassNode classNode, MethodNode methodNode, AnnotationNode annotationNode) {
		return false;
	}
	
	public boolean workMethodInvisibleTypeAnnotation(ClassNode classNode, MethodNode methodNode, TypeAnnotationNode typeAnnotationNode) {
		return false;
	}
	
	public boolean workMethodVisibleTypeAnnotation(ClassNode classNode, MethodNode methodNode, TypeAnnotationNode typeAnnotationNode) {
		return false;
	}
	
	public boolean workMethodTryCatchBlock(ClassNode classNode, MethodNode methodNode, TryCatchBlockNode tryCatchBlockNode) {
		return false;
	}
	
	public boolean workMethodInvisibleParameterAnnotation(ClassNode classNode, MethodNode methodNode, AnnotationNode annotationNode) {
		return false;
	}
	
	public boolean workMethodVisibleParameterAnnotation(ClassNode classNode, MethodNode methodNode, AnnotationNode annotationNode) {
		return false;
	}
	
	public boolean workInterface(ClassNode classNode, AtomicReference<String> interfaceA) {
		return false;
	}
	
	public boolean workMethodArgs(ClassNode classNode, MethodNode methodNode, List<Type> argTypes) {
		return false;
	}
	
	public Type workMethodRet(ClassNode classNode, MethodNode methodNode, Type returnType) {
		return returnType;
	}
	
	public boolean workFieldInvisibleAnnotation(ClassNode classNode, FieldNode fieldNode, AnnotationNode annotation) {
		return false;
	}
	
	public boolean workFieldInvisibleTypeAnnotation(ClassNode classNode, FieldNode fieldNode, TypeAnnotationNode annotation) {
		return false;
	}
	
	public boolean workFieldVisibleAnnotation(ClassNode classNode, FieldNode fieldNode, AnnotationNode annotation) {
		return false;
	}
	
	public boolean workFieldVisibleTypeAnnotation(ClassNode classNode, FieldNode fieldNode, TypeAnnotationNode annotation) {
		return false;
	}
}
