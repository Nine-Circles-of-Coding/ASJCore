package alexsocol.patcher.asm

import alexsocol.asjlib.ASJReflectionHelper
import alexsocol.asjlib.asm.*
import alexsocol.patcher.PatcherConfigHandler
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
import java.io.File

// -Dfml.coreMods.load=alexsocol.patcher.asm.ASJHookLoader
// -username=AlexSocol
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions(
	"alexsocol.patcher.asm.transformer",
	"alexsocol.asjlib.asm",
	"alexsocol.patcher.asm.transformer",
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
			PatcherConfigHandler.loadConfig(File("config/ASJCore.cfg"))
//			KASMLib.has2DumpChangedClasses = true
			
			if (PatcherConfigHandler.allowLWJGLTransform)
				ASJReflectionHelper.getValue<LaunchClassLoader, MutableSet<String>>(Launch.classLoader, "classLoaderExceptions")?.remove("org.lwjgl.")
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
		
		if (PatcherConfigHandler.topDownButtons) registerHookContainer("alexsocol.patcher.asm.hook.BlockButtonExtender")
		if (PatcherConfigHandler.deleteRealms) registerHookContainer("alexsocol.patcher.asm.hook.RealmsDeleter")
		
		registerHookContainer("alexsocol.patcher.asm.hook.ArmorFixes")
		
		if (OBF || System.getProperty("asjcore.fieldhooks", "false").toBoolean()) {
			ASJASM.registerFieldHookContainer("alexsocol.patcher.asm.hook.ASJFieldHookHandler")
			if (PatcherConfigHandler.optifinePostTransform) registerPostTransformer(OptiFinePostTransformer())
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
		
		if (PatcherConfigHandler.fixItemCollision) HookReplacerWorker.registerHookReplacerContainer("alexsocol.patcher.asm.hook.ItemCollisionFix")
		
		if (PatcherConfigHandler.fixCapeRotations) HookReplacerWorker.registerHookReplacerContainer("alexsocol.patcher.asm.hook.CapeRotationsFix")
		
		if (PatcherConfigHandler.deleteRealms) HookReplacerWorker.registerHookReplacerContainer("alexsocol.patcher.asm.hook.RealmsDeleterHR")
	}
}