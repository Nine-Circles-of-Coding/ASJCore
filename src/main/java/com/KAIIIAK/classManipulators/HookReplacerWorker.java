package com.KAIIIAK.classManipulators;

import com.KAIIIAK.KASMLib.KASMWorker;
import com.KAIIIAK.KASMLib.util.KASMUtil;
import com.KAIIIAK.nullsafety.Opt;
import com.KAIIIAK.superwrapper.McpToSrg;
import com.google.common.collect.ImmutableMap;
import gloomyfolken.hooklib.asm.HookLogger;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static com.KAIIIAK.classManipulators.SomeUtil.getStringRepresentation;
import static org.objectweb.asm.Opcodes.*;

public class HookReplacerWorker extends KASMWorker {
	
	public static HookLogger logger = new HookLogger.Log4JLogger("HookReplacer");
	
	public static KASMWorker inst = KASMUtil.inst();
	
	public List<ChangesHolder> methodsToChange = new ArrayList<>();
	
	{
		withRecalc = true;
	}
	
	@Override
	public void workDataStart() {
		super.workDataStart();
		methodsToChange.clear();
	}
	
	@Override
	public boolean workClass(ClassNode classNode) {
		for (ChangesHolder changesHolder : Opt.it(registeredMethods)) {
			if (classNode.name.equals(changesHolder.clazz.getInternalName()) || className.equals(changesHolder.clazz.getInternalName()) || transformedClassName.equals(changesHolder.clazz.getClassName())) {
				logger.debug("Found Class to hook into " + transformedClassName);
				methodsToChange.add(changesHolder);
			}
		}
		
		return false;
	}
	
	@Override
	public boolean workMethod(ClassNode classNode, MethodNode methodNode) {
		List<ChangesHolder> found = new ArrayList<>();
		
		for (ChangesHolder changesHolder : Opt.it(methodsToChange)) {
			String actualName = McpToSrg.getTargetMethodMatchingNameAndDesc(classNode.methods, changesHolder.methodName, Type.getMethodDescriptor(changesHolder.methodReturn, changesHolder.methodParams));
			
			logger.debug(String.format("Testing method %s to be equal to %s", methodNode.name, actualName));
			
			if (methodNode.name.equals(actualName)) {
				logger.debug(String.format("Found method to hook into %s", methodNode.name));
				Type methodType = Type.getMethodType(methodNode.desc);
				if (!methodType.getReturnType().equals(changesHolder.methodReturn)) continue;
				logger.debug(String.format("Found method to hook into with a correct return type %s", methodNode.name));
				Type[] argTypes = methodType.getArgumentTypes();
				if (!isTypesSame(argTypes, changesHolder.methodParams)) continue;
				logger.debug(String.format("Found method to hook into with a correct method params %s", methodNode.name));
				workMethodChanges(methodNode, changesHolder);
				
				found.add(changesHolder);
			}
		}
		
		if (!found.isEmpty())
			methodsToChange.removeAll(found);
		
		return false;
	}
	
	public boolean isTypesSame(Type[] types1, Type[] types2) {
		if (types1.length != types2.length) return false;
		
		for (int i = 0; i < types1.length; i++) {
			if (!types1[i].equals(types2[i])) return false;
		}
		
		return true;
	}
	
	public static List<AbstractInsnNode> getWithStaticIndexes(List<AbstractInsnNode> list) {
		List<AbstractInsnNode> buff = new ArrayList<>();
		for (AbstractInsnNode node : Opt.it(list)) {
			if (!(node instanceof VarInsnNode) || node.getOpcode() == Opcodes.RET) {
				buff.add(node);
				continue;
			}
			
			VarInsnNode varNode = (VarInsnNode) node;
			buff.add(new VarInsnNode(varNode.getOpcode(), varNode.var - 1));
		}
		
		return buff;
	}
	
	//TODO check start
	public void workMethodChanges(MethodNode methodNode, ChangesHolder changes) {
		for (int i = 0; i < methodNode.instructions.size(); i++)
			logger.trace(String.format("methodNode.instructions.get(%d) = %s", i, getStringRepresentation(methodNode.instructions.get(i))));
		
		for (Map.Entry<List<AbstractInsnNode>, List<AbstractInsnNode>> entry : Opt.it(((methodNode.access & Opcodes.ACC_STATIC) != 0) && !changes.correctStaticIndexes ? changes.instToReplaceForStaticSrc.entrySet() : changes.instToReplace.entrySet())) {
			List<AbstractInsnNode> fromList = entry.getKey();
			List<AbstractInsnNode> toList = entry.getValue();
			
			for (int i = 0; i < fromList.size(); i++)
				logger.trace(String.format("fromList.get(%d) = %s", i, getStringRepresentation(fromList.get(i))));
			
			for (int i = 0; i < toList.size(); i++)
				logger.trace(String.format("toList.get(%d) = %s", i, getStringRepresentation(toList.get(i))));
			
			AbstractInsnNode[] fromArray = fromList.toArray(new AbstractInsnNode[0]);
			
			InsnList instructions = methodNode.instructions;
			int index = findInstructions(instructions, fromArray, 0);
			
			while (index >= 0) {
				removeInstructions(instructions, index, fromArray.length);
				
				InsnList toList2 = new InsnList();
				
				for (AbstractInsnNode abstractInsnNode : Opt.it(toList)) {
					toList2.add(SomeUtil.copyInsnNode(abstractInsnNode));
				}
				
				instructions.insertBefore(instructions.get(index), toList2);
				
				logger.debug(String.format("Replaced insns at index %s", index));
				
				this.changes++;
				index = findInstructions(instructions, fromArray, index + 1);
			}
		}
	}
	
