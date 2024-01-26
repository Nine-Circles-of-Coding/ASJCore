package alexsocol.patcher.asm

import com.KAIIIAK.superwrapper.SuperWrapper
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.passive.EntityAnimal

@Suppress("unused")
object ASJSuperWrapperHandler {
	
	@JvmStatic
	@SuperWrapper
	fun getCanSpawnHere(entity: EntityCreature): Boolean {
		throw NotImplementedError()
	}
}