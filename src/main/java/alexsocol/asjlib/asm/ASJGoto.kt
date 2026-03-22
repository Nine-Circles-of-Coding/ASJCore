package alexsocol.asjlib.asm

import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.*
import org.objectweb.asm.tree.*

/**
 * Позволяет создавать метки наподобие `goto` для перехода назад по списку инструкций.
 */
class ASJGoto: IClassTransformer {
	
	override fun transform(name: String, transformedName: String, basicClass: ByteArray?): ByteArray? {
		if (basicClass == null || basicClass.isEmpty()) return basicClass
		
		val cr = ClassReader(basicClass)
		val cn = ClassNode()
		cr.accept(cn, 0)
		
		var applied = false
		
		cn.methods.forEach { mn ->
			loop@ for (iN in mn.instructions) {
				if (iN !is MethodInsnNode) continue
				
				if (iN.opcode != Opcodes.INVOKESTATIC || iN.name != "createGoto" || "ASJGoto" !in iN.owner) continue
				
				val lName = (iN.previous as? LdcInsnNode)?.cst as? String ?: continue
				
				var label = iN.previous
				
				while (label !is LabelNode) {
					label = label.previous
					if (label == null) continue@loop
				}
				
				applied = true
				
				mn.instructions.remove(iN.previous)
				mn.instructions.remove(iN)
				
				for (iN1 in mn.instructions) {
					if (iN1 !is MethodInsnNode) continue
					
					if (iN1.opcode != Opcodes.INVOKESTATIC || iN1.name != "invokeGoto" || "ASJGoto" !in iN1.owner) continue
					val lTarget = (iN1.previous as? LdcInsnNode)?.cst as? String ?: continue
					if (lTarget != lName) continue
					
					mn.instructions.insert(iN1, JumpInsnNode(Opcodes.GOTO, label))
					
					mn.instructions.remove(iN1.previous)
					mn.instructions.remove(iN1)
				}
			}
		}
		
		if (!applied) return basicClass

		val cw = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
		cn.accept(cw)
		return cw.toByteArray()
	}
	
	companion object {
		
		/**
		 * Создаёт метку для использования [invokeGoto].
		 *
		 * Пример: `createGoto("myLabel")`
		 *
		 * @param lName уникальный литерал имени метки
		 */
		@JvmStatic
		fun createGoto(lName: String) {
			throw RuntimeException("Error injecting label $lName")
		}
		
		/**
		 * Переходит на метку, созданную через [createGoto].
		 *
		 * Пример: `invokeGoto("myLabel")`
		 *
		 * @param lName литерал имени метки
		 */
		@JvmStatic
		fun invokeGoto(lName: String) {
			throw RuntimeException("Error going to label $lName")
		}
	}
}