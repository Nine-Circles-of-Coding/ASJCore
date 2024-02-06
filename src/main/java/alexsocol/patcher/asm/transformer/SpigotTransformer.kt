package alexsocol.patcher.asm.transformer

import org.objectweb.asm.tree.*

// fuck you
class SpigotTransformer: ASJAbstractClassTransformer() {
	
	var spigotShitDetected = false
	
	override fun transform(transformedName: String, basicClass: ByteArray): ByteArray {
		return when (transformedName) {
			"net.minecraft.entity.DataWatcher" -> {
				tree { cn ->
					val fn = cn.fields.find { it.name == "c" || it.name == "field_75697_a" }
					spigotShitDetected = fn?.desc == "Ljava/util/Map;" == true
				}
				
				println("DataWatcher parsed, fucking Spigot shit ${
					if (spigotShitDetected) "" else "not "
				}detected.${
					if (spigotShitDetected) "" else " Thank God!"
				}")
				
				basicClass
			}
			
			"alexsocol.patcher.helper.FuckingSpigotFix" -> {
				if (spigotShitDetected) {
					println("Applying fix for fucking Spigot shit")
					
					tree { cn ->
						val mn = cn.methods.first { it.name == "getDataWatcher_dataTypes" }
						val getstatic = object: Iterable<AbstractInsnNode> {
							override fun iterator() = mn.instructions.iterator()
						}.find {
							it is FieldInsnNode && it.name == "field_75697_a"
						} as? FieldInsnNode ?: return@tree
						
						getstatic.desc = "Ljava/util/Map;"
					}
				} else
					basicClass
			}
			
			else -> basicClass
		}
	}
}

