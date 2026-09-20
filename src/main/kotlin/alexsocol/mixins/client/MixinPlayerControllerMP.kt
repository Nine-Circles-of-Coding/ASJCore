package alexsocol.mixins.client

import alexsocol.patcher.asm.hook.ASJHookHandler
import alexsocol.patcher.asm.hook.ReachDistanceHooks
import net.minecraft.client.multiplayer.PlayerControllerMP
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

/** Was `@Hook(returnCondition = ALWAYS)`. FYI: ChromatiCraft targets this method as well. */
@Mixin(PlayerControllerMP::class)
abstract class MixinPlayerControllerMP {

	@Inject(method = ["getBlockReachDistance"], at = [At("HEAD")], cancellable = true)
	fun asjGetBlockReachDistance(cir: CallbackInfoReturnable<Float>?) {
		cir?.returnValue = ReachDistanceHooks.getBlockReachDistance(this as Any as PlayerControllerMP)
	}
	
	/** Was `@Hook(returnCondition = ALWAYS)`. */
	@Inject(method = ["enableEverythingIsScrewedUpMode"], at = [At("HEAD")], cancellable = true)
	fun asjEnableEverythingIsScrewedUpMode(cir: CallbackInfoReturnable<Boolean>?) {
		cir?.returnValue = ASJHookHandler.enableEverythingIsScrewedUpMode(this as Any as PlayerControllerMP)
	}
}
