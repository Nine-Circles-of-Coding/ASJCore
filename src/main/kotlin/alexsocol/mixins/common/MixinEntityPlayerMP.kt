package alexsocol.mixins.common

import alexsocol.patcher.asm.hook.ASJHookHandler
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.stats.StatBase
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

/** Stops non-achievement stats being recorded at all when `disableStats` is on. */
@Mixin(EntityPlayerMP::class)
abstract class MixinEntityPlayerMP {

	/** Was `@Hook(returnCondition = ON_TRUE)`; the target returns void, so this cancels. */
	@Inject(method = ["addStat"], at = [At("HEAD")], cancellable = true)
	fun asjAddStat(stat: StatBase?, amount: Int, ci: CallbackInfo?) {
		if (ASJHookHandler.addStat(this as Any as EntityPlayerMP, stat, amount)) ci?.cancel()
	}
}
