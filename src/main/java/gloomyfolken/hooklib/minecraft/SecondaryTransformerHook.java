package gloomyfolken.hooklib.minecraft;

import cpw.mods.fml.common.Loader;
import gloomyfolken.hooklib.asm.Hook;
import gloomyfolken.hooklib.asm.HookClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class SecondaryTransformerHook {
	
	/**
	 * Регистрирует хук-трансформер последним.
	 */
	@Hook
	public static void injectData(Loader loader, Object... data) {
		ClassLoader classLoader = SecondaryTransformerHook.class.getClassLoader();
		if (classLoader instanceof LaunchClassLoader) {
			((LaunchClassLoader) classLoader).registerTransformer(MinecraftClassTransformer.class.getName());
		} else {
			HookClassTransformer.logger.error("HookLib was not loaded by LaunchClassLoader. Hooks will not be injected.");
		}
	}
}