	private int findInstructions(InsnList instructions, AbstractInsnNode[] fromArray, int startFromIndex) {
		if (fromArray == null || fromArray.length == 0 || instructions == null || instructions.size() == 0) return -1;
		
		AbstractInsnNode[] mainInstructions = instructions.toArray();
		AbstractInsnNode first = fromArray[0];
		
		List<Integer> all = findAll(mainInstructions, first);
		markHere:
		for (Integer firstIndex : Opt.it(all)) {
			if (firstIndex < startFromIndex) continue;
			
			if (firstIndex + fromArray.length - 1 > (mainInstructions.length - 1)) continue;
			
			for (int i = 0; i < fromArray.length; i++) {
				if (!SomeUtil.myEquals(mainInstructions[i + firstIndex], fromArray[i])) continue markHere;
			}
			
			return firstIndex;
		}
		return -1;
		
	}
	
	private List<Integer> findAll(AbstractInsnNode[] instructions, AbstractInsnNode first) {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < instructions.length; i++) {
			if (SomeUtil.myEquals(instructions[i], first)) {
				list.add(i);
			}
		}
		return list;
	}
	
	private void removeInstructions(InsnList instructions, int startIndex, int count) {
		for (int i = 0; i < count; i++) {
			instructions.remove(instructions.get(startIndex));
		}
	}
	//TODO check end
	
	public static List<ChangesHolder> registeredMethods = new ArrayList<>();
	
	public static void registerHookReplacerContainer(String clazz) {
		Opt.it(HookReplacerWorker.class.getResourceAsStream('/' + clazz.replace('.', '/') + ".class"), it -> {
			try {
				registerHookReplacerContainer(IOUtils.toByteArray(it));
			} catch (IOException e) {
				logger.error(String.format("Can not parse hooks container %s", clazz), e);
				throw new RuntimeException(e);
			}
		});
	}
	
	public static void registerHookReplacerContainer(byte[] clazzBytes) {
		try {
			ClassReader classReader = new ClassReader(clazzBytes);
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			for (MethodNode methodNode : Opt.it(classNode.methods)) {
				AnnotationNode hookReplacerAnnotation = null;
				for (AnnotationNode annotationNode : Opt.it(methodNode.visibleAnnotations)) {
					if (annotationNode.desc.contains("HookReplacer")) {
						hookReplacerAnnotation = annotationNode;
						break;
					}
				}
				for (AnnotationNode annotationNode : Opt.it(methodNode.invisibleAnnotations)) {
					if (annotationNode.desc.contains("HookReplacer")) {
						hookReplacerAnnotation = annotationNode;
						break;
					}
				}
				
				if (hookReplacerAnnotation == null)
					continue;
				
				String targetMethodFromAnnotation = null;
				boolean correctStaticIndexes = false;
				
				if (hookReplacerAnnotation.values != null) {
					Map<String, Object> annotationArgs = SomeUtil.convertListToMap(hookReplacerAnnotation.values);
					if (annotationArgs.containsKey("targetMethod")) {
						targetMethodFromAnnotation = (String) annotationArgs.get("targetMethod");
					}
					if (annotationArgs.containsKey("correctStaticIndexes")) {
						correctStaticIndexes = (boolean) annotationArgs.get("correctStaticIndexes");
					}
				}
				
				logger.debug(String.format("Found HookReplacer annotation: %s.%s%s", classNode.name, methodNode.name, methodNode.desc));
				
				Type methodType = Type.getMethodType(methodNode.desc);
				Type[] argTypes = methodType.getArgumentTypes();
				if (argTypes.length == 0)
					continue;
				
				ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
				
				ArrayList<AbstractInsnNode> from = new ArrayList<>();
				ArrayList<AbstractInsnNode> to = new ArrayList<>();
				
				boolean isInsideFromBlock = false;
				boolean isInsideToBlock = false;
				
				while (iterator.hasNext()) {
					AbstractInsnNode insnNode = iterator.next();
					if (insnNode instanceof MethodInsnNode
								&& insnNode.getOpcode() == Opcodes.INVOKESTATIC
								&& Type.getInternalName(HookReplacer.Replacer.class).equals(((MethodInsnNode) insnNode).owner)) {
						MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
						switch (methodInsnNode.name) {
							case "startFROM":
								isInsideFromBlock = true;
								isInsideToBlock = false;
								continue;
							case "startTO":
								isInsideFromBlock = false;
								isInsideToBlock = true;
								continue;
							case "stop":
								isInsideFromBlock = false;
								isInsideToBlock = false;
								continue;
						}
					}
					if (isInsideFromBlock) {
						from.add(SomeUtil.copyInsnNode(insnNode));
					}
					
					if (isInsideToBlock) {
						to.add(SomeUtil.copyInsnNode(insnNode));
					}
				}
				
				removePOP(from);
				removePOP(to);
				removeLines(from);
				removeLines(to);
				removeStoreLoad(from);
				removeStoreLoad(to);
				
				if (!from.isEmpty()) {
					String methodName = targetMethodFromAnnotation != null ? targetMethodFromAnnotation : methodNode.name;
					ChangesHolder changesHolder = new ChangesHolder(argTypes[0], methodName);
					changesHolder.instToReplace.put(from, to);
					
					if (!correctStaticIndexes) {
						changesHolder.instToReplaceForStaticSrc.put(getWithStaticIndexes(from), getWithStaticIndexes(to));
						changesHolder.correctStaticIndexes = false;
					}
					
					Type[] methodParams = new Type[argTypes.length - 1];
					System.arraycopy(argTypes, 1, methodParams, 0, methodParams.length);
					changesHolder.methodParams = methodParams;
					
					changesHolder.methodReturn = methodType.getReturnType();
					
					registeredMethods.add(changesHolder);
					
					logger.debug(String.format("HookReplacer at %s.%s%s registered!", classNode.name, methodNode.name, methodNode.desc));
				}
			}
		} catch (Exception e) {
			logger.error("Can not parse hooks container", e);
			throw new RuntimeException(e);
		}
	}
	
	private static void removePOP(List<AbstractInsnNode> list) {
		if (!list.isEmpty()) {
			list.removeIf(node -> node instanceof MethodInsnNode
										  && node.getOpcode() == Opcodes.INVOKESTATIC
										  && Type.getInternalName(HookReplacer.Replacer.class).equals(((MethodInsnNode) node).owner)
										  && ((MethodInsnNode) node).name.equals("POP"));
		}
		
		if (!list.isEmpty()) {
			AbstractInsnNode last = list.get(list.size() - 1);
			if (last instanceof InsnNode && last.getOpcode() == Opcodes.POP) {
				list.remove(last);
			}
		}
	}
	
	private static void removeLines(ArrayList<AbstractInsnNode> list) {
		List<AbstractInsnNode> listToRemove = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			AbstractInsnNode node = list.get(i);
			if (node instanceof MethodInsnNode
						&& node.getOpcode() == Opcodes.INVOKESTATIC
						&& Type.getInternalName(HookReplacer.Replacer.class).equals(((MethodInsnNode) node).owner)
						&& ((MethodInsnNode) node).name.equals("POPLine")) {
				listToRemove.add(node);
				if ((i - 1) >= 0) {
					AbstractInsnNode nodeBefore = list.get(i - 1);
					if (nodeBefore instanceof LineNumberNode || nodeBefore instanceof LabelNode)
						listToRemove.add(nodeBefore);
				}
				if ((i - 2) >= 0) {
					AbstractInsnNode nodeBefore = list.get(i - 2);
					if (nodeBefore instanceof LineNumberNode || nodeBefore instanceof LabelNode)
						listToRemove.add(nodeBefore);
				}
			}
		}
		list.removeAll(listToRemove);
	}
	
	private static void removeStoreLoad(ArrayList<AbstractInsnNode> list) {
		List<AbstractInsnNode> newList = new ArrayList<>();
		
		for (int i = 0; i < list.size(); i++) {
			AbstractInsnNode node = list.get(i);
			if (!(node instanceof MethodInsnNode)) {
				newList.add(node);
				continue;
			}
			
			MethodInsnNode mnode = ((MethodInsnNode) node);
			
			if (node.getOpcode() != Opcodes.INVOKESTATIC ||
						!Type.getInternalName(HookReplacer.Replacer.class).equals(mnode.owner) ||
						!loadStoreInsns.containsKey(mnode.name)) {
				newList.add(mnode);
				continue;
			}
			
			LdcInsnNode varIndex = (LdcInsnNode) newList.remove(newList.size() - 1);
			int var = Integer.parseInt(varIndex.cst.toString());
			
			newList.add(new VarInsnNode(loadStoreInsns.get(mnode.name), var));
			if (mnode.name.equals("ALOAD")) i++;
		}
		
		list.clear();
		list.addAll(newList);
	}
	
	// why Java is so shitty? (c) AlexSocol
	private static ImmutableMap<String, Integer> loadStoreInsns = ImmutableMap.<String, Integer>builder()
																		  .put("ILOAD", ILOAD)
																		  .put("LLOAD", LLOAD)
																		  .put("FLOAD", FLOAD)
																		  .put("DLOAD", DLOAD)
																		  .put("ALOAD", ALOAD)
																		  .put("ISTORE", ISTORE)
																		  .put("LSTORE", LSTORE)
																		  .put("FSTORE", FSTORE)
																		  .put("DSTORE", DSTORE)
																		  .put("ASTORE", ASTORE).build();
}
