package alexsocol.patcher.asm.hook

import alexsocol.asjlib.*
import cpw.mods.fml.relauncher.*
import gloomyfolken.hooklib.asm.*
import net.minecraft.client.renderer.EntityRenderer
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.world.World

@Suppress("unused")
object NoEntityInteractionHandler {
	
	var hookEntities = false
	var noInteract = false
		set(value) {
			field = value
			ASJUtilities.say(mc.thePlayer, "asjcore.noEntityInteract.$field")
		}
	
	@JvmStatic
	@Hook(targetMethod = "getMouseOver")
	@SideOnly(Side.CLIENT)
	fun getMouseOverPre(er: EntityRenderer, ticks: Float) {
		if (noInteract) hookEntities = true
	}
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL)
	@SideOnly(Side.CLIENT)
	fun getEntitiesWithinAABBExcludingEntity(world: World, from: Entity?, aabb: AxisAlignedBB?) = if (hookEntities) ArrayList<Entity>() else null
	
	@JvmStatic
	@Hook(targetMethod = "getMouseOver", injectOnExit = true)
	@SideOnly(Side.CLIENT)
	fun getMouseOverPost(er: EntityRenderer, ticks: Float) {
		hookEntities = false
	}
}