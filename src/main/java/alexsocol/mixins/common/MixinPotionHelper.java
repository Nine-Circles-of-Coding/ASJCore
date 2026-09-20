package alexsocol.mixins.common;

import alexsocol.patcher.asm.hook.ASJHookHandler;
import net.minecraft.potion.PotionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

/** Potion colour blending. Java because the target is static. Was {@code @Hook(returnCondition = ALWAYS)}. */
@Mixin(PotionHelper.class)
public abstract class MixinPotionHelper {

	@Inject(method = "calcPotionLiquidColor", at = @At("HEAD"), cancellable = true)
	private static void asjCalcPotionLiquidColor(Collection potions, CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue(ASJHookHandler.calcPotionLiquidColor(null, potions));
	}
}
