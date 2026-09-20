package alexsocol.mixins.client;

import alexsocol.patcher.asm.hook.ASJHookHandler;
import net.minecraft.client.renderer.GLAllocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Defers display-list deletion to the render thread. Java because the target is static;
 * was {@code @Hook(returnCondition = ALWAYS)} on a static void method.
 */
@Mixin(GLAllocation.class)
public abstract class MixinGLAllocation {

	@Inject(method = "deleteDisplayLists", at = @At("HEAD"), cancellable = true)
	private static void asjDeleteDisplayLists(int id, CallbackInfo ci) {
		ASJHookHandler.deleteDisplayLists(null, id);
		ci.cancel();
	}
}
