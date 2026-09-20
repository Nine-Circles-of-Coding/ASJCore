package alexsocol.mixins.common;

import alexsocol.patcher.asm.hook.ASJHookHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets blocks opt into connecting to fences via ASJCore's own interfaces.
 * <p>
 * Java because {@code func_149825_a} is static and Mixin needs a static handler for a static target.
 * Both targets are vanilla (SRG parameter names), so unlike most of the compat mixins these do get
 * remapped and must appear in the refmap.
 */
@Mixin(BlockFence.class)
public abstract class MixinBlockFence {

	/** Was {@code @Hook(returnCondition = ON_TRUE)}. */
	@Inject(method = "canConnectFenceTo", at = @At("HEAD"), cancellable = true)
	private void asjCanConnectFenceTo(IBlockAccess world, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
		if (ASJHookHandler.canConnectFenceTo((BlockFence) (Object) this, world, x, y, z)) cir.setReturnValue(true);
	}

	/** Was {@code @Hook(returnCondition = ALWAYS)} on a static target. */
	@Inject(method = "func_149825_a", at = @At("HEAD"), cancellable = true)
	private static void asjIsFence(Block block, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(ASJHookHandler.func_149825_a(null, block));
	}
}
