package alexsocol.mixins.server

import alexsocol.patcher.asm.hook.ASJHookHandler
import alexsocol.patcher.duck.IMultiPlayerAware
import net.minecraft.server.dedicated.DedicatedServer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

/** Server-side half of [IMultiPlayerAware]. */
@Mixin(DedicatedServer::class)
abstract class MixinDedicatedServerMP: IMultiPlayerAware {
	
	override fun isMultiPlayer() = ASJHookHandler.isMultiPlayer(this as Any as DedicatedServer)
	
	@Inject(method = ["isSnooperEnabled"], at = [At("HEAD")], cancellable = true)
	fun asjIsSnooperEnabled(cir: CallbackInfoReturnable<Boolean>?) {
		cir?.returnValue = false
	}
}
