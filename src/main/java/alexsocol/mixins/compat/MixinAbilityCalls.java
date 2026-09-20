package alexsocol.mixins.compat;

import alexsocol.patcher.asm.hook.ReachDistanceHooks;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Routes ChromatiCraft's reach ability through ASJCore's attribute handler instead of letting it set
 * the reach distance directly.
 * <p>
 * Java because the target is static, and Mixin needs a static handler for a static target.
 */
@Pseudo
@Mixin(targets = "Reika.ChromatiCraft.Auxiliary.Ability.AbilityCalls", remap = false)
public abstract class MixinAbilityCalls {

	/** Was {@code @Hook(returnCondition = ALWAYS)} on a static void target. */
	@Inject(method = "setReachDistance", at = @At("HEAD"), cancellable = true, remap = false)
	private static void asjSetReachDistance(EntityPlayer player, int dist, CallbackInfo ci) {
		ReachDistanceHooks.setReachDistance(null, player, dist);
		ci.cancel();
	}
}
