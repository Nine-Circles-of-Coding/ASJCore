package alexsocol.asjlib.security

import alexsocol.asjlib.*
import net.minecraft.block.Block
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.init.Blocks
import net.minecraft.util.DamageSource
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.*
import net.minecraftforge.event.ForgeEventFactory
import net.minecraftforge.event.entity.living.LivingAttackEvent
import net.minecraftforge.event.entity.player.*
import net.minecraftforge.event.world.BlockEvent.BreakEvent

@Suppress("unused")
object InteractionSecurity {
	
	fun isInteractionBanned(performer: EntityLivingBase) = isInteractionBanned(performer, performer.posX, performer.posY, performer.posZ, performer.worldObj)
	
	fun isInteractionBanned(performer: EntityLivingBase, x: Number, y: Number, z: Number, world: World = performer.worldObj): Boolean {
		if (performer !is EntityPlayerMP) return false
		
		return ForgeEventFactory.onPlayerInteract(performer, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, x.D.mfloor(), y.D.mfloor(), z.D.mfloor(), 1, world).isCanceled
	}
	
	fun isBreakingBanned(performer: EntityLivingBase, x: Int, y: Int, z: Int, world: World = performer.worldObj, block: Block = Blocks.stone, meta: Int = 0): Boolean {
		if (performer !is EntityPlayerMP) return false
		if (!world.canMineBlock(performer, x, y, z)) return true
		
		return MinecraftForge.EVENT_BUS.post(BreakEvent(x, y, z, world, block, meta, performer))
	}
	
	fun isPlacementBanned(performer: EntityLivingBase, x: Int, y: Int, z: Int, world: World = performer.worldObj, block: Block = Blocks.stone, meta: Int = 0): Boolean {
		if (performer !is EntityPlayerMP) return false
		
		return ForgeEventFactory.onPlayerBlockPlace(performer, BlockSnapshot(world, x, y, z, block, meta), ForgeDirection.UNKNOWN).isCanceled
	}
	
	fun canInteractWithEntity(performer: EntityLivingBase, target: Entity) = when {
		performer === target      -> true
		performer is EntityPlayer -> !MinecraftForge.EVENT_BUS.post(EntityInteractEvent(performer, target))
		else                      -> true
	}
	
	fun canHurtEntity(attacker: EntityLivingBase, target: EntityLivingBase) = if (attacker === target) true else !MinecraftForge.EVENT_BUS.post(LivingAttackEvent(target, if (attacker is EntityPlayer) DamageSource.causePlayerDamage(attacker) else DamageSource.causeMobDamage(attacker), 0f))
}