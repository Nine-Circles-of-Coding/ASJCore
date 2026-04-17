package com.KAIIIAK.classManipulators;

import com.KAIIIAK.KASMLib.KASMLib;
import com.KAIIIAK.KASMLib.util.KASMUtil;
import com.KAIIIAK.asm.AsmTextParser;
import com.KAIIIAK.classManipulators.Tools.FlexiblePatternReplace;
import com.KAIIIAK.nullsafety.Opt;
import com.KAIIIAK.superwrapper.McpToSrg;
import gloomyfolken.hooklib.asm.HookClassTransformer;
import gloomyfolken.hooklib.asm.HookLogger;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.KAIIIAK.classManipulators.SomeUtil.copyListInstrs;
import static com.KAIIIAK.classManipulators.SomeUtil.getStringRepresentation;

public class HookReplacerWorker implements IClassTransformer {
	
	public static HookLogger logger = new HookLogger.Log4JLogger("HookReplacer");
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (basicClass == null)
			return null;
		
		ClassReader cr = new ClassReader(basicClass);
		Pair<Type, TreeMap<Integer, List<ChangesHolder>>> data = getCorrespondingChangesHolder(name, transformedName, cr);
		if (data == null)
			return basicClass;
		
		TreeMap<Integer, List<ChangesHolder>> registeredChanges = data.getValue();
		
