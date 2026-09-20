@file:Suppress("unused")

package alexsocol.mixins.compat

import alexsocol.patcher.asm.hook.ASJHookHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Pseudo
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

/*
 * One-off compatibility fixes. Registration is by target-class presence, see ASJLateMixins.
 */

/** Biomes O' Plenty logs, so they rotate on all six sides like vanilla ones now do. */
@Pseudo
@Mixin(targets = ["biomesoplenty.common.blocks.BlockBOPLog"], remap = false)
abstract class MixinBlockBOPLog {
	@Inject(method = ["onBlockPlaced"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjOnBlockPlaced(world: World?, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float, hitZ: Float, meta: Int, cir: CallbackInfoReturnable<Int>?) {
		if (ASJHookHandler.onBlockPlaced(this, world, x, y, z, side, hitX, hitY, hitZ, meta))
			cir?.returnValue = ASJHookHandler.placeAllsided(this, world, x, y, z, side, hitX, hitY, hitZ, meta)
	}
}

@Pseudo
@Mixin(targets = ["biomesoplenty.common.itemblocks.ItemBlockLog"], remap = false)
abstract class MixinItemBlockLog {
	@Inject(method = ["getMetadata"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjGetMetadata(meta: Int, cir: CallbackInfoReturnable<Int>?) {
		if (ASJHookHandler.getMetadata(this, meta)) cir?.returnValue = ASJHookHandler.allsidedMeta(this, meta)
	}
}

/** Witchery drops backpacks itself; let BetterStorage do it when that mod is present. */
@Pseudo
@Mixin(targets = ["com.emoniph.witchery.dimension.WorldProviderDreamWorld"], remap = false)
abstract class MixinWorldProviderDreamWorld {
	@Inject(method = ["dropBetterBackpacks"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjDropBetterBackpacks(player: EntityPlayer, ci: CallbackInfo?) {
		if (ASJHookHandler.dropBetterBackpacks(null, player)) ci?.cancel()
	}
}
