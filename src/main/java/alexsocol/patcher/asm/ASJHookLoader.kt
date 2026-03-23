package alexsocol.patcher.asm

import alexsocol.asjlib.ASJReflectionHelper
import alexsocol.asjlib.asm.*
import alexsocol.patcher.PatcherPreConfigHandler
import alexsocol.patcher.asm.transformer.*
import alexsocol.patcher.asm.worker.InterfaceAppenderWorker
import com.KAIIIAK.KASMLib.KASMLib
import com.KAIIIAK.KASMLib.workers.ReflectionLikeWorker
import com.KAIIIAK.classManipulators.HookReplacerWorker
import com.KAIIIAK.superwrapper.SuperWrapperTransformer
import com.KAIIIAK.superwrapper.SuperWrapperTransformer.registerSuperWrapperContainer
import cpw.mods.fml.relauncher.*
import gloomyfolken.hooklib.minecraft.*
import gloomyfolken.hooklib.minecraft.MinecraftClassTransformer.registerPostTransformer
import net.minecraft.launchwrapper.*

// -Dfml.coreMods.load=alexsocol.patcher.asm.ASJHookLoader
// -username=AlexSocol
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions(
	"alexsocol.asjlib.asm",
	"alexsocol.patcher.asm.transformer",
	"com.KAIIIAK.asm",
	"com.KAIIIAK.classManipulators",
	"com.KAIIIAK.ignorer",
	"com.KAIIIAK.KASMLib",
	"com.KAIIIAK.nullsafety",
	"com.KAIIIAK.superwrapper",
	"gloomyfolken.hooklib",
	"kotlin",
)
class ASJHookLoader: HookLoader() {
	
	companion object {
		
		// may be used before #injectData so reflection -_-
		val OBF = ASJReflectionHelper.getStaticValue<CoreModManager, Boolean>(CoreModManager::class.java, "deobfuscatedEnvironment") != true
		
		init {
			KASMLib.has2DumpChangedClasses = System.getProperty("KASMlib.dumpChangedClasses").toBoolean()
			
			if (PatcherPreConfigHandler.allowLWJGLTransform) {
				fun allowLWJGLTransform(set: String) {
					ASJReflectionHelper.getValue<LaunchClassLoader, MutableSet<String>>(Launch.classLoader, set)?.apply {
						remove("org.lwjgl.")
						remove("org.lwjglx.")
					}
				}
				
				allowLWJGLTransform("classLoaderExceptions")
				allowLWJGLTransform("transformerExceptions")
			}
		}
	}
	
	override fun getASMTransformerClass(): Array<String> {
		return arrayOf(
			PrimaryClassTransformer::class.java.name,
			ASJASM::class.java.name,
			ASJGoto::class.java.name,
			ASJAccessTransformer::class.java.name,
			ASJClassTransformer::class.java.name,
			ASJPacketCompleter::class.java.name,
			RealmsDeleteTransformer::class.java.name,
			SpigotTransformer::class.java.name
		)
	}
	
	override fun registerHooks() {
		FMLRelaunchLog.info("[ASJLib] Loaded coremod. Registering hooks...")
		
		registerHookContainer("alexsocol.patcher.asm.hook.ASJHookHandler")
		registerHookContainer("alexsocol.patcher.asm.hook.BiomeDictionaryForWEHooks")
		registerHookContainer("alexsocol.patcher.asm.hook.NoEntityInteractionHandler")
		
		if (PatcherPreConfigHandler.topDownButtons) registerHookContainer("alexsocol.patcher.asm.hook.BlockButtonExtender")
		if (PatcherPreConfigHandler.deleteRealms) registerHookContainer("alexsocol.patcher.asm.hook.RealmsDeleter")
		
		registerHookContainer("alexsocol.patcher.asm.hook.ArmorFixes")
		
		if (OBF || System.getProperty("asjcore.fieldhooks", "false").toBoolean()) {
			ASJASM.registerFieldHookContainer("alexsocol.patcher.asm.hook.ASJFieldHookHandler")
			if (PatcherPreConfigHandler.optifinePostTransform) registerPostTransformer(OptiFinePostTransformer())
		}
		
		registerPostTransformer(KASMLib(false))
		registerPostTransformer(KASMLib(true))
		registerPostTransformer(SuperWrapperTransformer())
		registerSuperWrapperContainer("alexsocol.patcher.asm.hook.ASJSuperWrapperHandler")
		
		KASMLib.register(HookReplacerWorker.inst)
		KASMLib.register(InterfaceAppenderWorker)
		KASMLib.register(ReflectionLikeWorker.inst)
		
		HookReplacerWorker.registerHookReplacerContainer("alexsocol.patcher.asm.hook.ASJHookReplacerHandler") // java
		HookReplacerWorker.registerHookReplacerContainer("alexsocol.patcher.asm.hook.ASJHookReplacerHandlerKt") // kotlin
		
		if (PatcherPreConfigHandler.fixItemCollision) HookReplacerWorker.registerHookReplacerContainer("alexsocol.patcher.asm.hook.ItemCollisionFix")
		
		if (PatcherPreConfigHandler.fixCapeRotations) HookReplacerWorker.registerHookReplacerContainer("alexsocol.patcher.asm.hook.CapeRotationsFix")
		
		if (PatcherPreConfigHandler.deleteRealms) HookReplacerWorker.registerHookReplacerContainer("alexsocol.patcher.asm.hook.RealmsDeleterHR")
	}
}