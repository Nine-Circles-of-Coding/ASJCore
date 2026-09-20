package alexsocol.mixins.client

import alexsocol.patcher.asm.hook.NoEntityInteractionHandler
import net.minecraft.client.renderer.EntityRenderer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(EntityRenderer::class)
abstract class MixinEntityRenderer {

	/** Was `@Hook(targetMethod = "getMouseOver")`. */
	@Inject(method = ["getMouseOver"], at = [At("HEAD")])
	fun asjGetMouseOverPre(ticks: Float, ci: CallbackInfo?) {
		NoEntityInteractionHandler.getMouseOverPre(this as Any as EntityRenderer, ticks)
	}

	/**
	 * Was `@Hook(targetMethod = "getMouseOver", injectOnExit = true)`. `RETURN` rather than `TAIL`:
	 * HookLib injected before every return opcode, and `TAIL` would only catch the last one.
	 */
	@Inject(method = ["getMouseOver"], at = [At("RETURN")])
	fun asjGetMouseOverPost(ticks: Float, ci: CallbackInfo?) {
		NoEntityInteractionHandler.getMouseOverPost(this as Any as EntityRenderer, ticks)
	}
}
