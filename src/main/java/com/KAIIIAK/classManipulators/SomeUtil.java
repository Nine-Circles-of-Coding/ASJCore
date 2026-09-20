package com.KAIIIAK.classManipulators;

import com.KAIIIAK.KASMLib.KASMLib;
import com.KAIIIAK.classManipulators.Tools.FlexiblePatternReplace;
import com.KAIIIAK.classManipulators.fakeNode.AnyNode;
import com.KAIIIAK.classManipulators.fakeNode.CaptureNode;
import com.KAIIIAK.nullsafety.Opt;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.objectweb.asm.Opcodes.INVOKESPECIAL;

@SuppressWarnings("unused")
public class SomeUtil {
	
	public static AbstractInsnNode copyInsnNode(AbstractInsnNode original) {
		if (original == null) {
			return null;
		}
		AbstractInsnNode copy;
		
		if (original instanceof TableSwitchInsnNode) {
			TableSwitchInsnNode tableSwitchInsn = (TableSwitchInsnNode) original;
			copy = new TableSwitchInsnNode(tableSwitchInsn.min, tableSwitchInsn.max, tableSwitchInsn.dflt, tableSwitchInsn.labels.toArray(new LabelNode[0]));
		} else if (original instanceof MultiANewArrayInsnNode) {
			MultiANewArrayInsnNode multiANewArrayInsn = (MultiANewArrayInsnNode) original;
			copy = new MultiANewArrayInsnNode(multiANewArrayInsn.desc, multiANewArrayInsn.dims);
		} else if (original instanceof LookupSwitchInsnNode) {
			LookupSwitchInsnNode lookupSwitchInsn = (LookupSwitchInsnNode) original;
			copy = new LookupSwitchInsnNode(lookupSwitchInsn.dflt, convertToIntArray(lookupSwitchInsn.keys), lookupSwitchInsn.labels.toArray(new LabelNode[0]));
		} else if (original instanceof LdcInsnNode) {
			LdcInsnNode ldcInsn = (LdcInsnNode) original;
			copy = new LdcInsnNode(ldcInsn.cst);
		} else if (original instanceof InvokeDynamicInsnNode) {
			InvokeDynamicInsnNode invokeDynamicInsn = (InvokeDynamicInsnNode) original;
			copy = new InvokeDynamicInsnNode(invokeDynamicInsn.name, invokeDynamicInsn.desc, invokeDynamicInsn.bsm, invokeDynamicInsn.bsmArgs);
		} else if (original instanceof IntInsnNode) {
			IntInsnNode intInsn = (IntInsnNode) original;
			copy = new IntInsnNode(intInsn.getOpcode(), intInsn.operand);
		} else if (original instanceof IincInsnNode) {
			IincInsnNode iincInsn = (IincInsnNode) original;
			copy = new IincInsnNode(iincInsn.var, iincInsn.incr);
		} else if (original instanceof FrameNode) {
			FrameNode frameInsn = (FrameNode) original;
			if (frameInsn.local != null && frameInsn.stack != null) {
				copy = new FrameNode(frameInsn.type, frameInsn.local.size(), frameInsn.local.toArray(), frameInsn.stack.size(), frameInsn.stack.toArray());
			} else if (frameInsn.local != null) {
				copy = new FrameNode(frameInsn.type, frameInsn.local.size(), frameInsn.local.toArray(), 0, null);
			} else if (frameInsn.stack != null) {
				copy = new FrameNode(frameInsn.type, 0, null, frameInsn.stack.size(), frameInsn.stack.toArray());
			} else copy = original.clone(null);
		} else if (original instanceof TypeInsnNode) {
			TypeInsnNode typeInsn = (TypeInsnNode) original;
			copy = new TypeInsnNode(typeInsn.getOpcode(), typeInsn.desc);
		} else if (original instanceof LabelNode) {
			copy = new LabelNode(((LabelNode) original).getLabel());
		} else if (original instanceof LineNumberNode) {
			LineNumberNode lineNumberInsn = (LineNumberNode) original;
			copy = new LineNumberNode(lineNumberInsn.line, lineNumberInsn.start);
		} else if (original instanceof FieldInsnNode) {
			FieldInsnNode fieldInsn = (FieldInsnNode) original;
			copy = new FieldInsnNode(fieldInsn.getOpcode(), fieldInsn.owner, fieldInsn.name, fieldInsn.desc);
		} else if (original instanceof JumpInsnNode) {
			JumpInsnNode jumpInsn = (JumpInsnNode) original;
			copy = new JumpInsnNode(jumpInsn.getOpcode(), jumpInsn.label);
		} else if (original instanceof VarInsnNode) {
			VarInsnNode varInsn = (VarInsnNode) original;
			copy = new VarInsnNode(varInsn.getOpcode(), varInsn.var);
		} else if (original instanceof MethodInsnNode) {
			MethodInsnNode methodInsn = (MethodInsnNode) original;
			copy = new MethodInsnNode(methodInsn.getOpcode(), methodInsn.owner, methodInsn.name, methodInsn.desc, methodInsn.itf);
		} else if (original instanceof InsnNode) {
			copy = new InsnNode(original.getOpcode());
		} else {
			copy = original.clone(null);
		}
		
		return copy;
	}
	
