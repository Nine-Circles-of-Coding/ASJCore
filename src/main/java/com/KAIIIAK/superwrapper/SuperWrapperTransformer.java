package com.KAIIIAK.superwrapper;

import com.KAIIIAK.KASMLib.KASMLib;
import com.KAIIIAK.classManipulators.SomeUtil;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.SideOnly;
import gloomyfolken.hooklib.asm.HookLogger;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static org.objectweb.asm.Opcodes.*;

public class SuperWrapperTransformer implements IClassTransformer {
	
	// AlexSocol side parsing --
	private static final String SIDEONLY_DESC = Type.getDescriptor(SideOnly.class);
	private static final String SIDE = FMLLaunchHandler.side().name();
	// -- end
	
	public static final String SUPERWRAPPER_DESC = Type.getDescriptor(SuperWrapper.class);
	public static HookLogger logger = new HookLogger.Log4JLogger("SuperWrapper");
	public static List<SuperWrapperTransformerContainer> registeredContainers = new ArrayList<>();
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (basicClass == null) return null;

		boolean needToTransform = false;
		for (SuperWrapperTransformerContainer container : registeredContainers) {
			if (name.equals(container.containerClassName) || name.equals(container.targetClass.getInternalName())) {
				needToTransform = true;
				break;
			}
		}
		if (!needToTransform) return basicClass;

		ClassReader classReader = new ClassReader(basicClass);
		ClassNode classNode = new ClassNode();
		classReader.accept(classNode, 0);
		long cng = 0;
		
		for (SuperWrapperTransformerContainer container : registeredContainers) {
			try {
				if (container.containerClassName.equals(classNode.name)) {
					if (classNode.methods == null) throw new IllegalArgumentException(String.format("SuperWrapper container %s must have at least one SuperWrapper method", classNode.name));
					
					logger.debug("Patching SuperWrapper container " + transformedName);
					
					for (MethodNode methodNode : classNode.methods) {
						if (!isMethodTheSameAsInContainer(methodNode, container)) continue;
						
						logger.debug("Patching SuperWrapper " + methodNode.name);
						
						Type methodType = Type.getMethodType(methodNode.desc);
						InsnList newInstructions = new InsnList();
						Type[] argumentTypes = methodType.getArgumentTypes();
						
						for (int i = 0; i < argumentTypes.length; i++)
							newInstructions.add(new VarInsnNode(argumentTypes[i].getOpcode(ILOAD), i));
						
						newInstructions.add(new MethodInsnNode(
								container.isInterface ? INVOKEINTERFACE : INVOKEVIRTUAL,
								container.targetClass.getInternalName(),
								container.getInsertMethodName(),
								container.getInsertMethodDesc(),
								container.isInterface
						));
						
						if (methodType.getReturnType() == Type.VOID_TYPE) {
							newInstructions.add(new InsnNode(RETURN));
						} else {
							newInstructions.add(new InsnNode(methodType.getReturnType().getOpcode(IRETURN)));
						}
						methodNode.instructions.clear();
						methodNode.instructions.add(newInstructions);
						
						cng++;
					}
				} else if (classNode.name.equals(container.targetClass.getInternalName())) {
					logger.debug("Injecting synthetic super bridge " + container.getInsertMethodName() + " to " + classNode.name);
					
					if (classNode.methods == null)
						classNode.methods = new ArrayList<>();
					
					MethodNode newMethod = new MethodNode(ACC_PUBLIC, container.getInsertMethodName(), container.getInsertMethodDesc(), container.signatureForInsetMethod, container.exceptionsForInsetMethod);
					newMethod.instructions = new InsnList();
					InsnList instructions = newMethod.instructions;
					instructions.add(new VarInsnNode(container.targetClass.getOpcode(ILOAD), 0));
					for (int i = 0; i < container.targetMethodArgs.length; i++) {
						instructions.add(new VarInsnNode(container.targetMethodArgs[i].getOpcode(ILOAD), i + 1));
					}
					
					String targetMethod = McpToSrg.getTargetMethodMatchingNameAndDesc(classNode.methods, container.targetMethod, container.getInsertMethodDesc());
					
					instructions.add(new MethodInsnNode(
							container.callThis ? INVOKESPECIAL : INVOKEVIRTUAL,
							container.targetClass.getInternalName(),
							targetMethod,
							container.getInsertMethodDesc(),
							false
					));
					
					if (container.targetMethodRet == Type.VOID_TYPE) {
						instructions.add(new InsnNode(RETURN));
					} else {
						instructions.add(new InsnNode(container.targetMethodRet.getOpcode(IRETURN)));
					}
					classNode.methods.add(newMethod);
					cng++;
				}
			} catch (Exception e) {
				logger.error("Exception while transforming class " + transformedName, e);
				throw e;
			}
		}
		
