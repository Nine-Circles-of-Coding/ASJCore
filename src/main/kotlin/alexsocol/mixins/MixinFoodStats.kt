package alexsocol.mixins

import alexsocol.patcher.asm.hook.ASJHookHandler
import alexsocol.patcher.duck.IFoodStatsHost
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.FoodStats
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(FoodStats::class)
abstract class MixinFoodStats: IFoodStatsHost {

	/**
	 * Kept public and under its original name on purpose: this field used to be spliced in by
	 * `@HookField`, so downstream mods may still look it up by name. [JvmField] keeps it a plain
	 * field instead of a property with generated accessors, which is what Mixin needs to see.
	 */
	@Unique
	@JvmField
	var ASJCore_host: EntityPlayer? = null

	override fun getAsjHost() = ASJCore_host

	override fun setAsjHost(host: EntityPlayer?) {
		ASJCore_host = host
	}
	
	/** Was `@Hook(returnCondition = ALWAYS)`; the target returns void, so this replaces its body. */
	@Inject(method = ["addStats(IF)V"], at = [At("HEAD")], cancellable = true)
	fun asjAddStats(foodLevel: Int, saturation: Float, ci: CallbackInfo?) {
		ASJHookHandler.addStats(this as Any as FoodStats, foodLevel, saturation)
		ci?.cancel()
	}
}
