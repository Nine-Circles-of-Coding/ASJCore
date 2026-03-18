package com.KAIIIAK.asm;

import com.KAIIIAK.nullsafety.Opt;
import org.objectweb.asm.*;
import org.objectweb.asm.util.Printer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("UnnecessaryUnicodeEscape")
public final class AsmTextParser {
	
	private static final Map<String, Integer> OPCODES;
	
	static {
		OPCODES = new HashMap<>();
		for (int i = 0; i < Printer.OPCODES.length; i++) {
			OPCODES.put(Printer.OPCODES[i], i);
		}
	}
	
	private final Map<String, Label> labels = new HashMap<>();
	
	private Label label(String name) {
		return labels.computeIfAbsent(name, k -> new Label());
	}
	
	public void visitMulti(MethodVisitor mv, String... lines) {
		for (String line : Opt.it(lines)) {
			visit(mv, line);
		}
	}
	
	public void visit(MethodVisitor mv, String line) {
		if (line == null || line.trim().isEmpty()) return;
		
		String[] t = tokenize(line.trim());
		String op = t[0];
		
		if ("CODE".equals(op)) {
			mv.visitCode();
		} else if ("END".equals(op)) {
			mv.visitEnd();
		} else if ("MAXS".equals(op)) {
			int maxStack = Integer.parseInt(t[1]);
			int maxLocals = Integer.parseInt(t[2]);
			mv.visitMaxs(maxStack, maxLocals);
		} else if ("LABEL".equals(op)) {
			mv.visitLabel(label(t[1]));
		} else if ("LINE".equals(op)) {
			mv.visitLineNumber(Integer.parseInt(t[1]), label(t[2]));
		} else if ("FRAME".equals(op)) {
			parseFrame(mv, t);
		} else if ("LDC".equals(op)) {
			mv.visitLdcInsn(parseLdc(t[1]));
		} else if ("TABLESWITCH".equals(op)) {
			parseTableSwitch(mv, t);
		} else if ("LOOKUPSWITCH".equals(op)) {
			parseLookupSwitch(mv, t);
		} else if ("TRYCATCH".equals(op)) {
			mv.visitTryCatchBlock(
					label(t[1]),
					label(t[2]),
					label(t[3]),
					"null".equals(t[4]) ? null : t[4]
			);
		} else {
			parseOpcode(mv, t);
		}
	}
	
	private void parseOpcode(MethodVisitor mv, String[] t) {
		Integer opcode = OPCODES.get(t[0]);
		if (opcode == null) {
			throw new IllegalArgumentException("Unknown opcode: " + t[0]);
		}
		
		switch (opcode) {
			// ZERO OPERAND
			case Opcodes.NOP:
			case Opcodes.ACONST_NULL:
			case Opcodes.DUP:
			case Opcodes.DUP2:
			case Opcodes.POP:
			case Opcodes.RETURN:
			case Opcodes.IRETURN:
			case Opcodes.ARETURN:
			case Opcodes.DRETURN:
			case Opcodes.LRETURN:
			case Opcodes.FRETURN:
				mv.visitInsn(opcode);
				break;
			
			// INT INSN
			case Opcodes.BIPUSH:
			case Opcodes.SIPUSH:
			case Opcodes.NEWARRAY:
				mv.visitIntInsn(opcode, Integer.parseInt(t[1]));
				break;
			
			// VAR
			case Opcodes.ALOAD:
			case Opcodes.ILOAD:
			case Opcodes.FLOAD:
			case Opcodes.DLOAD:
			case Opcodes.LLOAD:
			case Opcodes.ASTORE:
			case Opcodes.ISTORE:
			case Opcodes.FSTORE:
			case Opcodes.DSTORE:
			case Opcodes.LSTORE:
				mv.visitVarInsn(opcode, Integer.parseInt(t[1]));
				break;
			
			// TYPE
			case Opcodes.NEW:
			case Opcodes.ANEWARRAY:
			case Opcodes.CHECKCAST:
			case Opcodes.INSTANCEOF:
				mv.visitTypeInsn(opcode, t[1]);
				break;
			
			// FIELD
			case Opcodes.GETFIELD:
			case Opcodes.PUTFIELD:
			case Opcodes.GETSTATIC:
			case Opcodes.PUTSTATIC:
				mv.visitFieldInsn(opcode, t[1], t[2], t[3]);
				break;
			
			// METHOD
			case Opcodes.INVOKEVIRTUAL:
			case Opcodes.INVOKESTATIC:
			case Opcodes.INVOKESPECIAL:
			case Opcodes.INVOKEINTERFACE:
				mv.visitMethodInsn(opcode, t[1], t[2], t[3], opcode == Opcodes.INVOKEINTERFACE);
				break;
			
			// INVOKEDYNAMIC
			case Opcodes.INVOKEDYNAMIC:
				parseInvokeDynamic(mv, t);
				break;
			
			// JUMP
			case Opcodes.GOTO:
			case Opcodes.IFNULL:
			case Opcodes.IFNONNULL:
			case Opcodes.IFEQ:
			case Opcodes.IFNE:
			case Opcodes.IFLT:
			case Opcodes.IFLE:
			case Opcodes.IFGT:
			case Opcodes.IFGE:
				mv.visitJumpInsn(opcode, label(t[1]));
				break;
			
			// IINC
			case Opcodes.IINC:
				mv.visitIincInsn(Integer.parseInt(t[1]), Integer.parseInt(t[2]));
				break;
			
			// MULTIANEWARRAY
			case Opcodes.MULTIANEWARRAY:
				mv.visitMultiANewArrayInsn(t[1], Integer.parseInt(t[2]));
				break;
			
			default:
				mv.visitInsn(opcode);
		}
	}
	
