package alexsocol.patcher.asm.hook

import com.KAIIIAK.superwrapper.SuperWrapper
import cpw.mods.fml.relauncher.*
import net.minecraft.block.Block
import net.minecraft.block.BlockContainer
import net.minecraft.entity.*
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack
import net.minecraft.world.World

@Suppress("unused", "UNUSED_PARAMETER")
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
	
	@JvmStatic
	@SuperWrapper(callThis = false)
	fun setFlag(entity: Entity, id: Int, flag: Boolean) {
		throw NotImplementedError()
	}
	
	@JvmStatic
	@SuperWrapper(callThis = false)
	fun getFlag(entity: Entity, id: Int): Boolean {
		throw NotImplementedError()
	}
	
	@SideOnly(Side.CLIENT)
	@JvmStatic
	@SuperWrapper(callThis = false)
	fun equals(thiz: Any, other: Any): Boolean {
		throw NotImplementedError()
	}
	
	@JvmStatic
	@SuperWrapper
	fun breakBlock(target: BlockContainer, world: World?, x: Int, y: Int, z: Int, block: Block?, meta: Int) {
		throw NotImplementedError()
	}
}