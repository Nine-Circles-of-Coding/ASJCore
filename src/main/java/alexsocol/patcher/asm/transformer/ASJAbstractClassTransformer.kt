package alexsocol.patcher.asm.transformer

import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.*
import org.objectweb.asm.tree.ClassNode

abstract class ASJAbstractClassTransformer: IClassTransformer {
	
	var transformedName = ""
	var basicClass = byteArrayOf()
	
	override fun transform(name: String?, transformedName: String, basicClass: ByteArray?): ByteArray? {
		if (basicClass == null || basicClass.isEmpty()) return basicClass
		
		this.transformedName = transformedName
		this.basicClass = basicClass
		
		return transform(transformedName, basicClass)
	}
	
	abstract fun transform(transformedName: String, basicClass: ByteArray): ByteArray
	
	protected inline fun core(frames: Int = ClassReader.EXPAND_FRAMES, lambda: (ClassVisitor) -> ClassVisitor): ByteArray {
		println("Transforming $transformedName")
		val cr = ClassReader(basicClass)
		val cw = ClassWriter(ClassWriter.COMPUTE_MAXS)
		val transformer = lambda(cw)
		cr.accept(transformer, frames)
		return cw.toByteArray()
	}
	
	protected inline fun tree(lambda: (ClassNode) -> Unit): ByteArray {
		println("Transforming $transformedName")
		val cr = ClassReader(basicClass)
		val cw = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
		val cn = ClassNode()
		cr.accept(cn, ClassReader.EXPAND_FRAMES)
		
		lambda(cn)
		
		cn.accept(cw)
		return cw.toByteArray()
	}
}