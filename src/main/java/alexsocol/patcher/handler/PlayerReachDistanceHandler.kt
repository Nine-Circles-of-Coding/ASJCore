package alexsocol.patcher.handler

import alexsocol.patcher.PatcherMain
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraft.entity.ai.attributes.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing
import java.util.*

object PlayerReachDistanceHandler {
	
	val reachDistance: IAttribute = RangedAttribute("${PatcherMain.MODID}.reachDistance", Double.MIN_VALUE, Double.MIN_VALUE, 256.0).setDescription("Reach Distance").setShouldWatch(true)
	
	val basicModifierUUID = UUID.fromString("7bfa3696-d42e-4c94-9718-0f007b52ef66")!!
	fun basicModifier(value: Double) = AttributeModifier(basicModifierUUID, "Basic Value", value, 0)
	
	@SubscribeEvent
	fun entityConstruct(e: EntityConstructing) {
		val player = e.entity as? EntityPlayer ?: return
		
		player.getAttributeMap().registerAttribute(reachDistance)
		player.getEntityAttribute(reachDistance).applyModifier(basicModifier(5.0))
	}
	
	@JvmStatic
	fun getReachDistance(player: EntityPlayer?) = player?.getEntityAttribute(reachDistance)?.attributeValue ?: 5.0
	
	@JvmStatic
	fun setReachDistance(player: EntityPlayer, distance: Double) {
		val attribute = player.getEntityAttribute(reachDistance)
		val current = attribute.attributeValue
		val basicMod = attribute.getModifier(basicModifierUUID)
		attribute.removeModifier(basicMod)
		attribute.applyModifier(basicModifier(distance - (current - basicMod.amount)))
	}
}