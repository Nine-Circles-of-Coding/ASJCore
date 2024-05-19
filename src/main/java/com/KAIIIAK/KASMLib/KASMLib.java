package com.KAIIIAK.KASMLib;

import com.KAIIIAK.nullsafety.Opt;
import gloomyfolken.hooklib.asm.HookLogger;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class KASMLib implements IClassTransformer {
	
	public static HookLogger logger = new HookLogger.SystemOutLogger("KASMLib");

	public boolean has2DumpChangedClasses = false;
	public boolean has2DumpUnchangedClasses = false; // that has tp change
	public boolean isInfoDebugEnabled = false;
	
	public static Set<KASMWorker> workers = new HashSet<>();
	public boolean withRecalc;
	
	public KASMLib(boolean recalc) {
		withRecalc = recalc;
	}
	
	public static void register(KASMWorker worker) {
		workers.add(worker);
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		try {
			if (basicClass == null) return null;
			
			ClassReader classReader = new ClassReader(basicClass);
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			ClassWriter classWriter;
			
			if (withRecalc)
				classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			else
				classWriter = new ClassWriter(classReader, 0);
			
			long cng = 0;
			
			for (KASMWorker asmWorker : Opt.it(workers)) {
				if (asmWorker == null) continue;
				if (asmWorker.withRecalc != withRecalc) continue;
				asmWorker.className = name;
				asmWorker.transformedClassName = transformedName;
				asmWorker.workDataStart();
				if (asmWorker.workClass(classNode))
					return null;
				if (classNode.attrs != null)
					classNode.attrs.removeIf(attribute -> asmWorker.workClassAttribute(classNode, attribute));
				if (classNode.innerClasses != null)
					classNode.innerClasses.removeIf(innerClassNode -> asmWorker.workInnerClassNode(classNode, innerClassNode));
				if (classNode.visibleAnnotations != null)
					classNode.visibleAnnotations.removeIf(annotationNode -> asmWorker.workClassVisibleAnnotation(classNode, annotationNode));
				if (classNode.visibleTypeAnnotations != null)
					classNode.visibleTypeAnnotations.removeIf(typeAnnotationNode -> asmWorker.workClassVisibleTypeAnnotation(classNode, typeAnnotationNode));
				if (classNode.invisibleAnnotations != null)
					classNode.invisibleAnnotations.removeIf(annotationNode -> asmWorker.workClassInvisibleAnnotation(classNode, annotationNode));
				if (classNode.invisibleTypeAnnotations != null)
					classNode.invisibleTypeAnnotations.removeIf(typeAnnotationNode -> asmWorker.workClassInvisibleTypeAnnotation(classNode, typeAnnotationNode));
				if (classNode.fields != null) {
					classNode.fields.removeIf(fieldNode -> asmWorker.workField(classNode, fieldNode));
					for (FieldNode fieldNode : classNode.fields) {
						if (fieldNode.attrs != null)
							fieldNode.attrs.removeIf(attribute -> asmWorker.workFieldAttribute(classNode, attribute));
						
						if (fieldNode.invisibleAnnotations != null)
							fieldNode.invisibleAnnotations.removeIf(annotation -> asmWorker.workFieldInvisibleAnnotations(classNode, fieldNode, annotation));
						if (fieldNode.invisibleTypeAnnotations != null)
							fieldNode.invisibleTypeAnnotations.removeIf(annotation -> asmWorker.workFieldInvisibleTypeAnnotations(classNode, fieldNode, annotation));
						if (fieldNode.visibleAnnotations != null)
							fieldNode.visibleAnnotations.removeIf(annotation -> asmWorker.workFieldVisibleAnnotations(classNode, fieldNode, annotation));
						if (fieldNode.visibleTypeAnnotations != null)
							fieldNode.visibleTypeAnnotations.removeIf(annotation -> asmWorker.workFieldVisibleTypeAnnotations(classNode, fieldNode, annotation));
					}
				}
				
				if (classNode.interfaces != null) {
					List<String> interfacesL = new ArrayList<>();
					for (String interf : classNode.interfaces) {
						AtomicReference<String> interfaceA = new AtomicReference<>(interf);
						if (!asmWorker.workInterface(classNode, interfaceA)) {
							interfacesL.add(interfaceA.get());
						}
					}
					classNode.interfaces = interfacesL;
				}
				
				if (classNode.methods != null) {
					classNode.methods.removeIf(methodNode -> asmWorker.workMethod(classNode, methodNode));
					for (MethodNode methodNode : classNode.methods) {
						if (methodNode.attrs != null)
							methodNode.attrs.removeIf(attribute -> asmWorker.workMethodAttribute(classNode, methodNode, attribute));
						if (methodNode.localVariables != null)
							methodNode.localVariables.removeIf(localVariableNode -> asmWorker.workMethodLocalVariable(classNode, methodNode, localVariableNode));
						if (methodNode.visibleLocalVariableAnnotations != null)
							methodNode.visibleLocalVariableAnnotations.removeIf(localVariableAnnotationNode -> asmWorker.workMethodVisibleLocalVariableAnnotations(classNode, methodNode, localVariableAnnotationNode));
						if (methodNode.invisibleLocalVariableAnnotations != null)
							methodNode.invisibleLocalVariableAnnotations.removeIf(localVariableAnnotationNode -> asmWorker.workMethodInvisibleLocalVariableAnnotations(classNode, methodNode, localVariableAnnotationNode));
						if (methodNode.parameters != null)
							methodNode.parameters.removeIf(parameterNode -> asmWorker.workMethodParameters(classNode, methodNode, parameterNode));
						if (methodNode.invisibleAnnotations != null)
							methodNode.invisibleAnnotations.removeIf(annotationNode -> asmWorker.workMethodInvisibleAnnotations(classNode, methodNode, annotationNode));
						if (methodNode.visibleAnnotations != null)
							methodNode.visibleAnnotations.removeIf(annotationNode -> asmWorker.workMethodVisibleAnnotations(classNode, methodNode, annotationNode));
						if (methodNode.invisibleTypeAnnotations != null)
							methodNode.invisibleTypeAnnotations.removeIf(typeAnnotationNode -> asmWorker.workMethodInvisibleTypeAnnotations(classNode, methodNode, typeAnnotationNode));
						if (methodNode.visibleTypeAnnotations != null)
							methodNode.visibleTypeAnnotations.removeIf(typeAnnotationNode -> asmWorker.workMethodVisibleTypeAnnotations(classNode, methodNode, typeAnnotationNode));
						if (methodNode.tryCatchBlocks != null)
							methodNode.tryCatchBlocks.removeIf(tryCatchBlockNode -> asmWorker.workMethodTryCatchBlocks(classNode, methodNode, tryCatchBlockNode));
						for (List<AnnotationNode> listAnnotationNode : Opt.it(methodNode.invisibleParameterAnnotations)) {
							if (listAnnotationNode != null)
								listAnnotationNode.removeIf(annotationNode -> asmWorker.workMethodInvisibleParameterAnnotations(classNode, methodNode, annotationNode));
						}
						for (List<AnnotationNode> listAnnotationNode : Opt.it(methodNode.visibleParameterAnnotations)) {
							if (listAnnotationNode != null)
								listAnnotationNode.removeIf(annotationNode -> asmWorker.workMethodVisibleParameterAnnotations(classNode, methodNode, annotationNode));
						}
						
						Type methodType = Type.getMethodType(methodNode.desc);
						List<Type> argTypes = new ArrayList<>();
						Type[] argumentTypes = methodType.getArgumentTypes();
						for (Type type : argumentTypes) {
							AtomicReference<Type> typeA = new AtomicReference<>(type);
							if (!asmWorker.workMethodArg(classNode, methodNode, typeA)) {
								argTypes.add(typeA.get());
							}
						}
						Type returnType = asmWorker.workMethodRet(classNode, methodNode, methodType.getReturnType());
						methodNode.desc = Type.getMethodDescriptor(returnType, argTypes.toArray(new Type[0]));
						
						if (methodNode.instructions != null) {
							ListIterator<AbstractInsnNode> instructions = methodNode.instructions.iterator();
							while (instructions.hasNext()) {
								AbstractInsnNode abstractInsnNode = instructions.next();
								if (abstractInsnNode instanceof TableSwitchInsnNode) {
									if (asmWorker.workInstNode(classNode, methodNode, (TableSwitchInsnNode) abstractInsnNode))
										methodNode.instructions.remove(abstractInsnNode);
								}
								if (abstractInsnNode instanceof MultiANewArrayInsnNode) {
									if (asmWorker.workInstNode(classNode, methodNode, (MultiANewArrayInsnNode) abstractInsnNode))
										methodNode.instructions.remove(abstractInsnNode);
								}
								if (abstractInsnNode instanceof LookupSwitchInsnNode) {
									if (asmWorker.workInstNode(classNode, methodNode, (LookupSwitchInsnNode) abstractInsnNode))
										methodNode.instructions.remove(abstractInsnNode);
								}
								if (abstractInsnNode instanceof LdcInsnNode) {
									if (asmWorker.workInstNode(classNode, methodNode, (LdcInsnNode) abstractInsnNode))
										methodNode.instructions.remove(abstractInsnNode);
								}
								if (abstractInsnNode instanceof InvokeDynamicInsnNode) {
									if (asmWorker.workInstNode(classNode, methodNode, (InvokeDynamicInsnNode) abstractInsnNode))
										methodNode.instructions.remove(abstractInsnNode);
								}
								if (abstractInsnNode instanceof IntInsnNode) {
									if (asmWorker.workInstNode(classNode, methodNode, (IntInsnNode) abstractInsnNode))
										methodNode.instructions.remove(abstractInsnNode);
								}
								if (abstractInsnNode instanceof IincInsnNode) {
									if (asmWorker.workInstNode(classNode, methodNode, (IincInsnNode) abstractInsnNode))
										methodNode.instructions.remove(abstractInsnNode);
								}
								if (abstractInsnNode instanceof FrameNode) {
									if (asmWorker.workInstNode(classNode, methodNode, (FrameNode) abstractInsnNode))
										methodNode.instructions.remove(abstractInsnNode);
								}
								if (abstractInsnNode instanceof TypeInsnNode) {
									if (asmWorker.workInstNode(classNode, methodNode, (TypeInsnNode) abstractInsnNode))
										methodNode.instructions.remove(abstractInsnNode);
								}
								if (abstractInsnNode instanceof LabelNode) {
									if (asmWorker.workInstNode(classNode, methodNode, (LabelNode) abstractInsnNode))
										methodNode.instructions.remove(abstractInsnNode);
								}
								if (abstractInsnNode instanceof LineNumberNode) {
									if (asmWorker.workInstNode(classNode, methodNode, (LineNumberNode) abstractInsnNode))
										methodNode.instructions.remove(abstractInsnNode);
								}
								if (abstractInsnNode instanceof FieldInsnNode) {
									if (asmWorker.workInstNode(classNode, methodNode, (FieldInsnNode) abstractInsnNode))
										methodNode.instructions.remove(abstractInsnNode);
								}
								if (abstractInsnNode instanceof JumpInsnNode) {
									if (asmWorker.workInstNode(classNode, methodNode, (JumpInsnNode) abstractInsnNode))
										methodNode.instructions.remove(abstractInsnNode);
								}
								if (abstractInsnNode instanceof VarInsnNode) {
									if (asmWorker.workInstNode(classNode, methodNode, (VarInsnNode) abstractInsnNode))
										methodNode.instructions.remove(abstractInsnNode);
								}
								if (abstractInsnNode instanceof MethodInsnNode) {
									if (asmWorker.workInstNode(classNode, methodNode, (MethodInsnNode) abstractInsnNode))
										methodNode.instructions.remove(abstractInsnNode);
								}
								if (abstractInsnNode instanceof InsnNode) {
									if (asmWorker.workInstNode(classNode, methodNode, (InsnNode) abstractInsnNode))
										methodNode.instructions.remove(abstractInsnNode);
								}
							}
						}
					}
				}
				cng += asmWorker.changes;
				asmWorker.workDataEnd();
			}
			if (cng > 0) {
				if (isInfoDebugEnabled)
					logger.debug(String.format("Trying to make %d changes in %s(%s)", cng, name, transformedName));
				classNode.accept(classWriter);

				byte[] bytes = classWriter.toByteArray();
				
				if (has2DumpChangedClasses) {
					File file = new File("ASJCoreDumpClasses/KASMLib/" + transformedName.replaceAll("\\.", "/") + ".class");
					file.getParentFile().mkdirs();
					IOUtils.write(bytes, Files.newOutputStream(file.toPath()));
				}
				if (has2DumpUnchangedClasses) {
					File file = new File("ASJCoreDumpClasses/KASMLib/" + transformedName.replaceAll("\\.", "/") + "UNCHANGED.class");
					file.getParentFile().mkdirs();
					IOUtils.write(basicClass, Files.newOutputStream(file.toPath()));
				}

				return bytes;
			}
		} catch (Exception e) {
			logger.error(String.format("Error transforming class %s(%s)", name, transformedName), e);
		}
		return basicClass;
	}
}