		if (cng > 0) {
			logger.debug("Trying to make " + cng + " changes in " + name + "(" + transformedName + ")");
			
			try {
				ClassWriter classWriter = new ClassWriter(classReader, 0);
				classNode.accept(classWriter);
				byte[] bytes = classWriter.toByteArray();
				
				try {
					if (KASMLib.has2DumpChangedClasses) dump:{
						File file = new File("ASJCoreDumpClasses/SuperWrapper/" + transformedName.replaceAll("\\.", "/") + ".class");
						if (!file.getParentFile().mkdirs())
							break dump; // don't crash if can't dump
						IOUtils.write(bytes, Files.newOutputStream(file.toPath()));
					}
					if (KASMLib.has2DumpUnchangedClasses) dump:{
						File file = new File("ASJCoreDumpClasses/SuperWrapper/" + transformedName.replaceAll("\\.", "/") + "UNCHANGED.class");
						if (!file.getParentFile().mkdirs())
							break dump; // don't crash if can't dump
						IOUtils.write(basicClass, Files.newOutputStream(file.toPath()));
					}
				} catch (Exception e) {
					logger.trace("Can't dump " + transformedName + " class: " + e.getMessage());
				}
				
				return bytes;
			} catch (Exception e) {
				logger.error("Exception while making changes in class " + transformedName, e);
				throw new RuntimeException(e);
			}
		}
		
