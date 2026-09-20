package alexsocol.patcher.handler

import alexsocol.asjlib.eventForge
import cpw.mods.fml.common.eventhandler.*
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.monster.IMob
import net.minecraft.world.GameRules
import net.minecraftforge.event.entity.living.LivingAttackEvent
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent

object GameRulesHandler {
	
	/**
	 * Weather won't change if `false`
	 */
	const val GR_DO_WEATHER_CYCLE = "ASJCore_doWeatherCycle"
	
	/**
	 * Mobs won't fight or damage each other (accidents still can (?) happen) if `true`
	 */
	const val GR_MOBS_FRIENDSHIP = "ASJCore_mobFriendship"
	
	init {
		eventForge()
	}
	
	fun registerAll(to: GameRules) {
		to.addGameRule(GR_DO_WEATHER_CYCLE, true.toString())
		to.addGameRule(GR_MOBS_FRIENDSHIP, false.toString())
	}
	
	/////////////////////
	// Mobs friendship //
	/////////////////////
	
	fun mobsFriendship(one: Entity, other: Entity?): Boolean {
		if (one !is IMob || other !is IMob) return false
		
		val rules = one.worldObj.gameRules
		if (!rules.hasRule(GR_MOBS_FRIENDSHIP))
			rules.addGameRule(GR_MOBS_FRIENDSHIP, false.toString())
		
		return rules.getGameRuleBooleanValue(GR_MOBS_FRIENDSHIP)
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	fun mobsFriendship(e: LivingSetAttackTargetEvent) {
		if (!mobsFriendship(e.entityLiving, e.target ?: return)) return
		
		e.entityLiving.setRevengeTarget(null)
		(e.entityLiving as? EntityLiving)?.attackTarget = null
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	fun mobsFriendship(e: LivingAttackEvent) {
		if (mobsFriendship(e.entityLiving, e.source.entity ?: return))
			e.isCanceled = true
	}
}