package alexsocol.asjlib.asm

import gloomyfolken.hooklib.asm.HookLogger.SystemOutLogger
import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.*
import org.objectweb.asm.tree.ClassNode

class ASJASM: IClassTransformer {
	
	override fun transform(name: String, transformedName: String, basicClass: ByteArray?): ByteArray? {
		if (basicClass == null || basicClass.isEmpty()) return basicClass
		val fields = fieldsMap[transformedName] ?: return basicClass
		
		logger.debug("[ASJASM] Injecting hook fields into class $transformedName")
		
		val cr = ClassReader(basicClass)
		val cw = ClassWriter(cr, ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
		for (fd in fields) {
			logger.debug("[ASJASM] Injecting field $name")
			cw.visitField(fd.access, fd.name, fd.desc, null, null).visitEnd()
		}
		cr.accept(cw, 0)
		return cw.toByteArray()
	}
	
	companion object {
		
		var logger = SystemOutLogger()
		
		val fieldsMap = HashMap<String, ArrayList<FieldData>>()
		
		fun registerFieldHookContainer(className: String) {
			try {
				logger.debug("[ASJASM] Parsing field hooks container $className")
				parseFieldHookContainer(ASJASM::class.java.getResourceAsStream("/${className.replace('.', '/')}.class")?.readBytes() ?: throw NullPointerException("Can't read data from ${className}.class"))
			} catch (e: Exception) {
				System.err.println("[ASJASM] Can not parse field hooks container $className")
				e.printStackTrace()
			}
		}
		
		private fun parseFieldHookContainer(basicClass: ByteArray) {
			val cr = ClassReader(basicClass)
			val cn = ClassNode()
			cr.accept(cn, 0)
			for (fn in cn.fields) {
				var flag = false
				var targetClassName = ""
				if (fn.visibleAnnotations != null && fn.visibleAnnotations.isNotEmpty()) {
					for (an in fn.visibleAnnotations) {
						if (an.desc == Type.getDescriptor(HookField::class.java)) {
							flag = true
							if (an.values != null && an.values.isNotEmpty()) {
								var i = 0
								while (i < an.values.size) {
									if (an.values[i] == "targetClassName") {
										targetClassName = an.values[i + 1].toString()
										break
									}
									i += 2
								}
							}
						}
						if (flag && targetClassName.isNotEmpty()) break
					}
				}
				
				if (!flag || targetClassName.isEmpty()) continue
				
				fieldsMap.computeIfAbsent(targetClassName) { ArrayList() } += FieldData(fn.access, fn.name, fn.desc)
			}
		}
	}
}

data class FieldData(val access: Int, val name: String, val desc: String)