		return basicClass;
	}
	
	private boolean isMethodTheSameAsInContainer(MethodNode methodNode, SuperWrapperTransformerContainer container) {
		return methodNode.name.equals(container.methodName) && hasSuperWrapperAnnotation(methodNode) && methodNode.desc.equals(container.desc);
	}
	
	private boolean hasSuperWrapperAnnotation(MethodNode methodNode) {
		return containsAnnotation(methodNode.visibleAnnotations) || containsAnnotation(methodNode.invisibleAnnotations);
	}
	
	private boolean containsAnnotation(List<AnnotationNode> annotations) {
		if (annotations != null) {
			for (AnnotationNode annotationNode : annotations) {
				if (SUPERWRAPPER_DESC.equals(annotationNode.desc)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Регистрирует класс, в котором находятся методы с аннотацией {@link SuperWrapper}
	 */
	@SuppressWarnings({"DataFlowIssue", "unused"})
	public static void registerSuperWrapperContainer(String clazz) {
		logger.debug("Parsing SuperWrappers container " + clazz);
		try {
			registerSuperWrapperContainer(IOUtils.toByteArray(SuperWrapperTransformer.class.getResourceAsStream('/' + clazz.replace('.', '/') + ".class")));
		} catch (IOException e) {
			logger.error("Cannot parse SuperWrappers container " + clazz, e);
			throw new RuntimeException(e);
		}
	}
	
	private static void registerSuperWrapperContainer(byte[] clazzBytes) {
		try {
			ClassReader classReader = new ClassReader(clazzBytes);
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			
			if (classNode.methods == null) throw new IllegalArgumentException(String.format("SuperWrapper container %s must have at least one SuperWrapper method", classNode.name));
			
			boolean found = false;
			
			for (MethodNode methodNode : classNode.methods) {
				boolean containsAnnotation = false;
				String signatureFromAnnotation = null;
				String targetMethodFromAnnotation = null;
				List<String> exceptionsFromAnnotation = null;
				String methodPostfixFromAnnotation = null;
				String methodPrefixFromAnnotation = null;
				Boolean callThis = true;
				Boolean isInterface = false;
				
				List<AnnotationNode> annotations = new ArrayList<>();
				if (methodNode.visibleAnnotations != null) annotations.addAll(methodNode.visibleAnnotations);
				if (methodNode.invisibleAnnotations != null) annotations.addAll(methodNode.invisibleAnnotations);
				
				AnnotationNode sideOnlyAnnotation = annotations.stream().filter(it -> SIDEONLY_DESC.equals(it.desc)).findFirst().orElse(null);
				if (sideOnlyAnnotation != null) {
					String targetSide = ((String[]) sideOnlyAnnotation.values.get(1))[1];
					if (!SIDE.equals(targetSide)) {
						logger.debug("Skipping SuperWrapper method " + methodNode.name + methodNode.desc + " for invalid side " + targetSide);
						continue;
					}
				}
				
				for (AnnotationNode annotationNode : annotations) {
					if (!SUPERWRAPPER_DESC.equals(annotationNode.desc)) continue;
					
					if ((methodNode.access & ACC_STATIC) != ACC_STATIC) throw new IllegalArgumentException(String.format("SuperWrapper method %s$%s must be static!", classNode.name, methodNode.name));
					
					if (annotationNode.values != null) {
						Map<String, Object> annotationArgs = SomeUtil.convertListToMap(annotationNode.values);
						if (annotationArgs.containsKey("targetMethod")) {
							targetMethodFromAnnotation = (String) annotationArgs.get("targetMethod");
						}
						if (annotationArgs.containsKey("methodNamePrefix")) {
							methodPrefixFromAnnotation = (String) annotationArgs.get("methodNamePrefix");
						}
						if (annotationArgs.containsKey("methodNamePostfix")) {
							methodPostfixFromAnnotation = (String) annotationArgs.get("methodNamePostfix");
						}
						if (annotationArgs.containsKey("exceptions")) {
							//noinspection unchecked
							exceptionsFromAnnotation = (List<String>) annotationArgs.get("exceptions");
						}
						if (annotationArgs.containsKey("signature")) {
							signatureFromAnnotation = (String) annotationArgs.get("signature");
						}
						if (annotationArgs.containsKey("callThis")) {
							callThis = (Boolean) annotationArgs.get("callThis");
						}
						if (annotationArgs.containsKey("isInterface")) {
							isInterface = (Boolean) annotationArgs.get("isInterface");
						}
					}
					
					containsAnnotation = true;
					break;
				}
				
				if (!containsAnnotation)
					continue;
				
				Type methodType = Type.getMethodType(methodNode.desc);
				Type[] argTypes = methodType.getArgumentTypes();
				
				if (argTypes.length == 0) throw new IllegalArgumentException(String.format("SuperWrapper method %s$%s must have target class as first argument!", classNode.name, methodNode.name));
				
				Type[] newDescTypes = new Type[argTypes.length - 1];
				System.arraycopy(argTypes, 1, newDescTypes, 0, newDescTypes.length);
				Type newDescReturnType = methodType.getReturnType();
				
				SuperWrapperTransformerContainer container = new SuperWrapperTransformerContainer(classNode.name, argTypes[0], methodNode.name, newDescTypes, newDescReturnType);
				container.setPrefixForInsertMethod(methodPrefixFromAnnotation == null ? "Super__" : methodPrefixFromAnnotation);
				container.setPostfixForInsertMethod(methodPostfixFromAnnotation == null ? "__Wrapper" : methodPostfixFromAnnotation);
				
				container.setSignatureForInsertMethod(signatureFromAnnotation != null ? signatureFromAnnotation : methodNode.signature == null ? null : methodNode.signature.replaceFirst("\\(L" + container.targetClass.getInternalName() + ";", "("));
				container.setExceptionsForInsetMethod(exceptionsFromAnnotation != null ? exceptionsFromAnnotation : methodNode.exceptions);
				container.setCallThis(callThis);
				container.setIsInterface(isInterface);
				container.setDesc(methodNode.desc);
				container.setTargetMethod(targetMethodFromAnnotation != null ? targetMethodFromAnnotation : methodNode.name);
				
				registeredContainers.add(container);
				found = true;
				logger.trace("Registered SuperWrapper method " + methodNode.name + methodNode.desc);
			}
			
			if (!found) throw new IllegalArgumentException(String.format("SuperWrapper container %s must have at least one SuperWrapper method", classNode.name));
		} catch (Exception e) {
			logger.error("Cannot parse SuperWrappers container's bytes", e);
			throw e;
		}
	}
}
