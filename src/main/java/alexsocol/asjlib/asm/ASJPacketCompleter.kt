package alexsocol.asjlib.asm

import alexsocol.patcher.asm.transformer.*
import com.google.common.collect.Lists
import org.apache.commons.io.IOUtils
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.*

class ASJPacketCompleter: ASJAbstractClassTransformer() {
	
	override fun transform(transformedName: String, basicClass: ByteArray): ByteArray {
		try {
			val cr = ClassReader(basicClass)
			val cn = ClassNode()
			cr.accept(cn, 0)
			
			if (isASJPacket(mutableListOf(transformedName), basicClass)) {
				logger.debug("Expanding ASJPacket $transformedName")
				val fs = BooleanArray(5) // <init>, fromBytes, toBytes, fromCustomBytes, toCustomBytes
				for (mt in cn.methods) {
					if (mt.name == "<init>" && mt.desc == "()V") {
						fs[0] = true
					} else
					if (mt.name == "fromBytes" && mt.desc == "(Lio/netty/buffer/ByteBuf;)V") {
						fs[1] = true
					} else
					if (mt.name == "toBytes" && mt.desc == "(Lio/netty/buffer/ByteBuf;)V") {
						fs[2] = true
					} else
					if (mt.name == "fromCustomBytes" && mt.desc == "(Lio/netty/buffer/ByteBuf;)V") {
						fs[3] = true
					} else
					if (mt.name == "toCustomBytes" && mt.desc == "(Lio/netty/buffer/ByteBuf;)V") {
						fs[4] = true
					}
				}
				
				if (!fs[0]) makeConstructor(cn)
				if (!fs[1]) makeFromBytes(cn, fs[3])
				if (!fs[2]) makeToBytes(cn, fs[4])
				
				val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
				cn.accept(cw)
				
				return cw.toByteArray()
			}
		} catch (e: Throwable) {
			logger.error("Something went wrong while transforming class $transformedName. Ignore if everything is OK (this is NOT ASJCore error):", e)
			
			return basicClass
		}
		
		return basicClass
	}
	
	private fun isASJPacket(classNames: MutableList<String>, classBytes: ByteArray): Boolean {
		try {
			val classReader = ClassReader(classBytes)
			val classNode = ClassNode()
			
			classReader.accept(classNode, 0)
			if (classNode.superName == "alexsocol/asjlib/network/ASJPacket") return true
			
			val superClassName = classNode.superName
			if (superClassName != null) {
				classNames.add(superClassName)
				return isASJPacket(classNames, getClassData(superClassName))
			}
		} catch (ignore: Throwable) {}
		
		return false
	}
	
	private fun getClassData(className: String): ByteArray {
		val classResourceName = "/${className.replace('.', '/')}.class"
		val stream = ASJPacketCompleter::class.java.getResourceAsStream(classResourceName) ?: throw NullPointerException("Cannot read class `$className` (Path: `$classResourceName`)")
		return IOUtils.toByteArray(stream)
	}
	
	private fun makeConstructor(cl: ClassNode) {
		val mv = cl.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)
		mv.visitCode()
		mv.visitVarInsn(ALOAD, 0)
		mv.visitMethodInsn(INVOKESPECIAL, cl.superName, "<init>", "()V", false)
		mv.visitInsn(RETURN)
		mv.visitEnd()
	}
	
	private fun makeFromBytes(cl: ClassNode, callCustom: Boolean) {
		val mv = cl.visitMethod(ACC_PUBLIC, "fromBytes", "(Lio/netty/buffer/ByteBuf;)V", null, null)
		mv.visitCode()
		if (callCustom) {
			mv.visitVarInsn(ALOAD, 0)
			mv.visitVarInsn(ALOAD, 1)
			mv.visitMethodInsn(INVOKEVIRTUAL, cl.name, "fromCustomBytes", "(Lio/netty/buffer/ByteBuf;)V", false)
		}
		for (fn in getFileds(cl)) {
			if (!descriptors.contains(fn.desc)) continue
			mv.visitVarInsn(ALOAD, 0)
			mv.visitVarInsn(ALOAD, 1)
			mv.visitMethodInsn(INVOKESTATIC, "alexsocol/asjlib/network/ASJPacket", "read" + fn.desc.replace("/".toRegex(), "").replace(";".toRegex(), ""), "(Lio/netty/buffer/ByteBuf;)" + fn.desc, false)
			mv.visitFieldInsn(PUTFIELD, cl.name, fn.name, fn.desc)
		}
		mv.visitInsn(RETURN)
		mv.visitEnd()
	}
	
	private fun makeToBytes(cl: ClassNode, callCustom: Boolean) {
		val mv = cl.visitMethod(ACC_PUBLIC, "toBytes", "(Lio/netty/buffer/ByteBuf;)V", null, null)
		mv.visitCode()
		if (callCustom) {
			mv.visitVarInsn(ALOAD, 0)
			mv.visitVarInsn(ALOAD, 1)
			mv.visitMethodInsn(INVOKEVIRTUAL, cl.name, "toCustomBytes", "(Lio/netty/buffer/ByteBuf;)V", false)
		}
		for (fn in getFileds(cl)) {
			if (!descriptors.contains(fn.desc)) continue
			mv.visitVarInsn(ALOAD, 1)
			mv.visitVarInsn(ALOAD, 0)
			mv.visitFieldInsn(GETFIELD, cl.name, fn.name, fn.desc)
			mv.visitMethodInsn(INVOKESTATIC, "alexsocol/asjlib/network/ASJPacket", "write", "(Lio/netty/buffer/ByteBuf;" + fn.desc + ")V", false)
		}
		mv.visitInsn(RETURN)
		mv.visitEnd()
	}
	
	private fun getFileds(cl: ClassNode): List<FieldNode> {
		val fns = ArrayList<FieldNode>()
		for (fn in cl.fields) {
			if (fn.access and ACC_STATIC > 0 || fn.access and ACC_FINAL > 0) continue
			fns.add(fn)
		}
		return fns
	}
	
	companion object {
		
		val descriptors: List<String> = Lists.newArrayList("Z", "B", "C", "D", "F", "I", "J", "S", "Ljava/lang/String;", "Lnet/minecraft/item/ItemStack;", "Lnet/minecraft/nbt/NBTTagCompound;")
	}
}