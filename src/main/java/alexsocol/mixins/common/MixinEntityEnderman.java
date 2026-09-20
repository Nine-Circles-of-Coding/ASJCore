package alexsocol.mixins.common;

import alexsocol.patcher.asm.hook.ASJHookHandler;
import net.minecraft.entity.monster.EntityEnderman;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Rebuilds the enderman's static state from scratch: vanilla sizes {@code carriableBlocks} to the
 * block id range it knew about, which overflows once more than 256 block ids exist.
 * <p>
 * Was {@code @Hook(returnCondition = ALWAYS, targetMethod = "<clinit>")}, i.e. replace the whole
 * static initialiser. Cancelling at HEAD is exact parity because the handler reassigns every field
 * the original would have set - {@code attackingSpeedBoostModifierUUID},
 * {@code attackingSpeedBoostModifier} and {@code carriableBlocks}, all three of which
 * {@code asjlib_at.cfg} makes non-final with {@code public-f}.
 * <p>
 * Java because {@code <clinit>} is static, and deliberately fieldless so Mixin does not merge static
 * initialisers of its own into the initialiser being replaced.
 */
@Mixin(EntityEnderman.class)
public abstract class MixinEntityEnderman {

	@Inject(method = "<clinit>", at = @At("HEAD"), cancellable = true)
	private static void asjEndermanClinit(CallbackInfo ci) {
		ASJHookHandler.EntityEnderman(null);
		ci.cancel();
	}
}
