package alexsocol.patcher.asm.hook

import com.KAIIIAK.superwrapper.*
import com.google.common.collect.*
import net.minecraft.block.*
import net.minecraft.client.renderer.EntityRenderer
import net.minecraft.entity.*
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemTool
import net.minecraft.server.*
import net.minecraft.world.*

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
	@SuperWrapper
	fun getItemAttributeModifiers(item: ItemTool): Multimap<String, AttributeModifier> {
		throw NotImplementedError()
	}
	
	@JvmStatic
	@SuperWrapper(targetClass = "lotr.common.item.LOTRItemSword")
	fun getItemAttributeModifiers(item: Any): Multimap<String, AttributeModifier> {
		throw NotImplementedError()
	}

	@JvmStatic
	@SuperWrapper(targetClass = "minefantasy.mf2.item.weapon.ItemWeaponMF")
	fun getAttributeModifiers(item: Any, stack: ItemStack): Multimap<String, AttributeModifier> {
		throw NotImplementedError()
	}
	
	@JvmStatic
	@SuperWrapper
	fun getMouseOver(er: EntityRenderer, partialTick: Float) {
		throw NotImplementedError()
	}
}