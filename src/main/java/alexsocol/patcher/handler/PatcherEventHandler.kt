package alexsocol.patcher.handler

import alexsocol.asjlib.ASJUtilities
import alexsocol.patcher.PatcherConfigHandler
import cpw.mods.fml.common.eventhandler.*
import cpw.mods.fml.relauncher.*
import net.minecraft.client.gui.GuiScreen
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.entity.living.LivingAttackEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.oredict.OreDictionary

object PatcherEventHandler {
	
	@SubscribeEvent
	fun disableDamageInCreative(e: LivingAttackEvent) {
		if (!PatcherConfigHandler.creativeDamage && (e.entityLiving as? EntityPlayer)?.capabilities?.isCreativeMode == true) e.isCanceled = true
	}
}

object PatcherEventHandlerClient {
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	fun onItemTooltip(e: ItemTooltipEvent) {
		if (GuiScreen.isShiftKeyDown()) {
			val stack = e.itemStack
			
			if (PatcherConfigHandler.showOreDict) run {
				val ids = OreDictionary.getOreIDs(e.itemStack)
				if (ids.isEmpty()) return@run
				
				e.toolTip.add("")
				e.toolTip.add("OreDict:")
				for (id in ids)
					e.toolTip.add(OreDictionary.getOreName(id))
			}
			
			if (stack.hasTagCompound() && e.showAdvancedItemTooltips) {
				e.toolTip.add("")
				e.toolTip.add("NBT:")
				e.toolTip.addAll(listOf(*ASJUtilities.toString(stack.tagCompound).split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
			}
		}
	}
}