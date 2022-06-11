package alexsocol.asjlib.security

import alexsocol.asjlib.mfloor
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.util.DamageSource
import net.minecraft.world.World
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.LivingAttackEvent
import net.minecraftforge.event.entity.player.EntityInteractEvent

@Suppress("unused")
object InteractionSecurity {
	
	fun canDoSomethingHere(performer: EntityLivingBase) = canDoSomethingHere(performer, performer.posX, performer.posY, performer.posZ, performer.worldObj)
	
	fun canDoSomethingHere(performer: EntityLivingBase, x: Double, y: Double, z: Double, world: World = performer.worldObj) = canDoSomethingHere(performer, x.mfloor(), y.mfloor(), z.mfloor(), world)
	
	fun canDoSomethingHere(performer: EntityLivingBase, x: Int, y: Int, z: Int, world: World = performer.worldObj): Boolean {
		if (performer !is EntityPlayerMP) return true
//		return world.canMineBlock(performer, x, y, z)
		return ForgeHooks.onBlockBreakEvent(world, performer.theItemInWorldManager.gameType, performer, x, y, z).isCanceled
	}
	
	fun canInteractWithEntity(performer: EntityLivingBase, target: Entity) = when {
		performer === target      -> true
		performer is EntityPlayer -> !MinecraftForge.EVENT_BUS.post(EntityInteractEvent(performer, target))
		else                      -> true
	}
	
	fun canHurtEntity(attacker: EntityLivingBase, target: EntityLivingBase) = if (attacker === target) true else !MinecraftForge.EVENT_BUS.post(LivingAttackEvent(target, if (attacker is EntityPlayer) DamageSource.causePlayerDamage(attacker) else DamageSource.causeMobDamage(attacker), 0f))
}