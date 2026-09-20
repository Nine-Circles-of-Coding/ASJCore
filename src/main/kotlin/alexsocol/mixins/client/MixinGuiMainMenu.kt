package alexsocol.mixins.client

import alexsocol.patcher.PatcherPreConfigHandler
import alexsocol.patcher.asm.hook.RealmsDeleter
import net.minecraft.client.gui.GuiMainMenu
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

/**
 * The `deleteRealms` check used to live in [alexsocol.patcher.asm.ASJHookLoader], which simply did
 * not register the hook container when the option was off. A mixin config cannot be assembled that
 * late, so the mixin always applies and each injector returns early instead - with the flag off
 * neither one touches the callback, so the original method body runs untouched.
 */
@Mixin(GuiMainMenu::class)
abstract class MixinGuiMainMenu {

	/** Was `@Hook(injectOnExit = true)`. */
	@Inject(method = ["addSingleplayerMultiplayerButtons"], at = [At("RETURN")])
	fun asjRemoveRealmsButton(i: Int, j: Int, ci: CallbackInfo?) {
		if (!PatcherPreConfigHandler.deleteRealms) return
		RealmsDeleter.addSingleplayerMultiplayerButtons(this as Any as GuiMainMenu, i, j)
	}

	/** Was `@Hook(returnCondition = ALWAYS)` on a void method, i.e. skip the body entirely. */
	@Inject(method = ["func_140005_i"], at = [At("HEAD")], cancellable = true)
	fun asjSkipRealmsSwitch(ci: CallbackInfo?) {
		if (!PatcherPreConfigHandler.deleteRealms) return
		ci?.cancel()
	}
}
