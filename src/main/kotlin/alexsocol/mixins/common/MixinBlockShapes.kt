@file:Suppress("unused")

package alexsocol.mixins.common

import alexsocol.patcher.asm.hook.ASJHookHandler
import net.minecraft.block.BlockPane
import net.minecraft.block.BlockWall
import net.minecraft.world.IBlockAccess
import net.minecraftforge.common.util.ForgeDirection
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

/*
 * Lets blocks opt into connecting to panes and walls through ASJCore's own interfaces.
 *
 * Note the mixed remapping: `canConnectWallTo` is vanilla (SRG parameter names, so it remaps), while
 * `canPaneConnectTo` and `isSideSolid` are Forge additions and have no SRG name at all.
 *
 * The fence half is Java - see MixinBlockFence - because one of its targets is static.
 */

@Mixin(BlockPane::class)
abstract class MixinBlockPane {

	/** Was `@Hook(returnCondition = ON_TRUE)`. Forge-added method, hence `remap = false`. */
	@Inject(method = ["canPaneConnectTo"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjCanPaneConnectTo(world: IBlockAccess, x: Int, y: Int, z: Int, dir: ForgeDirection, cir: CallbackInfoReturnable<Boolean>?) {
		if (ASJHookHandler.canPaneConnectTo(this as Any as BlockPane, world, x, y, z, dir)) cir?.returnValue = true
	}
}

@Mixin(BlockWall::class)
abstract class MixinBlockWall {

	/** Was `@Hook(returnCondition = ON_TRUE)`. Vanilla method, so this one remaps. */
	@Inject(method = ["canConnectWallTo"], at = [At("HEAD")], cancellable = true)
	fun asjCanConnectWallTo(world: IBlockAccess, x: Int, y: Int, z: Int, cir: CallbackInfoReturnable<Boolean>?) {
		if (ASJHookHandler.canConnectWallTo(this as Any as BlockWall, world, x, y, z)) cir?.returnValue = true
	}

	/**
	 * Was `@Hook(returnCondition = ALWAYS, createMethod = true)`: `BlockWall` does not declare
	 * `isSideSolid`, it inherits Forge's implementation from `Block`. A plain, un-annotated method on
	 * the mixin is merged into the target and becomes the override - which is the whole of what
	 * `createMethod` did here.
	 */
	open fun isSideSolid(world: IBlockAccess, x: Int, y: Int, z: Int, side: ForgeDirection): Boolean =
		ASJHookHandler.isSideSolid(this as Any as BlockWall, world, x, y, z, side)
}
