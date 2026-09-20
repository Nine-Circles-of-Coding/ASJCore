package alexsocol.mixins.common;

import alexsocol.patcher.asm.hook.ASJHookHandler;
import net.minecraft.entity.EntityList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds spawn eggs for the mobs vanilla leaves out. Java because {@code <clinit>} is static, and no
 * fields of its own so Mixin's static-initialiser merging cannot interleave with the target's.
 */
@Mixin(EntityList.class)
public abstract class MixinEntityList {

	/** Was {@code @Hook(targetMethod = "<clinit>", injectOnExit = true)}. */
	@Inject(method = "<clinit>", at = @At("RETURN"))
	private static void asjEntityListClinit(CallbackInfo ci) {
		ASJHookHandler.EntityList$clinit(null);
	}
}
