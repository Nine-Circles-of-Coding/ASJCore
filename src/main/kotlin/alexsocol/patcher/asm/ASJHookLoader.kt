package alexsocol.patcher.asm

import alexsocol.asjlib.*
import alexsocol.asjlib.asm.*
import alexsocol.patcher.*
import alexsocol.patcher.asm.transformer.*
import alexsocol.patcher.asm.worker.*
import com.KAIIIAK.KASMLib.*
import com.KAIIIAK.KASMLib.workers.*
import com.KAIIIAK.callProxy.CallProxyLogic
import com.KAIIIAK.classManipulators.*
import com.KAIIIAK.classManipulators.HookReplacerWorker.*
import com.KAIIIAK.superwrapper.*
import com.KAIIIAK.superwrapper.SuperWrapperTransformer.*
import cpw.mods.fml.relauncher.*
import gloomyfolken.hooklib.minecraft.*
import gloomyfolken.hooklib.minecraft.MinecraftClassTransformer.*
import net.minecraft.launchwrapper.*
import kotlin.jvm.java

// -Dfml.coreMods.load=alexsocol.patcher.asm.ASJHookLoader
// -username=AlexSocol
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions(
	"alexsocol.asjlib.asm",
	"alexsocol.patcher.asm.transformer",
	"com.KAIIIAK.asm",
	"com.KAIIIAK.callProxy",
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
			KASMLib.has2DumpUnchangedClasses = System.getProperty("KASMlib.dumpUnChangedClasses").toBoolean()
			
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
		val classes = mutableListOf<String>(
			PrimaryClassTransformer::class.java.name,
			ASJASM::class.java.name,
			ASJGoto::class.java.name,
			ASJAccessTransformer::class.java.name,
			ASJClassTransformer::class.java.name,
			ASJPacketCompleter::class.java.name,
			CallProxyLogic::class.java.name,
			RealmsDeleteTransformer::class.java.name,
			SpigotTransformer::class.java.name,
		)
		
		if (!OBF) classes.add(ASJAccessTransformerDev::class.java.name)
		
		return classes.toTypedArray()
	}
	
	override fun registerHooks() {
		FMLRelaunchLog.info("[ASJLib] Loaded coremod. Registering hooks...")
		
		registerHookContainer("alexsocol.patcher.asm.hook.ASJHookHandler")
		// BiomeDictionaryForWEHooks is mixins.asjlib.json now.
		// NoEntityInteractionHandler is mixins.asjlib.json/client now.
		registerHookContainer("alexsocol.patcher.asm.hook.ReachDistanceHooks")
		
		// BlockButtonExtender is mixins.asjlib.json + .late.json now; both check topDownButtons.
		// RealmsDeleter is mixins.asjlib.json/client now; it checks deleteRealms itself.
		
		// ArmorFixes is mixins.asjlib.late.json now, gated by ASJLateMixins.
		
		// ASJCore_host and WorldEngine_SubBiomeList are added by MixinFoodStats/MixinChunk now, in
		// both dev and production, so there is no field hook container left to register here.
		// ASJASM.registerFieldHookContainer stays available for downstream mods.
		if (OBF || System.getProperty("asjcore.fieldhooks", "false").toBoolean()) {
			if (PatcherPreConfigHandler.optifinePostTransform) registerPostTransformer(OptiFinePostTransformer())
		}
		
		registerPostTransformer(KASMLib(false))
		registerPostTransformer(KASMLib(true))
		registerPostTransformer(SuperWrapperTransformer())
		registerPostTransformer(HookReplacerWorker())
		registerSuperWrapperContainer("alexsocol.patcher.asm.hook.ASJSuperWrapperHandler")
		
		KASMLib.register(InterfaceAppenderWorker)
		KASMLib.register(ReflectionLikeWorker.inst)
		
		registerGroupRegistry("alexsocol.patcher.asm.hook.ASJHookReplacerHandler") // java
		registerGroupRegistry("alexsocol.patcher.asm.hook.ASJHookReplacerHandlerKt") // kotlin
		registerHookReplacerContainer("alexsocol.patcher.asm.hook.ASJHookReplacerHandler") // java
		registerHookReplacerContainer("alexsocol.patcher.asm.hook.ASJHookReplacerHandlerKt") // kotlin
		registerHookReplacerContainer("alexsocol.patcher.asm.hook.ReachDistanceHooks")
		
		if (PatcherPreConfigHandler.fixItemCollision) registerHookReplacerContainer("alexsocol.patcher.asm.hook.ItemCollisionFix")
		if (PatcherPreConfigHandler.fixCapeRotations) registerHookReplacerContainer("alexsocol.patcher.asm.hook.CapeRotationsFix")
		if (PatcherPreConfigHandler.deleteRealms) registerHookReplacerContainer("alexsocol.patcher.asm.hook.RealmsDeleterHR")
		if (PatcherPreConfigHandler.tickrateHooks) {
			registerGroupRegistry("alexsocol.patcher.asm.hook.TickrateKt")
			registerHookReplacerContainer("alexsocol.patcher.asm.hook.TickrateKt")
		}
	}
}