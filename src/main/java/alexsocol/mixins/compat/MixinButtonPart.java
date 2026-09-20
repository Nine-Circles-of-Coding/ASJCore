package alexsocol.mixins.compat;

import alexsocol.patcher.PatcherPreConfigHandler;
import alexsocol.patcher.asm.hook.BlockButtonExtender;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.minecraft.ButtonPart;
import codechicken.multipart.minecraft.McBlockPart;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The Forge Multipart half of the top/bottom button support - see
 * {@code alexsocol.mixins.common.MixinBlockButton} for the vanilla block.
 * <p>
 * Java rather than Kotlin because {@code <clinit>} and {@code placement} are static, and Mixin needs
 * a static handler for a static target (Kotlin can only get there via a companion object, which puts
 * the annotation out of the processor's reach).
 * <p>
 * {@code @Pseudo} rather than a real {@code @Mixin(ButtonPart.class)} because the compile-time stub
 * in {@code src/api/java} only declares the two int arrays and {@code pressed()}; it has no
 * {@code getBounds} or {@code placement} for the processor to match against. The parameter types are
 * still the stubbed ones, which is fine - they resolve to the real classes at runtime, and the stubs
 * are compile-only and never packaged.
 * <p>
 * {@code placement} is treated as static on the strength of the old hook taking a nullable
 * {@code ButtonPart?} first parameter, which was HookLib's calling convention for a static target
 * (contrast {@code getBounds}, whose receiver was non-null). If that is wrong, Mixin will say so
 * loudly rather than silently skipping, because the late config sets {@code defaultRequire: 1}.
 */
@Pseudo
@Mixin(targets = "codechicken.multipart.minecraft.ButtonPart", remap = false)
public abstract class MixinButtonPart {

	/** Was {@code @Hook(injectOnExit = true, targetMethod = "<clinit>")}. */
	@Inject(method = "<clinit>", at = @At("RETURN"), remap = false)
	private static void asjButtonPartClinit(CallbackInfo ci) {
		if (!PatcherPreConfigHandler.INSTANCE.getTopDownButtons()) return;
		BlockButtonExtender.ButtonPart$clinit(null);
	}

	/** Was {@code @Hook(returnCondition = ON_NOT_NULL)}. */
	@Inject(method = "getBounds", at = @At("HEAD"), cancellable = true, remap = false)
	private void asjGetBounds(CallbackInfoReturnable<Cuboid6> cir) {
		if (!PatcherPreConfigHandler.INSTANCE.getTopDownButtons()) return;
		Cuboid6 bounds = BlockButtonExtender.getBounds((ButtonPart) (Object) this);
		if (bounds != null) cir.setReturnValue(bounds);
	}

	/** Was {@code @Hook(returnCondition = ON_NOT_NULL)} on a static target. */
	@Inject(method = "placement", at = @At("HEAD"), cancellable = true, remap = false)
	private static void asjPlacement(World world, BlockCoord pos, int side, int type, CallbackInfoReturnable<McBlockPart> cir) {
		if (!PatcherPreConfigHandler.INSTANCE.getTopDownButtons()) return;
		McBlockPart part = BlockButtonExtender.placement(null, world, pos, side, type);
		if (part != null) cir.setReturnValue(part);
	}
}
