package alexsocol.patcher.asm.transformer

import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.*

class OptiFinePostTransformer: ASJAbstractClassTransformer() {
	
	override fun transform(transformedName: String, basicClass: ByteArray): ByteArray {
		return when (transformedName) {
			"net.minecraft.client.renderer.RenderBlocks" -> tree { cn ->
				fun patch(mn: MethodNode?) {
					mn ?: return
					
					val iList = mutableListOf<MethodInsnNode>()
					
					for (i in mn.instructions) {
						if (i !is MethodInsnNode || i.name != "func_150098_a") continue
						iList += i
					}
					
					iList.forEach { i ->
						val pp = i.previous.previous
						val pppp = pp.previous.previous
						
						val north = pp is InsnNode && pp.opcode == ISUB
						val south = pp is InsnNode && pp.opcode == IADD
						val west = pppp is InsnNode && pppp.opcode == ISUB
						val east = pppp is InsnNode && pppp.opcode == IADD
						
						val dir = if (north) "NORTH" else if (south) "SOUTH" else if (west) "WEST" else if (east) "EAST" else return@forEach
						
						mn.instructions.insertBefore(i, InsnNode(POP))
						mn.instructions.insertBefore(i, VarInsnNode(ALOAD, 0))
						mn.instructions.insertBefore(i, FieldInsnNode(GETFIELD, "net/minecraft/client/renderer/RenderBlocks", "field_147845_a", "Lnet/minecraft/world/IBlockAccess;"))
						mn.instructions.insertBefore(i, VarInsnNode(ILOAD, 2))
						
						if (west || east) {
							mn.instructions.insertBefore(i, InsnNode(ICONST_1))
							mn.instructions.insertBefore(i, InsnNode(if (west) ISUB else IADD))
						}
						
						mn.instructions.insertBefore(i, VarInsnNode(ILOAD, 3))
						mn.instructions.insertBefore(i, VarInsnNode(ILOAD, 4))
						
						if (north || south) {
							mn.instructions.insertBefore(i, InsnNode(ICONST_1))
							mn.instructions.insertBefore(i, InsnNode(if (north) ISUB else IADD))
						}
						
						mn.instructions.insertBefore(i, FieldInsnNode(GETSTATIC, "net/minecraftforge/common/util/ForgeDirection", dir, "Lnet/minecraftforge/common/util/ForgeDirection;"))
						
						i.name = "canPaneConnectTo"
						i.desc = "(Lnet/minecraft/world/IBlockAccess;IIILnet/minecraftforge/common/util/ForgeDirection;)Z"
					}
				}
				
				val renderBlockPane = cn.methods.firstOrNull { it.name == "func_147767_a" }
				patch(renderBlockPane)
				
				val renderBlockStainedGlassPane = cn.methods.firstOrNull { it.name == "func_147733_k" }
				patch(renderBlockStainedGlassPane)
			}
			
			else                                         -> this.basicClass
		}
	}
}
