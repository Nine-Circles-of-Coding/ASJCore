package com.KAIIIAK.KASMLib;

import com.KAIIIAK.nullsafety.Opt;
import gloomyfolken.hooklib.asm.HookLogger;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.KAIIIAK.KASMLib.KASMWorker.WorkerCapability.*;

public class KASMLib implements IClassTransformer {
	
	public static HookLogger logger = new HookLogger.Log4JLogger("KASMLib");

	public static boolean has2DumpChangedClasses = false;
	public static boolean has2DumpUnchangedClasses = false; // that has tp change
	
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
			
			long cng = 0;
			
			for (KASMWorker asmWorker : Opt.it(workers)) {
				if (asmWorker == null) continue;
				if (asmWorker.withRecalc != withRecalc) continue;
				asmWorker.className = name;
				asmWorker.transformedClassName = transformedName;
				asmWorker.workDataStart();
				if (asmWorker.handles(CLASS)&&asmWorker.workClass(classNode))
					return null;
				if (classNode.attrs != null&&asmWorker.handles(CLASS_ATTRIBUTE))
					classNode.attrs.removeIf(attribute -> asmWorker.workClassAttribute(classNode, attribute));
				if (classNode.innerClasses != null&&asmWorker.handles(INNER_CLASS))
					classNode.innerClasses.removeIf(innerClassNode -> asmWorker.workInnerClassNode(classNode, innerClassNode));
				if (classNode.visibleAnnotations != null&&asmWorker.handles(CLASS_VISIBLE_ANNOTATION))
					classNode.visibleAnnotations.removeIf(annotationNode -> asmWorker.workClassVisibleAnnotation(classNode, annotationNode));
				if (classNode.visibleTypeAnnotations != null&&asmWorker.handles(CLASS_VISIBLE_TYPE_ANNOTATION))
					classNode.visibleTypeAnnotations.removeIf(typeAnnotationNode -> asmWorker.workClassVisibleTypeAnnotation(classNode, typeAnnotationNode));
				if (classNode.invisibleAnnotations != null&&asmWorker.handles(CLASS_INVISIBLE_ANNOTATION))
					classNode.invisibleAnnotations.removeIf(annotationNode -> asmWorker.workClassInvisibleAnnotation(classNode, annotationNode));
				if (classNode.invisibleTypeAnnotations != null&&asmWorker.handles(CLASS_INVISIBLE_TYPE_ANNOTATION))
					classNode.invisibleTypeAnnotations.removeIf(typeAnnotationNode -> asmWorker.workClassInvisibleTypeAnnotation(classNode, typeAnnotationNode));
				if (classNode.fields != null) {
					if(asmWorker.handles(FIELD))
					{
						classNode.fields.removeIf(fieldNode -> asmWorker.workField(classNode, fieldNode));
					}
					boolean shouldHandleFieldAttribute = asmWorker.handles(FIELD_ATTRIBUTE);
					boolean shouldHandleFieldInvisibleAnnotation = asmWorker.handles(FIELD_INVISIBLE_ANNOTATION);
					boolean shouldHandleFieldInvisibleTypeAnnotation = asmWorker.handles(FIELD_INVISIBLE_TYPE_ANNOTATION);
					boolean shouldHandleFieldVisibleAnnotation = asmWorker.handles(FIELD_VISIBLE_ANNOTATION);
					boolean shouldHandleFieldVisibleTypeAnnotation = asmWorker.handles(FIELD_VISIBLE_TYPE_ANNOTATION);
					boolean shouldHandleFields = shouldHandleFieldAttribute
							||shouldHandleFieldInvisibleAnnotation
							||shouldHandleFieldInvisibleTypeAnnotation
							||shouldHandleFieldVisibleAnnotation
							||shouldHandleFieldVisibleTypeAnnotation
							;

					if(shouldHandleFields)
					{
						for (FieldNode fieldNode : classNode.fields) {
							if (fieldNode.attrs != null && shouldHandleFieldAttribute)
								fieldNode.attrs.removeIf(attribute -> asmWorker.workFieldAttribute(classNode, attribute));
							if (fieldNode.invisibleAnnotations != null && shouldHandleFieldInvisibleAnnotation)
								fieldNode.invisibleAnnotations.removeIf(annotation -> asmWorker.workFieldInvisibleAnnotation(classNode, fieldNode, annotation));
							if (fieldNode.invisibleTypeAnnotations != null&& shouldHandleFieldInvisibleTypeAnnotation)
								fieldNode.invisibleTypeAnnotations.removeIf(annotation -> asmWorker.workFieldInvisibleTypeAnnotation(classNode, fieldNode, annotation));
							if (fieldNode.visibleAnnotations != null&& shouldHandleFieldVisibleAnnotation)
								fieldNode.visibleAnnotations.removeIf(annotation -> asmWorker.workFieldVisibleAnnotation(classNode, fieldNode, annotation));
							if (fieldNode.visibleTypeAnnotations != null&& shouldHandleFieldVisibleTypeAnnotation)
								fieldNode.visibleTypeAnnotations.removeIf(annotation -> asmWorker.workFieldVisibleTypeAnnotation(classNode, fieldNode, annotation));
						}
					}
				}
				
				if (classNode.interfaces != null&&asmWorker.handles(INTERFACE)) {
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
					if(asmWorker.handles(METHOD))
					{
						classNode.methods.removeIf(methodNode -> asmWorker.workMethod(classNode, methodNode));
					}

					boolean shouldHandleTableSwitchInst = asmWorker.handles(INST_TABLE_SWITCH);
					boolean shouldHandleMultiANewArrayInst = asmWorker.handles(INST_MULTI_NEW_ARRAY);
					boolean shouldHandleLookupSwitchInst = asmWorker.handles(INST_LOOKUP_SWITCH);
					boolean shouldHandleLdcInst = asmWorker.handles(INST_LDC);
					boolean shouldHandleInvokeDynamicInst = asmWorker.handles(INST_INVOKE_DYNAMIC);
					boolean shouldHandleIntInst = asmWorker.handles(INST_INT);
					boolean shouldHandleIincInst = asmWorker.handles(INST_IINC);
					boolean shouldHandleFrameInst = asmWorker.handles(INST_FRAME);
					boolean shouldHandleTypeInst = asmWorker.handles(INST_TYPE);
					boolean shouldHandleLabelInst = asmWorker.handles(INST_LABEL);
					boolean shouldHandleLineInst = asmWorker.handles(INST_LINE_NUMBER);
					boolean shouldHandleFieldInst = asmWorker.handles(INST_FIELD);
					boolean shouldHandleJumpInst = asmWorker.handles(INST_JUMP);
					boolean shouldHandleVarInst = asmWorker.handles(INST_VAR);
					boolean shouldHandleMethodInst = asmWorker.handles(INST_METHOD);
					boolean shouldHandleInsnInst = asmWorker.handles(INST_INSN);
					boolean shouldHandleInstructions = shouldHandleTableSwitchInst
							|| shouldHandleMultiANewArrayInst
							|| shouldHandleLookupSwitchInst
							|| shouldHandleLdcInst
							|| shouldHandleInvokeDynamicInst
							|| shouldHandleIntInst
							|| shouldHandleIincInst
							|| shouldHandleFrameInst
							|| shouldHandleTypeInst
							|| shouldHandleLabelInst
							|| shouldHandleLineInst
							|| shouldHandleFieldInst
							|| shouldHandleJumpInst
							|| shouldHandleVarInst
							|| shouldHandleMethodInst
							|| shouldHandleInsnInst
							;

					boolean shouldHandleMethodAtribute = asmWorker.handles(METHOD_ATTRIBUTE);
					boolean shouldHandleMethodLocalVariable = asmWorker.handles(METHOD_LOCAL_VARIABLE);
					boolean shouldHandleMethodVisibleLocalVariableAnnotation = asmWorker.handles(METHOD_VISIBLE_LOCAL_VARIABLE_ANNOTATION);
					boolean shouldHandleMethodInvisibleLocalVariableAnnotation = asmWorker.handles(METHOD_INVISIBLE_LOCAL_VARIABLE_ANNOTATION);
					boolean shouldHandleMethodParameter = asmWorker.handles(METHOD_PARAMETER);
					boolean shouldHandleMethodInvisibleAnnotation = asmWorker.handles(METHOD_INVISIBLE_ANNOTATION);
					boolean shouldHandleMethodVisibleAnnotation = asmWorker.handles(METHOD_VISIBLE_ANNOTATION);
					boolean shouldHandleMethodInvisibleTypeAnnotation = asmWorker.handles(METHOD_INVISIBLE_TYPE_ANNOTATION);
					boolean shouldHandleMethodVisibleTypeAnnotation = asmWorker.handles(METHOD_VISIBLE_TYPE_ANNOTATION);
					boolean shouldHandleMethodTryCatchBlock = asmWorker.handles(METHOD_TRY_CATCH_BLOCK);
					boolean shouldHandleMethodInvisibleParameterAnnotation = asmWorker.handles(METHOD_INVISIBLE_PARAMETER_ANNOTATION);
					boolean shouldHandleMethodVisibleParameterAnnotation = asmWorker.handles(METHOD_VISIBLE_PARAMETER_ANNOTATION);

					boolean shouldHandleReturn = asmWorker.handles(METHOD_RET);
					boolean shouldHandleArgs = asmWorker.handles(METHOD_ARGS);
					boolean shouldHandleMethods = shouldHandleInstructions
							||shouldHandleMethodAtribute
							||shouldHandleMethodLocalVariable
							||shouldHandleMethodVisibleLocalVariableAnnotation
							||shouldHandleMethodInvisibleLocalVariableAnnotation
							||shouldHandleMethodParameter
							||shouldHandleMethodInvisibleAnnotation
							||shouldHandleMethodVisibleAnnotation
							||shouldHandleMethodInvisibleTypeAnnotation
							||shouldHandleMethodVisibleTypeAnnotation
							||shouldHandleMethodTryCatchBlock
							||shouldHandleMethodInvisibleParameterAnnotation
							||shouldHandleMethodVisibleParameterAnnotation
							||shouldHandleReturn
							||shouldHandleArgs
							;

					if(shouldHandleMethods)
					{
						for (MethodNode methodNode : classNode.methods)
						{
							if (methodNode.attrs != null && shouldHandleMethodAtribute)
								methodNode.attrs.removeIf(attribute -> asmWorker.workMethodAttribute(classNode, methodNode, attribute));
							if (methodNode.localVariables != null && shouldHandleMethodLocalVariable)
								methodNode.localVariables.removeIf(localVariableNode -> asmWorker.workMethodLocalVariable(classNode, methodNode, localVariableNode));
							if (methodNode.visibleLocalVariableAnnotations != null && shouldHandleMethodVisibleLocalVariableAnnotation)
								methodNode.visibleLocalVariableAnnotations.removeIf(localVariableAnnotationNode -> asmWorker.workMethodVisibleLocalVariableAnnotation(classNode, methodNode, localVariableAnnotationNode));
							if (methodNode.invisibleLocalVariableAnnotations != null && shouldHandleMethodInvisibleLocalVariableAnnotation)
								methodNode.invisibleLocalVariableAnnotations.removeIf(localVariableAnnotationNode -> asmWorker.workMethodInvisibleLocalVariableAnnotation(classNode, methodNode, localVariableAnnotationNode));
							if (methodNode.parameters != null && shouldHandleMethodParameter)
								methodNode.parameters.removeIf(parameterNode -> asmWorker.workMethodParameter(classNode, methodNode, parameterNode));
							if (methodNode.invisibleAnnotations != null && shouldHandleMethodInvisibleAnnotation)
								methodNode.invisibleAnnotations.removeIf(annotationNode -> asmWorker.workMethodInvisibleAnnotation(classNode, methodNode, annotationNode));
							if (methodNode.visibleAnnotations != null && shouldHandleMethodVisibleAnnotation)
								methodNode.visibleAnnotations.removeIf(annotationNode -> asmWorker.workMethodVisibleAnnotation(classNode, methodNode, annotationNode));
							if (methodNode.invisibleTypeAnnotations != null && shouldHandleMethodInvisibleTypeAnnotation)
								methodNode.invisibleTypeAnnotations.removeIf(typeAnnotationNode -> asmWorker.workMethodInvisibleTypeAnnotation(classNode, methodNode, typeAnnotationNode));
							if (methodNode.visibleTypeAnnotations != null && shouldHandleMethodVisibleTypeAnnotation)
								methodNode.visibleTypeAnnotations.removeIf(typeAnnotationNode -> asmWorker.workMethodVisibleTypeAnnotation(classNode, methodNode, typeAnnotationNode));
							if (methodNode.tryCatchBlocks != null && shouldHandleMethodTryCatchBlock)
								methodNode.tryCatchBlocks.removeIf(tryCatchBlockNode -> asmWorker.workMethodTryCatchBlock(classNode, methodNode, tryCatchBlockNode));
							if (shouldHandleMethodInvisibleParameterAnnotation)
								for (List<AnnotationNode> listAnnotationNode : Opt.it(methodNode.invisibleParameterAnnotations))
								{
									if (listAnnotationNode != null)
										listAnnotationNode.removeIf(annotationNode -> asmWorker.workMethodInvisibleParameterAnnotation(classNode, methodNode, annotationNode));
								}
							if(shouldHandleMethodVisibleParameterAnnotation)
								for (List<AnnotationNode> listAnnotationNode : Opt.it(methodNode.visibleParameterAnnotations))
								{
									if (listAnnotationNode != null)
										listAnnotationNode.removeIf(annotationNode -> asmWorker.workMethodVisibleParameterAnnotation(classNode, methodNode, annotationNode));
								}



							if(shouldHandleArgs || shouldHandleReturn)
							{
								Type methodType = Type.getMethodType(methodNode.desc);
								Type[] argumentTypes = methodType.getArgumentTypes();
								Type returnType = methodType.getReturnType();
								List<Type> argTypes = new ArrayList<>(Arrays.asList(argumentTypes));

								if(shouldHandleArgs)
								{
									if (asmWorker.workMethodArgs(classNode, methodNode, argTypes)) {
										argTypes = new ArrayList<>();
									}
								}

								if(shouldHandleReturn)
								{
									returnType = asmWorker.workMethodRet(classNode, methodNode, returnType);
								}

								methodNode.desc = Type.getMethodDescriptor(returnType, argTypes.toArray(new Type[0]));
							}

							if (methodNode.instructions != null && shouldHandleInstructions) {
								ListIterator<AbstractInsnNode> instructions = methodNode.instructions.iterator();
								while (instructions.hasNext()) {
									AbstractInsnNode abstractInsnNode = instructions.next();
									if (abstractInsnNode instanceof TableSwitchInsnNode && shouldHandleTableSwitchInst) {
										if (asmWorker.workInstNode(classNode, methodNode, (TableSwitchInsnNode) abstractInsnNode))
											methodNode.instructions.remove(abstractInsnNode);
									}
									if (abstractInsnNode instanceof MultiANewArrayInsnNode && shouldHandleMultiANewArrayInst) {
										if (asmWorker.workInstNode(classNode, methodNode, (MultiANewArrayInsnNode) abstractInsnNode))
											methodNode.instructions.remove(abstractInsnNode);
									}
									if (abstractInsnNode instanceof LookupSwitchInsnNode && shouldHandleLookupSwitchInst) {
										if (asmWorker.workInstNode(classNode, methodNode, (LookupSwitchInsnNode) abstractInsnNode))
											methodNode.instructions.remove(abstractInsnNode);
									}
									if (abstractInsnNode instanceof LdcInsnNode && shouldHandleLdcInst) {
										if (asmWorker.workInstNode(classNode, methodNode, (LdcInsnNode) abstractInsnNode))
											methodNode.instructions.remove(abstractInsnNode);
									}
									if (abstractInsnNode instanceof InvokeDynamicInsnNode && shouldHandleInvokeDynamicInst) {
										if (asmWorker.workInstNode(classNode, methodNode, (InvokeDynamicInsnNode) abstractInsnNode))
											methodNode.instructions.remove(abstractInsnNode);
									}
									if (abstractInsnNode instanceof IntInsnNode && shouldHandleIntInst) {
										if (asmWorker.workInstNode(classNode, methodNode, (IntInsnNode) abstractInsnNode))
											methodNode.instructions.remove(abstractInsnNode);
									}
									if (abstractInsnNode instanceof IincInsnNode && shouldHandleIincInst) {
										if (asmWorker.workInstNode(classNode, methodNode, (IincInsnNode) abstractInsnNode))
											methodNode.instructions.remove(abstractInsnNode);
									}
									if (abstractInsnNode instanceof FrameNode && shouldHandleFrameInst) {
										if (asmWorker.workInstNode(classNode, methodNode, (FrameNode) abstractInsnNode))
											methodNode.instructions.remove(abstractInsnNode);
									}
									if (abstractInsnNode instanceof TypeInsnNode && shouldHandleTypeInst) {
										if (asmWorker.workInstNode(classNode, methodNode, (TypeInsnNode) abstractInsnNode))
											methodNode.instructions.remove(abstractInsnNode);
									}
									if (abstractInsnNode instanceof LabelNode && shouldHandleLabelInst) {
										if (asmWorker.workInstNode(classNode, methodNode, (LabelNode) abstractInsnNode))
											methodNode.instructions.remove(abstractInsnNode);
									}
									if (abstractInsnNode instanceof LineNumberNode && shouldHandleLineInst) {
										if (asmWorker.workInstNode(classNode, methodNode, (LineNumberNode) abstractInsnNode))
											methodNode.instructions.remove(abstractInsnNode);
									}
									if (abstractInsnNode instanceof FieldInsnNode && shouldHandleFieldInst) {
										if (asmWorker.workInstNode(classNode, methodNode, (FieldInsnNode) abstractInsnNode))
											methodNode.instructions.remove(abstractInsnNode);
									}
									if (abstractInsnNode instanceof JumpInsnNode && shouldHandleJumpInst) {
										if (asmWorker.workInstNode(classNode, methodNode, (JumpInsnNode) abstractInsnNode))
											methodNode.instructions.remove(abstractInsnNode);
									}
									if (abstractInsnNode instanceof VarInsnNode && shouldHandleVarInst) {
										if (asmWorker.workInstNode(classNode, methodNode, (VarInsnNode) abstractInsnNode))
											methodNode.instructions.remove(abstractInsnNode);
									}
									if (abstractInsnNode instanceof MethodInsnNode && shouldHandleMethodInst) {
										if (asmWorker.workInstNode(classNode, methodNode, (MethodInsnNode) abstractInsnNode))
											methodNode.instructions.remove(abstractInsnNode);
									}
									if (abstractInsnNode instanceof InsnNode && shouldHandleInsnInst) {
										if (asmWorker.workInstNode(classNode, methodNode, (InsnNode) abstractInsnNode))
											methodNode.instructions.remove(abstractInsnNode);
									}
								}
							}
						}
					}


				}
				cng += asmWorker.changes;
				asmWorker.workDataEnd();
			}
			if (cng > 0) {
				if (logger.isDebugEnabled())
					logger.debug(String.format("Trying to make %d changes in %s(%s)", cng, name, transformedName));

				ClassWriter classWriter = new ClassWriter(classReader, withRecalc ? ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES : 0);
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
			throw new RuntimeException(e);
		}
		return basicClass;
	}
}