		try {
			if (KASMLib.has2DumpUnchangedClasses) {
				File file = new File("ASJCoreDumpClasses/HookReplacer/" + transformedName.replaceAll("\\.", "/") + "UNCHANGED.class");
				file.getParentFile().mkdirs();
				
				IOUtils.write(basicClass, Files.newOutputStream(file.toPath()));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		logger.debug("Found Class to hook-replace into " + transformedName);
		TreeMap<Integer, List<ChangesHolder>> registeredChangesCopy = deepCopy(registeredChanges);
		
		ClassNode cn = new ClassNode();
		cr.accept(cn, 0);
		
		AtomicInteger changes = new AtomicInteger(0);
		
		for (MethodNode methodNode : Opt.it(cn.methods)) {
			logger.debug(String.format("Testing method %s", methodNode.name));
			processMethod(cn.methods, methodNode, registeredChangesCopy, changes);
		}
		
		boolean crash = false;
		for (List<ChangesHolder> listChangesHolder : Opt.it(registeredChangesCopy.values())) {
			for (ChangesHolder changesHolder : Opt.it(listChangesHolder)) {
				// because I expect to have changesHolder deleted after their full processing, so if isMandatory and still in the list that means that it was not processed...
				if (!changesHolder.isMandatory) continue;
				
				logger.error("Mandatory replacer not injected: " + changesHolder);
				crash = true;
			}
		}
		
		if (crash) throw new RuntimeException("Mandatory replacer(s) not injected, check logs");
		
		if (!HookClassTransformer.skipTransformation) {
			TreeMap<Integer, List<ChangesHolder>> map = notInjectedHookReplacers.get(data.getKey());
			map.clear();
			map.putAll(registeredChangesCopy);
		}
		
		if (changes.get() > 0) {
			logger.debug(String.format("Trying to make %s changes in %s", changes, transformedName));
			
			ClassWriter cw = new ClassWriter(0);
			cn.accept(cw);
			byte[] result = cw.toByteArray();
			
			try {
				if (KASMLib.has2DumpChangedClasses) {
					File file = new File("ASJCoreDumpClasses/HookReplacer/" + transformedName.replaceAll("\\.", "/") + ".class");
					file.getParentFile().mkdirs();
					
					IOUtils.write(result, Files.newOutputStream(file.toPath()));
				}
			} catch (IOException ignored) {}
			
			logger.debug(String.format("Finished replacing for %s", transformedName));
			return result;
		}
		
		return basicClass;
	}
	
	private void processMethod(List<MethodNode> methods, MethodNode method, TreeMap<Integer, List<ChangesHolder>> registeredChanges, AtomicInteger changes) {
		for (List<ChangesHolder> listChangesHolder : registeredChanges.values()) {
			Iterator<ChangesHolder> iterator = listChangesHolder.iterator();
			while (iterator.hasNext()) {
				ChangesHolder changesHolder = iterator.next();
				if (!matchesTarget(methods, method, changesHolder)) continue;
				
				logger.debug(String.format("Trying to apply replacer: %s (for method: %s)", changesHolder, method.name));
				
				if (applyChange(method, changesHolder)) {
					iterator.remove();
					changes.getAndIncrement();
				}
			}
		}
	}
	
	private boolean applyChange(MethodNode method, ChangesHolder ch) {
		if (method.instructions == null)
			throw new RuntimeException(String.format("For some weird reasons %s.%s%s hasn't got instructions at all!", ch.targetClass, ch.targetMethodName, method.desc));
		
		if (ch.from == null)
			throw new RuntimeException(String.format("For some weird reasons %s.%s%s hasn't got FROM instructions at all!", ch.containerClass, ch.containerMethod, method.desc));
		
		if (logger.isTraceEnabled()) {
			for (int i = 0; i < method.instructions.size(); i++)
				logger.trace(String.format("methodNode.instructions.get(%d) = %s", i, getStringRepresentation(method.instructions.get(i))));
			
			for (int i = 0; i < ch.from.size(); i++)
				logger.trace(String.format("fromList.get(%d) = %s", i, getStringRepresentation(ch.from.get(i))));
			
			if (ch.to != null && !ch.to.isEmpty()) {
				for (int i = 0; i < ch.to.size(); i++)
					logger.trace(String.format("toList.get(%d) = %s", i, getStringRepresentation(ch.to.get(i))));
			} else {
				logger.trace("toList is empty!");
			}
		}
		
		List<AbstractInsnNode> instr = SomeUtil.toList(method.instructions);
		FlexiblePatternReplace.ReplaceResult<AbstractInsnNode> result =
				FlexiblePatternReplace.replaceMatches(
						instr,
						ch.from,
						node -> (ch.ignoreLines && node instanceof LineNumberNode) || (ch.ignoreLabels && node instanceof LabelNode), // true - ignore
						SomeUtil::myEquals,
						(node, ctx) -> {},
						null,
						ctx -> copyListInstrs(ch.to),
						ch.matchIndex
				);
		
		
		if (result.matchesReplaced > 0) {
			method.instructions.clear();
			result.result.forEach(method.instructions::add);
			
			logger.debug("Replaced insns at indexes: " + result.getReplacedRangesFormatedString());
			if (!result.successfullyFully) {
				logger.debug(String.format("Replacer (%s) applied NOT fully! (for method: %s)", ch, method.name));
			}
		}
		
		if (result.successfullyFully) {
			logger.debug(String.format("Replacer (%s) applied fully! (for method: %s)", ch, method.name));
		}
		
		return result.successfullyFully;
	}
	
	private boolean matchesTarget(List<MethodNode> methods, MethodNode method, ChangesHolder ch) {
		String actualName = McpToSrg.getTargetMethodMatchingNameAndDesc(methods, ch.targetMethodName, Type.getMethodDescriptor(ch.targetMethodReturn, ch.targetMethodParams));
		return method.name.equals(actualName) && method.desc.equals(Type.getMethodDescriptor(ch.targetMethodReturn, ch.targetMethodParams));
	}
	
	private TreeMap<Integer, List<ChangesHolder>> deepCopy(TreeMap<Integer, List<ChangesHolder>> map) {
		TreeMap<Integer, List<ChangesHolder>> deepCopy = new TreeMap<>();
		
		for (Map.Entry<Integer, List<ChangesHolder>> entry : map.entrySet()) {
			List<ChangesHolder> copiedList = new ArrayList<>();
			for (ChangesHolder holder : entry.getValue()) {
				copiedList.add(holder.copy());
			}
			deepCopy.put(entry.getKey(), copiedList);
		}
		
		return deepCopy;
	}
	
	private Pair<Type, TreeMap<Integer, List<ChangesHolder>>> getCorrespondingChangesHolder(String name, String transformedName, ClassReader basicClass) {
		String[] candidates = {name.replace('.', '/'), transformedName.replace('.', '/'), basicClass.getClassName()};
		for (String internalName : candidates) {
			Type type = Type.getObjectType(internalName);
			TreeMap<Integer, List<ChangesHolder>> changes = registeredChangesHolders.get(type);
			if (changes != null)
				return Pair.of(type, changes);
		}
		return null;
	}
	
	// target class to <priority to all hooks with this priority>
	public static Map<Type, TreeMap<Integer, List<ChangesHolder>>> registeredChangesHolders = new HashMap<>();
	public static Map<Type, TreeMap<Integer, List<ChangesHolder>>> notInjectedHookReplacers = new HashMap<>();
	
	static void registerChangesHolder(ChangesHolder ch) {
		registeredChangesHolders
				.computeIfAbsent(ch.targetClass, type -> new TreeMap<>())
				.computeIfAbsent(ch.priority, ArrayList::new)
				.add(ch);
		
		notInjectedHookReplacers
				.computeIfAbsent(ch.targetClass, type -> new TreeMap<>())
				.computeIfAbsent(ch.priority, ArrayList::new)
				.add(ch);
	}
	
	public static void registerHookReplacerContainer(byte[] clazzBytes, String clazzName) {
		try {
			if (KASMLib.has2DumpUnchangedClasses) {
				File file = new File("ASJCoreDumpClasses/HookReplacerContainer/" + clazzName.replaceAll("\\.", "/") + "UNCHANGED.class");
				file.getParentFile().mkdirs();
				
				IOUtils.write(clazzBytes, Files.newOutputStream(file.toPath()));
			}
		} catch (IOException ignored) {}
		
		clazzBytes = KASMUtil.applyAllPossibleTransformers(clazzName, clazzName, clazzBytes);// because transformers does not apply by default from raw (class.getResource()) class bytes
		
		try {
			if (KASMLib.has2DumpChangedClasses) {
				File file = new File("ASJCoreDumpClasses/HookReplacerContainer/" + clazzName.replaceAll("\\.", "/") + ".class");
				file.getParentFile().mkdirs();
				
				IOUtils.write(clazzBytes, Files.newOutputStream(file.toPath()));
			}
		} catch (IOException ignored) {}
		
		try {
			ClassReader classReader = new ClassReader(clazzBytes);
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			
			for (MethodNode methodNode : Opt.it(classNode.methods)) {
				AnnotationNode hookReplacerAnnotation = getHookReplacerAnnotation(methodNode);
				
				if (hookReplacerAnnotation == null)
					continue;
				
				logger.trace(String.format("Found HookReplacer annotation: %s.%s%s", classNode.name, methodNode.name, methodNode.desc));
				
				if ((methodNode.access & Opcodes.ACC_STATIC) == 0)
					throw new RuntimeException(String.format("HookReplacer method %s.%s%s must be static!", classNode.name, methodNode.name, methodNode.desc));
				
				boolean removePop = false;
				String targetMethodFromAnnotation = null;
				boolean correctStaticIndexes = false;
				int[] matchIndex = {};
				boolean isMandatory = true;
				boolean ignoreLines = true;
				boolean ignoreLabels = true;
				int priority = 0;
				
				if (hookReplacerAnnotation.values != null) {
					Map<String, Object> annotationArgs = SomeUtil.convertListToMap(hookReplacerAnnotation.values);
					if (annotationArgs.containsKey("targetMethod")) {
						targetMethodFromAnnotation = (String) annotationArgs.get("targetMethod");
					}
					if (annotationArgs.containsKey("correctStaticIndexes")) {
						correctStaticIndexes = (boolean) annotationArgs.get("correctStaticIndexes");
					}
					if (annotationArgs.containsKey("isMandatory")) {
						isMandatory = (boolean) annotationArgs.get("isMandatory");
					}
					if (annotationArgs.containsKey("removePop")) {
						removePop = (boolean) annotationArgs.get("removePop");
					}
					if (annotationArgs.containsKey("ignoreLines")) {
						ignoreLines = (boolean) annotationArgs.get("ignoreLines");
					}
					if (annotationArgs.containsKey("ignoreLabels")) {
						ignoreLabels = (boolean) annotationArgs.get("ignoreLabels");
					}
					if (annotationArgs.containsKey("priority")) {
						priority = (int) annotationArgs.get("priority");
					}
					if (annotationArgs.containsKey("matchIndex")) {
						Object val = annotationArgs.get("matchIndex");
						if (val instanceof int[]) {
							matchIndex = (int[]) val;
						} else if (val instanceof List) {
							List<?> listVal = (List<?>) val;
							matchIndex = listVal.stream()
									.mapToInt(o -> ((Number) o).intValue())
									.toArray();
						}
					}
				}
				
				Type methodType = Type.getMethodType(methodNode.desc);
				Type[] argTypes = methodType.getArgumentTypes();
				if (argTypes.length == 0)
					continue;
				
				ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
				
				List<AbstractInsnNode> from = new ArrayList<>();
				List<AbstractInsnNode> to = new ArrayList<>();
				
				boolean isInsideFromBlock = false;
				boolean isInsideToBlock = false;
				
				while (iterator.hasNext()) {
					AbstractInsnNode insnNode = iterator.next();
					if (removePop && (isInsideFromBlock || isInsideToBlock) && insnNode instanceof InsnNode && (insnNode.getOpcode() == Opcodes.POP || insnNode.getOpcode() == Opcodes.POP2))
						continue;
					
					if (ignoreLabels && (isInsideFromBlock || isInsideToBlock) && insnNode instanceof LabelNode)
						continue;
					
					if (ignoreLines && (isInsideFromBlock || isInsideToBlock) && insnNode instanceof LineNumberNode)
						continue;
					
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
				
				if (from.isEmpty()) continue;
				
				String methodName = targetMethodFromAnnotation != null ? targetMethodFromAnnotation : methodNode.name;
				ChangesHolder changesHolder = new ChangesHolder(argTypes[0], methodName);
				
				if (correctStaticIndexes) {
					from = getWithStaticIndexes(from);
					to = getWithStaticIndexes(to);
				}
				
				removeStoreLoad(from);
				removeStoreLoad(to);
				
				changesHolder.from = from;
				changesHolder.to = to;
				
				Type[] methodParams = new Type[argTypes.length - 1];
				System.arraycopy(argTypes, 1, methodParams, 0, methodParams.length);
				changesHolder.targetMethodParams = methodParams;
				
				changesHolder.targetMethodReturn = methodType.getReturnType();
				
				changesHolder.matchIndex = matchIndex;
				changesHolder.isMandatory = isMandatory;
				changesHolder.ignoreLines = ignoreLines;
				changesHolder.ignoreLabels = ignoreLabels;
				changesHolder.priority = priority;
				changesHolder.containerClass = clazzName;
				changesHolder.containerMethod = methodNode.name;
				registerChangesHolder(changesHolder);
				logger.trace(String.format("HookReplacer at %s.%s%s registered!", classNode.name, methodNode.name, methodNode.desc));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static AnnotationNode getHookReplacerAnnotation(MethodNode methodNode) {
		for (AnnotationNode annotationNode : Opt.it(methodNode.invisibleAnnotations)) {
			if ("Lcom/KAIIIAK/classManipulators/HookReplacer;".equals(annotationNode.desc)) {
				return annotationNode;
			}
		}
		for (AnnotationNode annotationNode : Opt.it(methodNode.visibleAnnotations)) {
			if ("Lcom/KAIIIAK/classManipulators/HookReplacer;".equals(annotationNode.desc)) {
				return annotationNode;
			}
		}
		return null;
	}
	
	private static void removePOP(List<AbstractInsnNode> list) {
		if (!list.isEmpty()) {
			list.removeIf(node -> node instanceof MethodInsnNode
					&& node.getOpcode() == Opcodes.INVOKESTATIC
					&& Type.getInternalName(HookReplacer.Replacer.class).equals(((MethodInsnNode) node).owner)
					&& ((MethodInsnNode) node).name.equals("POP"));
		}
	}
	
	private static void removeLines(List<AbstractInsnNode> list) {
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
	
	/**
	 * WARNING! Should be called AFTER removePOP() method here due to
	 * AsmTextParser.OPCODES contains all opcodes including POP but POP should be handled in another way
	 *
	 */
	private static void removeStoreLoad(List<AbstractInsnNode> list) {
		List<AbstractInsnNode> newList = new ArrayList<>();
		boolean shouldRemoveCheckCast = false;
		
		for (AbstractInsnNode node : list) {
			if (shouldRemoveCheckCast) {
				shouldRemoveCheckCast = false;
				if (node instanceof TypeInsnNode && node.getOpcode() == Opcodes.CHECKCAST) continue;
			}
			
			if (!(node instanceof MethodInsnNode)) {
				newList.add(node);
				continue;
			}
			
			MethodInsnNode mnode = ((MethodInsnNode) node);
			
			if (node.getOpcode() != Opcodes.INVOKESTATIC ||
					!Type.getInternalName(HookReplacer.Replacer.class).equals(mnode.owner) ||
					!AsmTextParser.OPCODES.containsKey(mnode.name)) {
				newList.add(mnode);
				continue;
			}
			
			LdcInsnNode varIndex = (LdcInsnNode) newList.remove(newList.size() - 1);
			int var = Integer.parseInt(varIndex.cst.toString());
			
			newList.add(new VarInsnNode(AsmTextParser.OPCODES.get(mnode.name), var));
			if (mnode.name.equals("ALOAD")) shouldRemoveCheckCast = true;
		}
		
		list.clear();
		list.addAll(newList);
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
	
	public static void registerHookReplacerContainer(String clazz) {
		String resource = '/' + clazz.replace('.', '/') + ".class";
		try (InputStream resourceStream = HookReplacerWorker.class.getResourceAsStream(resource)) {
			registerHookReplacerContainer(IOUtils.toByteArray(resourceStream), clazz);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
