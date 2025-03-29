package alexsocol.patcher.asm.transformer

import gloomyfolken.hooklib.asm.HookLogger
import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.*
import org.objectweb.asm.tree.ClassNode

abstract class ASJAbstractClassTransformer: IClassTransformer {
	
	protected var transformedName = ""
	protected var basicClass = byteArrayOf()
	protected open val logger = HookLogger.Log4JLogger("ASJCore")
	
	final override fun transform(name: String?, transformedName: String, basicClass: ByteArray?): ByteArray? {
		if (basicClass == null || basicClass.isEmpty()) return basicClass
		
		this.transformedName = transformedName
		this.basicClass = basicClass
		
		return transform(transformedName, basicClass)
	}
	
	abstract fun transform(transformedName: String, basicClass: ByteArray): ByteArray
	
	protected inline fun core(acceptFlags: Int = ClassReader.EXPAND_FRAMES, cwFlags: Int = ClassWriter.COMPUTE_MAXS, lambda: (ClassVisitor) -> ClassVisitor): ByteArray {
		logger.debug("Transforming $transformedName")
		val cr = ClassReader(basicClass)
		val cw = ClassWriter(cwFlags)
		
		val transformer = lambda(cw)
		
		cr.accept(transformer, acceptFlags)
		return cw.toByteArray()
	}
	
	protected inline fun tree(acceptFlags: Int = ClassReader.EXPAND_FRAMES, cwFlags: Int = ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES, lambda: (ClassNode) -> Unit): ByteArray {
		logger.debug("Transforming $transformedName")
		val cr = ClassReader(basicClass)
		val cw = ClassWriter(cwFlags)
		val cn = ClassNode()
		cr.accept(cn, acceptFlags)
		
		lambda(cn)
		
		cn.accept(cw)
		return cw.toByteArray()
	}
}