	private void parseTableSwitch(MethodVisitor mv, String[] t) {
		int min = Integer.parseInt(t[1]);
		int max = Integer.parseInt(t[2]);
		Label dflt = label(t[3]);
		Label[] arr = new Label[max - min + 1];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = label(t[4 + i]);
		}
		mv.visitTableSwitchInsn(min, max, dflt, arr);
	}
	
	private void parseLookupSwitch(MethodVisitor mv, String[] t) {
		Label dflt = label(t[1]);
		int pairs = (t.length - 2) / 2;
		int[] keys = new int[pairs];
		Label[] labelsArr = new Label[pairs];
		for (int i = 0; i < pairs; i++) {
			keys[i] = Integer.parseInt(t[2 + i * 2]);
			labelsArr[i] = label(t[3 + i * 2]);
		}
		mv.visitLookupSwitchInsn(dflt, keys, labelsArr);
	}
	
	private void parseInvokeDynamic(MethodVisitor mv, String[] t) {
		String name = t[1];
		String desc = t[2];
		Handle bsm = parseHandle(t[3]);
		// ASM 5 не поддерживает bootstrap args в конструкторе Handle напрямую
		mv.visitInvokeDynamicInsn(name, desc, bsm);
	}
	
	private Handle parseHandle(String s) {
		// формат: H(tag owner name desc)
		if (!s.startsWith("H(") || !s.endsWith(")")) {
			throw new IllegalArgumentException("Invalid handle: " + s);
		}
		String inner = s.substring(2, s.length() - 1).trim();
		String[] parts = inner.split("\\s+");
		if (parts.length < 4) throw new IllegalArgumentException("Invalid handle parts: " + s);
		int tag = Integer.parseInt(parts[0]);
		String owner = parts[1];
		String name = parts[2];
		String desc = parts[3];
		return new Handle(tag, owner, name, desc); // ASM 5
	}
	
	private void parseFrame(MethodVisitor mv, String[] t) {
		int type;
		if ("FULL".equals(t[1])) type = Opcodes.F_FULL;
		else if ("APPEND".equals(t[1])) type = Opcodes.F_APPEND;
		else if ("CHOP".equals(t[1])) type = Opcodes.F_CHOP;
		else if ("SAME".equals(t[1])) type = Opcodes.F_SAME;
		else if ("SAME1".equals(t[1])) type = Opcodes.F_SAME1;
		else throw new IllegalArgumentException("Unknown frame type: " + t[1]);
		mv.visitFrame(type, 0, null, 0, null);
	}
	
	private Object parseLdc(String s) {
		if (s.startsWith("\"") && s.endsWith("\"")) return s.substring(1, s.length() - 1);
		if (s.endsWith("L")) return Long.parseLong(s.substring(0, s.length() - 1));
		if (s.endsWith("f")) return Float.parseFloat(s.substring(0, s.length() - 1));
		if (s.endsWith("d")) return Double.parseDouble(s.substring(0, s.length() - 1));
		if (s.matches("-?\\d+")) return Integer.parseInt(s);
		if (s.startsWith("Type(") && s.endsWith(")")) return Type.getType(s.substring(5, s.length() - 1));
		throw new IllegalArgumentException("Unsupported LDC: " + s);
	}
	
	private String[] tokenize(String line) {
		List<String> tokens = new ArrayList<>();
		Matcher m = Pattern.compile("\"[^\"]*\"|\\S+").matcher(line);
		while (m.find()) tokens.add(m.group());
		return tokens.toArray(new String[0]);
	}
}