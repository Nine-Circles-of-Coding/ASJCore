package alexsocol.patcher.asm.hook

import alexsocol.asjlib.*
import cpw.mods.fml.relauncher.*
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
	
	// Called from alexsocol.mixins.client.MixinEntityRenderer / MixinWorldEntitySelection.
	
	@JvmStatic
	@SideOnly(Side.CLIENT)
	fun getMouseOverPre(er: EntityRenderer, ticks: Float) {
		if (noInteract) hookEntities = true
	}
	
	@JvmStatic
	@SideOnly(Side.CLIENT)
	fun getEntitiesWithinAABBExcludingEntity(world: World, from: Entity?, aabb: AxisAlignedBB?) = if (hookEntities) ArrayList<Entity>() else null
	
	@JvmStatic
	@SideOnly(Side.CLIENT)
	fun getMouseOverPost(er: EntityRenderer, ticks: Float) {
		hookEntities = false
	}
}