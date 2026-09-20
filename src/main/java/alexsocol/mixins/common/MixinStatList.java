package alexsocol.mixins.common;

import alexsocol.patcher.asm.hook.ASJHookHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.EntityList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Replaces the whole statistics registry with a single dummy stat when {@code disableStats} is on.
 * <p>
 * Java because every target here is static. Deliberately declares no fields of its own: Mixin merges
 * a mixin's static initialisers into the target's, and this class injects into {@code <clinit>},
 * so anything it added would interleave with the very initialiser it is rewriting.
 * <p>
 * Each handler checks {@code disableStats} itself, so with the option off none of these touches the
 * callback and vanilla behaviour is untouched.
 */
@Mixin(StatList.class)
public abstract class MixinStatList {

	/** Was {@code @Hook(injectOnExit = true, targetMethod = "<clinit>")}. */
	@Inject(method = "<clinit>", at = @At("RETURN"))
	private static void asjStatListClinit(CallbackInfo ci) {
		ASJHookHandler.StatList_static(null);
	}

	/** Was {@code @Hook(returnCondition = ON_TRUE)}; the target returns void, so this cancels. */
	@Inject(method = "func_151178_a", at = @At("HEAD"), cancellable = true)
	private static void asjInitStats(CallbackInfo ci) {
		if (ASJHookHandler.func_151178_a(null)) ci.cancel();
	}

	/** Was {@code @Hook(returnCondition = ON_NOT_NULL)}. */
	@Inject(method = "func_151182_a", at = @At("HEAD"), cancellable = true)
	private static void asjEntityKilledByStat(EntityList.EntityEggInfo info, CallbackInfoReturnable<StatBase> cir) {
		StatBase stat = ASJHookHandler.func_151182_a(null, info);
		if (stat != null) cir.setReturnValue(stat);
	}

	@Inject(method = "func_151176_b", at = @At("HEAD"), cancellable = true)
	private static void asjEntityKilledStat(EntityList.EntityEggInfo info, CallbackInfoReturnable<StatBase> cir) {
		StatBase stat = ASJHookHandler.func_151176_b(null, info);
		if (stat != null) cir.setReturnValue(stat);
	}

	/** Was {@code @Hook(returnCondition = ON_NOT_NULL, injectOnExit = true)} with an {@code @ReturnValue}. */
	@ModifyReturnValue(method = "func_151177_a", at = @At("RETURN"))
	private static StatBase asjGetStat(StatBase original, String name) {
		StatBase stat = ASJHookHandler.func_151177_a(null, name, original);
		return stat != null ? stat : original;
	}
}
