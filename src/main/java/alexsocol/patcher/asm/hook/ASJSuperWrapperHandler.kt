package alexsocol.patcher.asm.hook

import com.KAIIIAK.superwrapper.SuperWrapper
import net.minecraft.block.*
import net.minecraft.entity.*
import net.minecraft.server.MinecraftServer
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
	fun setFlag(entity: Entity, id: Int, flag: Boolean) {
		throw NotImplementedError()
	}
	
	@JvmStatic
	@SuperWrapper(callThis = false)
	fun getFlag(entity: Entity, id: Int): Boolean {
		throw NotImplementedError()
	}
	
	@JvmStatic
	@SuperWrapper
	fun breakBlock(target: BlockContainer, world: World?, x: Int, y: Int, z: Int, block: Block?, meta: Int) {
		throw NotImplementedError()
	}
	
	@JvmStatic
	@SuperWrapper(callThis = false)
	fun isMultiPlayer(server: MinecraftServer): Boolean {
		throw NotImplementedError()
	}
}