package alexsocol.mixins.server;

import alexsocol.patcher.asm.hook.ASJHookHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.gui.MinecraftServerGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.swing.JComponent;

/**
 * Reskins the dedicated server's Swing window. Java because {@code createServerGui} is static, and
 * the other two are private instance methods on the same class.
 */
@Mixin(MinecraftServerGui.class)
public abstract class MixinMinecraftServerGui {

	@Inject(method = "createServerGui", at = @At("HEAD"))
	private static void asjCreateServerGui(DedicatedServer server, CallbackInfo ci) {
		ASJHookHandler.createServerGui(null, server);
	}

	@ModifyReturnValue(method = "getPlayerListComponent", at = @At("RETURN"))
	private JComponent asjGetPlayerListComponent(JComponent original) {
		return ASJHookHandler.getPlayerListComponent((MinecraftServerGui) (Object) this, original);
	}

	@ModifyReturnValue(method = "getLogComponent", at = @At("RETURN"))
	private JComponent asjGetLogComponent(JComponent original) {
		return ASJHookHandler.getLogComponent((MinecraftServerGui) (Object) this, original);
	}
}
