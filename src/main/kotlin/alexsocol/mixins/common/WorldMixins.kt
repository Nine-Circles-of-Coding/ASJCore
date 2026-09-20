@file:Suppress("unused")

package alexsocol.mixins.common

import alexsocol.patcher.asm.hook.ASJHookHandler
import com.llamalad7.mixinextras.injector.ModifyReturnValue
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.world.World
import net.minecraft.world.WorldProviderEnd
import net.minecraft.world.WorldProviderHell
import net.minecraft.world.WorldServer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

/*
 * World-level fixes. Note which targets remap: `updateWeatherBody` is a Forge addition with no SRG
 * name at all, while everything else here is vanilla even where Forge has patched the body.
 */

@Mixin(World::class)
abstract class MixinWorldFixes {

	/** Was `ON_TRUE` with `returnAnotherMethod = "swapLightningSpawn"`: weather effects are rerouted. */
	@Inject(method = ["spawnEntityInWorld"], at = [At("HEAD")], cancellable = true)
	fun asjSpawnEntityInWorld(target: Entity?, cir: CallbackInfoReturnable<Boolean>?) {
		val world = this as Any as World
		if (ASJHookHandler.spawnEntityInWorld(world, target))
			cir?.returnValue = ASJHookHandler.swapLightningSpawn(world, target)
	}

	@Inject(method = ["extinguishFire"], at = [At("HEAD")], cancellable = true)
	fun asjExtinguishFire(player: EntityPlayer?, x: Int, y: Int, z: Int, side: Int, cir: CallbackInfoReturnable<Boolean>?) {
		cir?.returnValue = ASJHookHandler.extinguishFire(this as Any as World, player, x, y, z, side)
	}

	/** Was `ALWAYS` + `injectOnExit` + `@ReturnValue`: the list is rebuilt without its nulls. */
	@ModifyReturnValue(method = ["getCollidingBoundingBoxes"], at = [At("RETURN")])
	fun asjGetCollidingBoundingBoxes(original: MutableList<AxisAlignedBB?>, entity: Entity?, aabb: AxisAlignedBB?): List<AxisAlignedBB> =
		ASJHookHandler.getCollidingBoundingBoxes(this as Any as World, entity, aabb, original)

	/** Target returns void, so `ON_TRUE` meant "skip the vanilla body". */
	@Inject(method = ["spawnParticle"], at = [At("HEAD")], cancellable = true)
	fun asjSpawnParticle(name: String?, x: Double, y: Double, z: Double, mx: Double, my: Double, mz: Double, ci: CallbackInfo?) {
		if (ASJHookHandler.spawnParticle(this as Any as World, name, x, y, z, mx, my, mz)) ci?.cancel()
	}

	/** Forge-added method - no SRG name, hence `remap = false`. Returns void, so this cancels. */
	@Inject(method = ["updateWeatherBody"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjUpdateWeatherBody(ci: CallbackInfo?) {
		if (ASJHookHandler.updateWeatherBody(this as Any as World)) ci?.cancel()
	}
}

@Mixin(WorldServer::class)
abstract class MixinWorldServer {

	@Inject(method = ["wakeAllPlayers"], at = [At("RETURN")])
	fun asjWakeAllPlayers(ci: CallbackInfo?) {
		ASJHookHandler.wakeAllPlayers(this as Any as WorldServer)
	}
}

@Mixin(WorldProviderHell::class)
abstract class MixinWorldProviderHell {

	@Inject(method = ["canRespawnHere"], at = [At("HEAD")], cancellable = true)
	fun asjCanRespawnHere(cir: CallbackInfoReturnable<Boolean>?) {
		cir?.returnValue = ASJHookHandler.canRespawnHere(this as Any as WorldProviderHell)
	}
}

@Mixin(WorldProviderEnd::class)
abstract class MixinWorldProviderEnd {

	@Inject(method = ["canRespawnHere"], at = [At("HEAD")], cancellable = true)
	fun asjCanRespawnHere(cir: CallbackInfoReturnable<Boolean>?) {
		cir?.returnValue = ASJHookHandler.canRespawnHere(this as Any as WorldProviderEnd)
	}
}