	public static int[] convertToIntArray(List<Integer> list) {
		int[] array = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			array[i] = list.get(i);
		}
		return array;
	}
	
	public static String getStringRepresentation(AbstractInsnNode insnNode) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(insnNode.getClass().getSimpleName()).append(": ");
		stringBuilder.append("opcode=").append(getOpcodeName(insnNode.getOpcode())).append(", ");
		Field[] fields = insnNode.getClass().getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			try {
				Object value = field.get(insnNode);
				stringBuilder.append(field.getName()).append("=").append(value).append(", ");
			} catch (IllegalAccessException e) {
				KASMLib.logger.error("Error:", e);
			}
		}
		
		// Remove the trailing comma and space
		if (stringBuilder.length() >= 2) {
			stringBuilder.setLength(stringBuilder.length() - 2);
		}
		
		return stringBuilder.toString();
	}
	
	private static final Map<Integer, String> opcodeNames = new HashMap<>();
	
	static {
		try {
			for (Field field : Opcodes.class.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers()) && field.getType() == int.class) {
					int fieldValue = field.getInt(null);
					String fieldName = field.getName();
					opcodeNames.put(fieldValue, fieldName);
				}
			}
		} catch (IllegalAccessException e) {
			KASMLib.logger.error("Error:", e);
		}
	}
	
	public static String getOpcodeName(int opcode) {
		String opcodeName = opcodeNames.get(opcode);
		return opcodeName != null ? opcodeName : "Unknown";
	}


	public static void cleverCaptureSource(AbstractInsnNode value, FlexiblePatternReplace.MatchContext<AbstractInsnNode> context)
	{
		AbstractInsnNode patternValue =
				context.matchedPattern.get(context.matchedPattern.size() - 1);
		if(patternValue instanceof CaptureNode)
		{
			context.put("capture" + ((CaptureNode)patternValue).captureIndex, value);
		}
	}

	public static List<AbstractInsnNode> cleverReplacementBuilder(FlexiblePatternReplace.MatchContext<AbstractInsnNode> context, List<AbstractInsnNode> replaceInto)
	{
		List<AbstractInsnNode> result = new ArrayList<>();

		for (AbstractInsnNode value : Opt.it(replaceInto)) {
      		if(value instanceof CaptureNode)
      		{
				int captureIndex = ((CaptureNode)value).captureIndex;
				result.add(context.<AbstractInsnNode>get("capture" + captureIndex));
			} else {
				result.add(value);
			}
		}

		return result;
	}

	public static boolean myEquals(AbstractInsnNode sourceValue, AbstractInsnNode patternValue) {
		if (sourceValue == patternValue) {
			return true;
		}

		if (patternValue == null)
		{
			return false;
		}

		if (patternValue instanceof AnyNode || patternValue instanceof CaptureNode)
		{
			return true;
		}

		if (sourceValue == null) {
			return false;
		}
		
		if (sourceValue.getClass() != patternValue.getClass()) {
			return false;
		}
		
		if (sourceValue instanceof FieldInsnNode) {
			FieldInsnNode firstFieldInsn = (FieldInsnNode) sourceValue;
			FieldInsnNode secondFieldInsn = (FieldInsnNode) patternValue;
			
			return firstFieldInsn.getOpcode() == secondFieldInsn.getOpcode() &&
					Objects.equals(firstFieldInsn.owner, secondFieldInsn.owner) &&
					Objects.equals(firstFieldInsn.name, secondFieldInsn.name) &&
					Objects.equals(firstFieldInsn.desc, secondFieldInsn.desc);
		} else if (sourceValue instanceof FrameNode) {
			FrameNode firstFrameNode = (FrameNode) sourceValue;
			FrameNode secondFrameNode = (FrameNode) patternValue;
			
			// Compare the frame type and local/stack values
			return firstFrameNode.getOpcode() == secondFrameNode.getOpcode() &&
					(firstFrameNode.type != secondFrameNode.type || // Ochen unsafe huinya (c) KAIIIAK
							(Objects.equals(firstFrameNode.local, secondFrameNode.local) && Objects.equals(firstFrameNode.stack, secondFrameNode.stack)));
		} else if (sourceValue instanceof IincInsnNode) {
			IincInsnNode firstIincInsn = (IincInsnNode) sourceValue;
			IincInsnNode secondIincInsn = (IincInsnNode) patternValue;
			
			return firstIincInsn.var == secondIincInsn.var &&
					firstIincInsn.incr == secondIincInsn.incr;
		} else if (sourceValue instanceof IntInsnNode) {
			IntInsnNode firstIntInsn = (IntInsnNode) sourceValue;
			IntInsnNode secondIntInsn = (IntInsnNode) patternValue;
			
			return firstIntInsn.getOpcode() == secondIntInsn.getOpcode() &&
					firstIntInsn.operand == secondIntInsn.operand;
		} else if (sourceValue instanceof InvokeDynamicInsnNode) {
			InvokeDynamicInsnNode firstInvokeDynamicInsn = (InvokeDynamicInsnNode) sourceValue;
			InvokeDynamicInsnNode secondInvokeDynamicInsn = (InvokeDynamicInsnNode) patternValue;
			
			return Objects.equals(firstInvokeDynamicInsn.name, secondInvokeDynamicInsn.name) &&
					Objects.equals(firstInvokeDynamicInsn.desc, secondInvokeDynamicInsn.desc) &&
					Objects.equals(firstInvokeDynamicInsn.bsm, secondInvokeDynamicInsn.bsm) &&
					Arrays.equals(firstInvokeDynamicInsn.bsmArgs, secondInvokeDynamicInsn.bsmArgs);
		} else if (sourceValue instanceof LdcInsnNode) {
			LdcInsnNode firstLdcInsn = (LdcInsnNode) sourceValue;
			LdcInsnNode secondLdcInsn = (LdcInsnNode) patternValue;
			
			return Objects.equals(firstLdcInsn.cst, secondLdcInsn.cst);
		} else if (sourceValue instanceof LineNumberNode) {
			// For LineNumberNode, compare the opcode
			return sourceValue.getOpcode() == patternValue.getOpcode();
		} else if (sourceValue instanceof LookupSwitchInsnNode) {
			LookupSwitchInsnNode firstLookupSwitchInsn = (LookupSwitchInsnNode) sourceValue;
			LookupSwitchInsnNode secondLookupSwitchInsn = (LookupSwitchInsnNode) patternValue;
			
			return Objects.equals(firstLookupSwitchInsn.dflt, secondLookupSwitchInsn.dflt) &&
					Objects.equals(firstLookupSwitchInsn.keys, secondLookupSwitchInsn.keys) &&
					Objects.equals(firstLookupSwitchInsn.labels, secondLookupSwitchInsn.labels);
		} else if (sourceValue instanceof MultiANewArrayInsnNode) {
			MultiANewArrayInsnNode firstMultiANewArrayInsn = (MultiANewArrayInsnNode) sourceValue;
			MultiANewArrayInsnNode secondMultiANewArrayInsn = (MultiANewArrayInsnNode) patternValue;
			
			return Objects.equals(firstMultiANewArrayInsn.desc, secondMultiANewArrayInsn.desc) &&
					firstMultiANewArrayInsn.dims == secondMultiANewArrayInsn.dims;
		} else if (sourceValue instanceof TableSwitchInsnNode) {
			TableSwitchInsnNode firstTableSwitchInsn = (TableSwitchInsnNode) sourceValue;
			TableSwitchInsnNode secondTableSwitchInsn = (TableSwitchInsnNode) patternValue;
			
			return Objects.equals(firstTableSwitchInsn.dflt, secondTableSwitchInsn.dflt) &&
					firstTableSwitchInsn.min == secondTableSwitchInsn.min &&
					firstTableSwitchInsn.max == secondTableSwitchInsn.max &&
					Objects.equals(firstTableSwitchInsn.labels, secondTableSwitchInsn.labels);
		} else if (sourceValue instanceof TypeInsnNode) {
			TypeInsnNode firstTypeInsn = (TypeInsnNode) sourceValue;
			TypeInsnNode secondTypeInsn = (TypeInsnNode) patternValue;
			
			return firstTypeInsn.getOpcode() == secondTypeInsn.getOpcode() &&
					Objects.equals(firstTypeInsn.desc, secondTypeInsn.desc);
		} else if (sourceValue instanceof VarInsnNode) {
			VarInsnNode firstVarInsn = (VarInsnNode) sourceValue;
			VarInsnNode secondVarInsn = (VarInsnNode) patternValue;
			
			return firstVarInsn.getOpcode() == secondVarInsn.getOpcode() &&
					firstVarInsn.var == secondVarInsn.var;
		} else if (sourceValue instanceof MethodInsnNode) {
			MethodInsnNode firstMethodInsn = (MethodInsnNode) sourceValue;
			MethodInsnNode secondMethodInsn = (MethodInsnNode) patternValue;
			
			return (firstMethodInsn.getOpcode() == secondMethodInsn.getOpcode() ||
					firstMethodInsn.getOpcode() == INVOKESPECIAL ||
					secondMethodInsn.getOpcode() == INVOKESPECIAL) &&
					Objects.equals(firstMethodInsn.owner, secondMethodInsn.owner) &&
					Objects.equals(firstMethodInsn.name, secondMethodInsn.name) &&
					Objects.equals(firstMethodInsn.desc, secondMethodInsn.desc) &&
					firstMethodInsn.itf == secondMethodInsn.itf;
		} else if (sourceValue instanceof JumpInsnNode) {
			// For JumpInsnNode subclasses, compare the opcode
			return sourceValue.getOpcode() == patternValue.getOpcode();
		} else if (sourceValue instanceof InsnNode) {
			// For InsnNode subclasses, compare the opcode
			return sourceValue.getOpcode() == patternValue.getOpcode();
		} else if (sourceValue instanceof LabelNode) {
			// For LabelNode, compare the opcode
			return sourceValue.getOpcode() == patternValue.getOpcode();
		}
		
		// For unknown AbstractInsnNode types, return false
		return false;
	}
	
	public static Map<String, Object> convertListToMap(List<Object> list) {
		return IntStream.range(0, list.size() / 2).boxed().collect(Collectors.toMap(i -> (String) list.get(i * 2), i -> list.get(i * 2 + 1)));
	}
	
	public static List<AbstractInsnNode> toList(InsnList insnList) {
		List<AbstractInsnNode> ret = new ArrayList<>();
		if (insnList == null || insnList.size() == 0) return ret;
		for (AbstractInsnNode node = insnList.getFirst(); node != null; node = node.getNext()){
			ret.add(node);
		}
		return ret;
	}
	
	public static List<AbstractInsnNode> toListCopy(InsnList insnList) {
		return copyListInstrs(toList(insnList));
	}
	
	public static List<AbstractInsnNode> copyListInstrs(List<AbstractInsnNode> list) {
		List<AbstractInsnNode> ret = new ArrayList<>();
		for (AbstractInsnNode node : Opt.it(list)) {
			ret.add(copyInsnNode(node));
		}
		return ret;
	}
	
	public static InsnList toInsnList(List<AbstractInsnNode> list) {
		InsnList ret = new InsnList();
		for (AbstractInsnNode node : Opt.it(list)) {
			ret.add(node);
		}
		return ret;
	}
	
}
