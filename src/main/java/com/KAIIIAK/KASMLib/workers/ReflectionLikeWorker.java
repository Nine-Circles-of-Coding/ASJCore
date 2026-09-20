package com.KAIIIAK.KASMLib.workers;

import alexsocol.patcher.PatcherPreConfigHandler;
import com.KAIIIAK.KASMLib.KASMWorker;
import com.KAIIIAK.KASMLib.util.KASMUtil;
import com.KAIIIAK.KASMLib.util.ReflectionLikeUtil;
import com.KAIIIAK.nullsafety.Opt;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class ReflectionLikeWorker extends KASMWorker {
	
	public static KASMWorker inst = KASMUtil.inst();
	
	public String utilClassName = ReflectionLikeUtil.class.getName().replace(".", "/");

	public boolean ignoreClass;
	
	@Override
	public void workDataStart() {
	
	}
	
	@Override
	public void workDataEnd() {
		changes = 0;
		ignoreClass = false;
	}
	
	@Override
	public boolean workClass(ClassNode classNode) {
		for (String ignClassName : Opt.it(PatcherPreConfigHandler.INSTANCE.getIgnoredClasses())) {
			if ((className.contains(ignClassName) || transformedClassName.contains(ignClassName))) {
				ignoreClass = true;
				break;
			}
		}
		
		return false;
	}
	
	public boolean workClassVisibleAnnotation(ClassNode classNode, AnnotationNode annotationNode) {
		if (annotationNode.desc.contains("ScalaSignature")) ignoreClass = true;
		return false;
	}

	public boolean workClassInvisibleAnnotation(ClassNode classNode, AnnotationNode annotationNode) {
		if (annotationNode.desc.contains("ScalaSignature")) ignoreClass = true;
		return false;
	}

	public boolean workField(ClassNode classNode, FieldNode fieldNode) {
		if(ignoreClass) return false;

		if ("__OBFID".equals(fieldNode.name)) {
			return false;
		}
		
		for (FieldNode fieldN : Opt.it(classNode.fields)) {
			// lightloader WTF
			if ("__TARGET".equals(fieldN.name)) {
				return false;
			}
		}
		
		for (AnnotationNode annotationNode : Opt.it(classNode.visibleAnnotations)) {
			if (annotationNode.desc.contains("GameRegistry$ObjectHolder")) return false;
		}
		
		if (PatcherPreConfigHandler.INSTANCE.getAllPublic()) {
			int access = fieldNode.access;
			
			fieldNode.access |= ACC_PUBLIC;
			fieldNode.access &= ~ACC_PRIVATE;
			fieldNode.access &= ~ACC_PROTECTED;
			
			if (access != fieldNode.access) changes++;
		}
		
		return false;
	}

	public boolean workMethod(ClassNode classNode, MethodNode methodNode) {
		if(ignoreClass) return false;
		
		if (PatcherPreConfigHandler.INSTANCE.getAllPublic()) {
			int access = methodNode.access;
			
			methodNode.access |= ACC_PUBLIC;
			methodNode.access &= ~ACC_PRIVATE;
			methodNode.access &= ~ACC_PROTECTED;
			
			if (access != methodNode.access) changes++;
		}
		
		return false;
	}

	@Override
	public boolean workInstNode(ClassNode classNode, MethodNode methodNode, MethodInsnNode insnNode) {
		if (!insnNode.owner.equals(utilClassName)) return false;
		if (changeMethodInsnToFieldInsn(methodNode, insnNode, "GETFIELD", GETFIELD)) return true;
		if (changeMethodInsnToFieldInsn(methodNode, insnNode, "GETSTATICFIELD", GETSTATIC)) return true;
		if (changeMethodInsnToFieldInsn(methodNode, insnNode, "SETFIELD", PUTFIELD)) return true;
		return changeMethodInsnToFieldInsn(methodNode, insnNode, "SETSTATICFIELD", PUTSTATIC);
	}
	
	boolean changeMethodInsnToFieldInsn(MethodNode methodNode, MethodInsnNode insnNode, String methodMame, int opcode) {
		if (insnNode.name.equals(methodMame)) {
			AbstractInsnNode previousNode = insnNode.getPrevious();
			if (previousNode instanceof LdcInsnNode) {
				LdcInsnNode fieldStringDataNode = (LdcInsnNode) previousNode;
				if (fieldStringDataNode.cst instanceof String) {
					String[] fieldData = ((String) fieldStringDataNode.cst).split("\\.");
					
					if (fieldData.length == 3) {
						String fieldOwner = fieldData[0];
						String fieldName = fieldData[1];
						String fieldDesc = fieldData[2];
						FieldInsnNode fieldInsnNode = new FieldInsnNode(opcode, fieldOwner, fieldName, fieldDesc);
						methodNode.instructions.insertBefore(fieldStringDataNode, fieldInsnNode);
						methodNode.instructions.remove(fieldStringDataNode);
						changes++;
						return true;
					}
				}
			}
		}
		return false;
	}
}
