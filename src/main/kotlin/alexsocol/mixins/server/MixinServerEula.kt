package alexsocol.mixins.server

import alexsocol.patcher.asm.hook.ASJHookHandler
import net.minecraft.server.ServerEula
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.io.File

/** Accepts the EULA automatically on a dedicated server. Was `@Hook(injectOnExit = true, targetMethod = "<init>")`. */
@Mixin(ServerEula::class)
abstract class MixinServerEula {
	
	@Inject(method = ["<init>(Ljava/io/File;)V"], at = [At("RETURN")])
	fun asjServerEulaInit(file: File, ci: CallbackInfo?) {
		ASJHookHandler.ServerEula(this as Any as ServerEula, file)
	}
}
