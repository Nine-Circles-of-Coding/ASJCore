package com.KAIIIAK.KASMLib.util;

import alexsocol.asjlib.ASJReflectionHelper;
import alexsocol.patcher.PatcherPreConfigHandler;
import com.KAIIIAK.nullsafety.Opt;
import cpw.mods.fml.relauncher.CoreModManager;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import gloomyfolken.hooklib.asm.HookLogger;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.lang.reflect.Field;
import java.util.*;

@SuppressWarnings("unused")
public class KASMUtil {
	
	public static HookLogger logger = new HookLogger.Log4JLogger("KASMUtil");
	
	public static Class<?> getClass(int i) {
		try {
			return Class.forName(Thread.currentThread().getStackTrace()[i].getClassName());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T inst() {
		try {
			return (T) getClass(3).newInstance();
		} catch (IllegalAccessException | InstantiationException | ClassCastException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Field coreModInstance;
	public static Field loadPlugins;
	public static Field transformers;
	
	static {
		try {
			transformers = ASJReflectionHelper.getField(LaunchClassLoader.class, "transformers");
			loadPlugins = ASJReflectionHelper.getField(CoreModManager.class, "loadPlugins");
			coreModInstance = ASJReflectionHelper.getField(Class.forName("cpw.mods.fml.relauncher.CoreModManager$FMLPluginWrapper"), "coreModInstance");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Map<String, IClassTransformer> generatedTransformersCache = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public static Set<IClassTransformer> collectAllPossibleTransformers() {
		Set<String> allPossibleClasses = new LinkedHashSet<>();
		Set<IClassTransformer> ret = new LinkedHashSet<>();
		try {
			List<IClassTransformer> list = (List<IClassTransformer>) transformers.get(Launch.classLoader);
			for (IClassTransformer iClassTransformer : Opt.it(list)) {
				allPossibleClasses.add(iClassTransformer.getClass().getName());
				logger.trace("Found transformer (class): " + iClassTransformer.getClass().getName());
				ret.add(iClassTransformer);
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		try {
			List<?> lPL = (List<?>) loadPlugins.get(null);
			for (Object fmlPluginWrapper : Opt.it(lPL)) {
				IFMLLoadingPlugin plugin = transformersCacheExpected.computeIfAbsent(
						fmlPluginWrapper, fmlPluginWrapperr -> {
							try {
								return (IFMLLoadingPlugin) coreModInstance.get(fmlPluginWrapperr);
							} catch (IllegalAccessException e) {
								throw new RuntimeException(e);
							}
						}
				);
				out: for (String registeredTransformers : Opt.it(plugin.getASMTransformerClass())) {
					for (String exception : Opt.it(PatcherPreConfigHandler.INSTANCE.getTransformersForHookReplacerBlacklist()))
						if (registeredTransformers.contains(exception)) continue out;
					
					if (!allPossibleClasses.contains(registeredTransformers)) {
						logger.trace("Found transformer (named): " + registeredTransformers);
						ret.add(generatedTransformersCache.computeIfAbsent(
								registeredTransformers, rTransf -> {
									try {
										return (IClassTransformer) Launch.classLoader.loadClass(registeredTransformers).newInstance();
									} catch (InstantiationException | IllegalAccessException |
											 ClassNotFoundException e) {
										throw new RuntimeException(e);
									}
								}
						));
						allPossibleClasses.add(registeredTransformers);
					}
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return ret;
	}
	
	public static byte[] applyAllPossibleTransformers(String name, String transformedName, byte[] basicClass) {
		byte[] ret = basicClass;
		for (IClassTransformer transformer : Opt.it(collectAllPossibleTransformers())) {
			ret = transformer.transform(name, transformedName, ret);
		}
		return ret;
	}
	
	public static Map<Object, IFMLLoadingPlugin> transformersCacheExpected = new HashMap<>();
	
	public static List<String> getAllTransformersThatAreExpectedToBeRegistered() {
		List<String> ret = new ArrayList<>();
		try {
			List<?> list = (List<?>) loadPlugins.get(null);
			for (Object fmlPluginWrapper : Opt.it(list)) {
				IFMLLoadingPlugin pluginO = transformersCacheExpected.computeIfAbsent(
						fmlPluginWrapper, fmlPluginWrapperr -> {
							try {
								return (IFMLLoadingPlugin) coreModInstance.get(fmlPluginWrapperr);
							} catch (IllegalAccessException e) {
								throw new RuntimeException(e);
							}
						}
				);
				for (String registeredTransformers : Opt.it(pluginO.getASMTransformerClass())) {
					ret.add(registeredTransformers);
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public static Map<IClassTransformer, Class<?>> transformersCache = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public static List<String> getAllRegisteredTransformers() {
		List<String> ret = new ArrayList<>();
		try {
			List<IClassTransformer> list = (List<IClassTransformer>) transformers.get(Launch.classLoader);
			for (IClassTransformer iClassTransformer : Opt.it(list)) {
				ret.add(transformersCache.computeIfAbsent(iClassTransformer, IClassTransformer::getClass).getName());
			}
			
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return ret;
	}
}
