package alexsocol.asjlib.security

import net.minecraft.block.Block
import net.minecraft.entity.*
import net.minecraft.init.Blocks
import net.minecraft.world.World

@Suppress("unused")
@Deprecated("Will be removed in 1.3.*")
object InteractionSecurity {
	
	@Deprecated("Will be removed in 1.3.*")
	fun isInteractionBanned(performer: EntityLivingBase) = false
	
	@Deprecated("Will be removed in 1.3.*")
	fun isInteractionBanned(performer: EntityLivingBase, x: Number, y: Number, z: Number, world: World = performer.worldObj) = false
	
	@Deprecated("Will be removed in 1.3.*")
	fun isBreakingBanned(performer: EntityLivingBase, x: Int, y: Int, z: Int, world: World = performer.worldObj, block: Block = Blocks.stone, meta: Int = 0) = false
	
	@Deprecated("Will be removed in 1.3.*")
	fun isPlacementBanned(performer: EntityLivingBase, x: Int, y: Int, z: Int, world: World = performer.worldObj, block: Block = Blocks.stone, meta: Int = 0) = false
	
	@Deprecated("Will be removed in 1.3.*")
	fun canInteractWithEntity(performer: EntityLivingBase, target: Entity) = true
	
	@Deprecated("Will be removed in 1.3.*")
	fun canHurtEntity(attacker: EntityLivingBase, target: EntityLivingBase) = true
}