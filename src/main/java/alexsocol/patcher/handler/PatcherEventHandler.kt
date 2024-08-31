package alexsocol.patcher.handler

import alexsocol.asjlib.*
import alexsocol.patcher.PatcherConfigHandler
import cpw.mods.fml.common.eventhandler.*
import cpw.mods.fml.common.gameevent.PlayerEvent.*
import cpw.mods.fml.common.gameevent.TickEvent
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent
import io.netty.channel.ChannelOption
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.GuiScreen
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.player.*
import net.minecraft.network.play.server.S1FPacketSetExperience
import net.minecraftforge.event.entity.living.LivingAttackEvent
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent
import net.minecraftforge.event.entity.player.*
import net.minecraftforge.event.world.ExplosionEvent
import net.minecraftforge.oredict.OreDictionary

object PatcherEventHandler {
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	fun livingAttackEventHandler(e: LivingAttackEvent) {
		disableDamageInCreative(e)
		noAttackAfterDeath(e)
	}
	
	fun disableDamageInCreative(e: LivingAttackEvent) {
		if (!PatcherConfigHandler.creativeDamage && (e.entity as? EntityPlayer)?.capabilities?.isCreativeMode == true) e.isCanceled = true
	}
	
	fun noAttackAfterDeath(e: LivingAttackEvent) {
		if (e.source.sourceOfDamage?.isDead == true) e.isCanceled = true
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	fun disableExplosions(e: ExplosionEvent.Start) {
		e.isCanceled = !PatcherConfigHandler.explosions
	}
	
	@SubscribeEvent
	fun fixNaNHealthBug(e: LivingUpdateEvent) {
		val target = e.entityLiving
		
		var max = target.maxHealth
		if (max.isNaN()) max = target.getEntityAttribute(SharedMonsterAttributes.maxHealth).attributeValue.F
		if (max.isNaN()) max = target.getEntityAttribute(SharedMonsterAttributes.maxHealth).baseValue.F
		if (max.isNaN()) max = 20f
		
		if (target.health.isNaN()) target.health = max
		if (target.prevHealth.isNaN()) target.prevHealth = target.health
		if (target.lastDamage.isNaN()) target.lastDamage = 0f
		if (target.absorptionAmount.isNaN()) target.absorptionAmount = 0f
		
		if (target.health > max) target.health = max
		if (target.health < 0f) target.health = 0f
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	fun flyingBreakingSpeed(e: PlayerEvent.BreakSpeed) {
		if (!PatcherConfigHandler.flyFastDig || e.entityPlayer.onGround || !(e.entityPlayer.capabilities.isFlying || e.entityPlayer.isOnLadder)) return
		e.newSpeed *= 5f
	}
	
	@SubscribeEvent
	fun assignFoodStatsHost(e: PlayerTickEvent) {
		if (e.phase != TickEvent.Phase.START) return
		
		e.player.foodStats.ASJCore_host = e.player
	}
	
	@SubscribeEvent
	fun syncXpAndAbsorption(e: PlayerChangedDimensionEvent) {
		val player = e.player as? EntityPlayerMP ?: return
		player.playerNetServerHandler.sendPacket(S1FPacketSetExperience(player.experience, player.experienceTotal, player.experienceLevel))
		player.dataWatcher.setObjectWatched(17)
	}
	
	@SubscribeEvent
	fun setNoTcpDelay(e: PlayerLoggedInEvent) {
		val player = e.player as? EntityPlayerMP ?: return
		player.playerNetServerHandler.netManager.channel().config().setOption(ChannelOption.TCP_NODELAY, true)
	}
}

object PatcherEventHandlerClient {
	
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
	
	@SubscribeEvent
	fun endlessSprint(e: LivingUpdateEvent) {
		if (!PatcherConfigHandler.endlessSprint) return
		val player = e.entity as? EntityPlayerSP ?: return
		player.sprintingTicksLeft = 1200
	}
}