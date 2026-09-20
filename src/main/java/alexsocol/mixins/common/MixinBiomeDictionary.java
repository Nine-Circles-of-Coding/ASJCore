package alexsocol.mixins.common;

import alexsocol.patcher.asm.hook.BiomeDictionaryForWEHooks;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Teaches Forge's biome dictionary about WorldEngine biomes, which are not in {@code BiomeGenBase}'s
 * registry array and so are invisible to every lookup here.
 * <p>
 * <b>This one is Java on purpose.</b> All six targets are static, and Mixin requires a handler to be
 * static exactly when its target is - but Kotlin can only produce a static method via {@code @JvmStatic}
 * in a companion object, which leaves the annotation sitting on the {@code Companion} class as far as
 * the annotation processor is concerned ("Found @Inject annotation on a non-mixin method"). Every
 * static-target mixin in this mod has to be Java for the same reason.
 * <p>
 * {@link BiomeDictionary} is a Forge class, so nothing in it has an SRG name and every injector is
 * {@code remap = false}. The handlers still take the leading {@code static} parameter that was
 * HookLib's calling convention for a static target; null is passed for it, exactly as HookLib did.
 */
@Mixin(value = BiomeDictionary.class, remap = false)
public abstract class MixinBiomeDictionary {

	/** Was {@code @Hook(returnCondition = ON_TRUE)}. */
	@Inject(method = "registerBiomeType", at = @At("HEAD"), cancellable = true, remap = false)
	private static void asjRegisterBiomeType(BiomeGenBase biome, Type[] types, CallbackInfoReturnable<Boolean> cir) {
		if (BiomeDictionaryForWEHooks.registerBiomeType(null, biome, types)) cir.setReturnValue(true);
	}

	/** Was {@code @Hook(returnCondition = ALWAYS, injectOnExit = true)} with an {@code @ReturnValue} capture. */
	@ModifyReturnValue(method = "getBiomesForType", at = @At("RETURN"), remap = false)
	private static BiomeGenBase[] asjGetBiomesForType(BiomeGenBase[] original, Type type) {
		return BiomeDictionaryForWEHooks.getBiomesForType(null, type, original);
	}

	@ModifyReturnValue(method = "getTypesForBiome", at = @At("RETURN"), remap = false)
	private static Type[] asjGetTypesForBiome(Type[] original, BiomeGenBase biome) {
		return BiomeDictionaryForWEHooks.getTypesForBiome(null, biome, original);
	}

	@ModifyReturnValue(method = "isBiomeOfType", at = @At("RETURN"), remap = false)
	private static boolean asjIsBiomeOfType(boolean original, BiomeGenBase biome, Type type) {
		return BiomeDictionaryForWEHooks.isBiomeOfType(null, biome, type, original);
	}

	/** Spelled out: Forge overloads this with an {@code int} variant that must not be touched. */
	@ModifyReturnValue(method = "isBiomeRegistered(Lnet/minecraft/world/biome/BiomeGenBase;)Z", at = @At("RETURN"), remap = false)
	private static boolean asjIsBiomeRegistered(boolean original, BiomeGenBase biome) {
		return BiomeDictionaryForWEHooks.isBiomeRegistered(null, biome, original);
	}

	/** Was {@code @Hook(returnCondition = ON_TRUE)}, but the target returns void, so this cancels. */
	@Inject(method = "makeBestGuess", at = @At("HEAD"), cancellable = true, remap = false)
	private static void asjMakeBestGuess(BiomeGenBase biome, CallbackInfo ci) {
		if (BiomeDictionaryForWEHooks.makeBestGuess(null, biome)) ci.cancel();
	}
}
