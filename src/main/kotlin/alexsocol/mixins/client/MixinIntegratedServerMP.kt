package alexsocol.mixins.client

import alexsocol.patcher.asm.hook.ASJHookHandler
import alexsocol.patcher.duck.IMultiPlayerAware
import net.minecraft.server.integrated.IntegratedServer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

/** Client-side half of [IMultiPlayerAware]; IntegratedServer only exists on the client. */
@Mixin(IntegratedServer::class)
abstract class MixinIntegratedServerMP: IMultiPlayerAware {
	
	override fun isMultiPlayer() = ASJHookHandler.isMultiPlayer(this as Any as IntegratedServer)
	
	@Inject(method = ["isSnooperEnabled"], at = [At("HEAD")], cancellable = true)
	fun asjIsSnooperEnabled(cir: CallbackInfoReturnable<Boolean>?) {
		cir?.returnValue = false
	}
}
