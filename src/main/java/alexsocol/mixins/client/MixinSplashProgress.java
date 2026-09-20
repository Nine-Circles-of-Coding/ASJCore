package alexsocol.mixins.client;

import alexsocol.patcher.asm.hook.ASJHookHandler;
import cpw.mods.fml.client.SplashProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** FML class, so {@code remap = false}. Was {@code @Hook(injectOnExit = true)} on a static target. */
@Mixin(value = SplashProgress.class, remap = false)
public abstract class MixinSplashProgress {

	@Inject(method = "start", at = @At("RETURN"), remap = false)
	private static void asjStart(CallbackInfo ci) {
		ASJHookHandler.start(null);
	}
}
