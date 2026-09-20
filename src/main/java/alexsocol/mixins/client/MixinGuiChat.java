package alexsocol.mixins.client;

import alexsocol.patcher.asm.hook.ASJHookHandler;
import net.minecraft.client.gui.GuiChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adjusts the protocol list the chat GUI builds in its static initialiser. Java because
 * {@code <clinit>} is static, and fieldless so Mixin merges no initialisers of its own.
 */
@Mixin(GuiChat.class)
public abstract class MixinGuiChat {

	@Inject(method = "<clinit>", at = @At("RETURN"))
	private static void asjGuiChatClinit(CallbackInfo ci) {
		ASJHookHandler.GuiChat_clinit(null);
	}
}
