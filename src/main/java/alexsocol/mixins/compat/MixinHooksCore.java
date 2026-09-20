package alexsocol.mixins.compat;

import alexsocol.patcher.asm.hook.ASJHookHandler;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * CoFH replaces vanilla's pane-connection logic with its own ASM-injected calls to this class, so
 * the {@code BlockPane} mixin alone is not enough when CoFH Core is installed - hence "a hook into
 * your hook".
 * <p>
 * <b>This one is the least certain mixin in the migration.</b> {@code HooksCore} lives inside CoFH's
 * coremod jar and is called from call sites CoFH itself injects, so two things could go wrong that
 * cannot be checked here: the class may be loaded before this config is prepared, and CoFH coremod
 * packages are sometimes on the classloader exclusion list. If {@code -Dmixin.debug.countInjections=true}
 * reports nothing injected with CoFH installed, revert this single hook to HookLib rather than
 * fighting it.
 * <p>
 * Java because the target is static.
 */
@Pseudo
@Mixin(targets = "cofh.asmhooks.HooksCore", remap = false)
public abstract class MixinHooksCore {

	/** Was {@code @Hook(returnCondition = ALWAYS, targetClass = "cofh.asmhooks.HooksCore")}. */
	@Inject(method = "paneConnectsTo", at = @At("HEAD"), cancellable = true, remap = false)
	private static void asjPaneConnectsTo(IBlockAccess world, int x, int y, int z, ForgeDirection dir, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(ASJHookHandler.paneConnectsTo(null, world, x, y, z, dir));
	}
}
