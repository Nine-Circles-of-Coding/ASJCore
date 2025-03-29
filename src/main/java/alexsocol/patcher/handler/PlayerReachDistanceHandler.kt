package alexsocol.patcher.handler

import alexsocol.patcher.PatcherMain
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraft.entity.ai.attributes.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing

object PlayerReachDistanceHandler {
	
	val reachDistance: IAttribute = RangedAttribute("${PatcherMain.MODID}.reachDistance", 5.0, Double.MIN_VALUE, 256.0).setDescription("Reach Distance").setShouldWatch(true)
	
	@SubscribeEvent
	fun entityConstruct(e: EntityConstructing) {
		(e.entity as? EntityPlayer)?.getAttributeMap()?.registerAttribute(reachDistance)
	}
}