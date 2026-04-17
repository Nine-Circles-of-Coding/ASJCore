package alexsocol.patcher.handler

import alexsocol.asjlib.ASJUtilities
import alexsocol.patcher.PatcherConfigHandler
import cpw.mods.fml.common.eventhandler.*
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.oredict.OreDictionary

object PatcherEventHandlerClient {
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	fun onItemTooltip(e: ItemTooltipEvent) {
		if (!GuiScreen.isShiftKeyDown()) return
		
		val stack = e.itemStack
		
		if (PatcherConfigHandler.showRegName) {
			e.toolTip.add("")
			e.toolTip.add(GameRegistry.findUniqueIdentifierFor(stack.item).toString())
		}
		
		if (PatcherConfigHandler.showOreDict) run {
			val ids = OreDictionary.getOreIDs(e.itemStack)
			if (ids.isEmpty()) return@run
			
			e.toolTip.add("")
			e.toolTip.add("OreDict:")
			for (id in ids)
				e.toolTip.add(OreDictionary.getOreName(id))
		}
		
		if (PatcherConfigHandler.showNbt && stack.hasTagCompound() && e.showAdvancedItemTooltips) {
			e.toolTip.add("")
			e.toolTip.add("NBT:")
			e.toolTip.addAll(listOf(*ASJUtilities.toString(stack.tagCompound).split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
		}
	}
	
	@SubscribeEvent
	fun endlessSprint(e: LivingUpdateEvent) {
		if (!PatcherConfigHandler.endlessSprint) return
		val player = e.entity as? EntityPlayerSP ?: return
		player.sprintingTicksLeft = 1200
	}
}