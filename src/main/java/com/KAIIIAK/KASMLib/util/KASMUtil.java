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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings({"unused", "unchecked"})
public class KASMUtil {
	
	public static HookLogger logger = new HookLogger.Log4JLogger("KASMUtil");
	
	public static Class<?> getClass(int i) {
		try {
			return Class.forName(Thread.currentThread().getStackTrace()[i].getClassName());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T inst() {
		try {
			return (T) getClass(3).newInstance();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Method findLoadedClass;
	public static Field coreModInstance;
	public static Field loadPlugins;
	public static Field transformers;
	
	static {
		try {
			findLoadedClass = ASJReflectionHelper.getMethod(ClassLoader.class, "findLoadedClass", new Class[]{String.class});
			findLoadedClass.setAccessible(true);
			
			transformers = ASJReflectionHelper.getField(LaunchClassLoader.class, "transformers");
			loadPlugins = ASJReflectionHelper.getField(CoreModManager.class, "loadPlugins");
			coreModInstance = ASJReflectionHelper.getField(Class.forName("cpw.mods.fml.relauncher.CoreModManager$FMLPluginWrapper"), "coreModInstance");
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Map<String, IClassTransformer> generatedTransformersCache = new HashMap<>();
	public static Set<String> skippedTransformersCache = new HashSet<>();
	
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
		} catch (Throwable e) {
			logger.error("Exception getting active transformers", e);
		}
		try {
			List<?> lPL = (List<?>) loadPlugins.get(null);
			for (Object fmlPluginWrapper : Opt.it(lPL)) {
				IFMLLoadingPlugin plugin = transformersCacheExpected.computeIfAbsent(
					fmlPluginWrapper, fmlPluginWrapperr -> {
						try {
							return (IFMLLoadingPlugin) coreModInstance.get(fmlPluginWrapperr);
						} catch (Throwable e) {
							logger.error("Exception unwrapping plugin '" + fmlPluginWrapperr.getClass().getName() + "':", e);
							return null;
						}
					}
				);
				out:
				for (String namedTransformer : Opt.it(plugin.getASMTransformerClass())) {
					if (skippedTransformersCache.contains(namedTransformer)) continue;
					
					for (String exception : Opt.it(PatcherPreConfigHandler.INSTANCE.getTransformersForHookReplacerBlacklist()))
						if (namedTransformer.contains(exception)) continue out;
					
					if (allPossibleClasses.contains(namedTransformer)) continue;
					
					logger.trace("Found transformer (named): " + namedTransformer);
					IClassTransformer instantiated = generatedTransformersCache.computeIfAbsent(
						namedTransformer, rTransf -> {
							try {
								return (IClassTransformer) Launch.classLoader.loadClass(namedTransformer).newInstance();
							} catch (Throwable e) {
								logger.error("Exception instantiating named transformer '" + namedTransformer + "':", e);
								skippedTransformersCache.add(namedTransformer);
								return null;
							}
						}
					);
					if (instantiated != null) ret.add(instantiated);
					allPossibleClasses.add(namedTransformer);
				}
			}
		} catch (Throwable e) {
			logger.error("Exception getting inactive transformers", e);
		}
		
		return ret;
	}
	
	public static byte[] applyAllPossibleTransformers(String name, String transformedName, byte[] basicClass) {
		Set<IClassTransformer> all;
		try {
			all = collectAllPossibleTransformers();
		} catch (Throwable e) {
			logger.error("Exception collecting transformers for '" + transformedName + "':", e);
			return basicClass;
		}
		
		byte[] ret = basicClass;
		for (IClassTransformer transformer : Opt.it(all)) {
			try {
				ret = transformer.transform(name, transformedName, ret);
			} catch (Throwable e) {
				logger.error("Exception applying transformer '" + transformer.getClass().getName() + "':", e);
			}
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
							} catch (Throwable e) {
								throw new RuntimeException(e);
							}
						}
				);
				for (String registeredTransformers : Opt.it(pluginO.getASMTransformerClass())) {
					ret.add(registeredTransformers);
				}
			}
		} catch (Throwable e) {
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
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public static Class<?> findLoadedClass(String name) {
		try {
			return (Class<?>) findLoadedClass.invoke(Launch.classLoader, name);
		} catch (Throwable ignored) {}
		
		return null;
	}
	
	public static <T> T[] getAnnotationValueArray(Class<T> clazz, Object val) {
		if (val instanceof List) {
			List<?> list = (List<?>) val;
			T[] arr = (T[]) Array.newInstance(clazz, list.size());
			
			for (int i = 0; i < list.size(); i++) {
				arr[i] = clazz.cast(list.get(i));
			}
			return arr;
		}
		if (clazz.isInstance(val)) {
			T[] arr = (T[]) Array.newInstance(clazz, 1);
			arr[0] = (T) val;
			return arr;
		}
		if (val != null && val.getClass().isArray()) {
			return (T[]) val;
		}
		return (T[]) Array.newInstance(clazz, 0);
	}
	
	public static <T extends Enum<T>> T getAnnotationEnumValue(Class<T> enumClass, Object val) {
		if (val instanceof String) {
			return Enum.valueOf(enumClass, (String) val);
		}
		
		if (val instanceof String[]) {
			String[] arr = (String[]) val;
			
			if (arr.length < 2) {
				throw new IllegalArgumentException("Invalid enum annotation format");
			}
			
			org.objectweb.asm.Type asmType = org.objectweb.asm.Type.getType(arr[0]);
			Class<?> rawClass;
			try {
				rawClass = Class.forName(asmType.getClassName());
			} catch (Throwable e) {
				throw new RuntimeException("Failed to resolve enum class from annotation: " + arr[0], e);
			}
			
			if (!enumClass.isAssignableFrom(rawClass)) {
				throw new IllegalArgumentException("Wrong enum type. Expected: " + enumClass.getName() + ", got: " + rawClass.getName());
			}
			
			return Enum.valueOf(enumClass, arr[1]);
		}
		
		if (enumClass.isInstance(val)) {
			return (T) val;
		}
		
		throw new RuntimeException("Something went wrong with getting enum from annotation... expecting enumClass: " + enumClass.getName() + ", but got " + val.getClass().getName() + " with value: " + val);
	}
	
}
