package alexsocol.mixins.common

import alexsocol.patcher.PatcherPreConfigHandler
import alexsocol.patcher.asm.hook.BlockButtonExtender
import net.minecraft.block.Block
import net.minecraft.block.BlockButton
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

/**
 * Lets buttons sit on the top and bottom faces of a block.
 *
 * Every one of these was `returnCondition = ALWAYS` or `ON_TRUE`, i.e. the hook replaced the vanilla
 * body outright. Three of the targets return void (`onNeighborBlockChange`, `func_150043_b` and
 * `func_150042_a`), so there the replacement is "run the handler, then cancel" rather than a
 * return value.
 *
 * `topDownButtons` used to decide whether the hook container was registered at all; a mixin config
 * is assembled too early for that, so each injector checks the flag and leaves the callback alone
 * when it is off, which lets the vanilla body run exactly as before.
 */
@Mixin(BlockButton::class)
abstract class MixinBlockButton {

	@Inject(method = ["canPlaceBlockOnSide"], at = [At("HEAD")], cancellable = true)
	fun asjCanPlaceBlockOnSide(world: World, x: Int, y: Int, z: Int, side: Int, cir: CallbackInfoReturnable<Boolean>?) {
		if (!PatcherPreConfigHandler.topDownButtons) return
		cir?.returnValue = BlockButtonExtender.canPlaceBlockOnSide(this as Any as BlockButton, world, x, y, z, side)
	}

	@Inject(method = ["canPlaceBlockAt"], at = [At("HEAD")], cancellable = true)
	fun asjCanPlaceBlockAt(world: World, x: Int, y: Int, z: Int, cir: CallbackInfoReturnable<Boolean>?) {
		if (!PatcherPreConfigHandler.topDownButtons) return
		cir?.returnValue = BlockButtonExtender.canPlaceBlockAt(this as Any as BlockButton, world, x, y, z)
	}

	@Inject(method = ["onBlockPlaced"], at = [At("HEAD")], cancellable = true)
	fun asjOnBlockPlaced(world: World, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float, hitZ: Float, metadata: Int, cir: CallbackInfoReturnable<Int>?) {
		if (!PatcherPreConfigHandler.topDownButtons) return
		cir?.returnValue = BlockButtonExtender.onBlockPlaced(this as Any as BlockButton, world, x, y, z, side, hitX, hitY, hitZ, metadata)
	}

	/** Vanilla's "pick a face that still has support"; SRG-only name, so it maps to itself. */
	@Inject(method = ["func_150045_e"], at = [At("HEAD")], cancellable = true)
	fun asjPickValidMeta(world: World, x: Int, y: Int, z: Int, cir: CallbackInfoReturnable<Int>?) {
		if (!PatcherPreConfigHandler.topDownButtons) return
		cir?.returnValue = BlockButtonExtender.pickValidMeta(this as Any as BlockButton, world, x, y, z)
	}

	@Inject(method = ["onNeighborBlockChange"], at = [At("HEAD")], cancellable = true)
	fun asjOnNeighborBlockChange(world: World, x: Int, y: Int, z: Int, block: Block?, ci: CallbackInfo?) {
		if (!PatcherPreConfigHandler.topDownButtons) return
		BlockButtonExtender.onNeighborBlockChange(this as Any as BlockButton, world, x, y, z, block)
		ci?.cancel()
	}

	/** Target returns void, so `ON_TRUE` meant "skip the vanilla body", not "return true". */
	@Inject(method = ["func_150043_b"], at = [At("HEAD")], cancellable = true)
	fun asjSetBlockBoundsFromMeta(meta: Int, ci: CallbackInfo?) {
		if (!PatcherPreConfigHandler.topDownButtons) return
		if (BlockButtonExtender.setBlockBoundsFromMeta(this as Any as BlockButton, meta)) ci?.cancel()
	}

	/** Was `ON_TRUE` with `intReturnConstant = 15`. */
	@Inject(method = ["isProvidingStrongPower"], at = [At("HEAD")], cancellable = true)
	fun asjIsProvidingStrongPower(world: IBlockAccess, x: Int, y: Int, z: Int, side: Int, cir: CallbackInfoReturnable<Int>?) {
		if (!PatcherPreConfigHandler.topDownButtons) return
		if (BlockButtonExtender.isProvidingStrongPower(this as Any as BlockButton, world, x, y, z, side)) cir?.returnValue = 15
	}

	@Inject(method = ["func_150042_a"], at = [At("HEAD")], cancellable = true)
	fun asjUpdateNeighbor(world: World, x: Int, y: Int, z: Int, metaBase: Int, ci: CallbackInfo?) {
		if (!PatcherPreConfigHandler.topDownButtons) return
		BlockButtonExtender.updateNeighbor(this as Any as BlockButton, world, x, y, z, metaBase)
		ci?.cancel()
	}
}
