package alexsocol.mixins.client

import alexsocol.patcher.asm.hook.ASJHookHandler
import net.minecraft.client.gui.achievement.GuiStats
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

/** Trims the stats screen down to the one remaining button when `disableStats` is on. */
@Mixin(GuiStats::class)
abstract class MixinGuiStats {

	/** Was `@Hook(injectOnExit = true)`. */
	@Inject(method = ["initGui"], at = [At("RETURN")])
	fun asjInitGui(ci: CallbackInfo?) {
		ASJHookHandler.initGui(this as Any as GuiStats)
	}

	@Inject(method = ["func_146541_h"], at = [At("RETURN")])
	fun asjTrimButtons(ci: CallbackInfo?) {
		ASJHookHandler.func_146541_h(this as Any as GuiStats)
	}
}
