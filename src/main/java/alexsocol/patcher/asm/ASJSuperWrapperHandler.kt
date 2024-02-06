package alexsocol.patcher.asm

import com.KAIIIAK.superwrapper.SuperWrapper
import net.minecraft.entity.EntityCreature
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack

@Suppress("unused")
object ASJSuperWrapperHandler {
	
	@JvmStatic
	@SuperWrapper
	fun getCanSpawnHere(entity: EntityCreature): Boolean {
		throw NotImplementedError()
	}
	
	@JvmStatic
	@SuperWrapper(callThis = false)
	fun mergeItemStack(container: Container, stack: ItemStack?, wtfI1: Int, wtfI2: Int, wtfFlag: Boolean): Boolean {
		throw NotImplementedError()
	}
}