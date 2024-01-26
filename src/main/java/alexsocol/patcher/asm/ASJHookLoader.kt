package alexsocol.patcher.asm

import alexsocol.asjlib.ASJReflectionHelper
import alexsocol.asjlib.asm.*
import alexsocol.patcher.PatcherConfigHandler
import alexsocol.patcher.asm.transformer.*
import com.KAIIIAK.superwrapper.SuperWrapperTransformer
import com.KAIIIAK.superwrapper.SuperWrapperTransformer.*
import cpw.mods.fml.relauncher.*
import gloomyfolken.hooklib.minecraft.*
import gloomyfolken.hooklib.minecraft.MinecraftClassTransformer.registerPostTransformer
import java.io.File

// -Dfml.coreMods.load=alexsocol.patcher.asm.ASJHookLoader
// -username=AlexSocol
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions("alexsocol.patcher.asm.transformer", "alexsocol.asjlib.asm", "gloomyfolken.hooklib", "kotlin")
class ASJHookLoader: HookLoader() {
	
	companion object {
		
		val OBF = ASJReflectionHelper.getStaticValue<CoreModManager, Boolean>(CoreModManager::class.java, "deobfuscatedEnvironment") != true
		
		init {
			PatcherConfigHandler.loadConfig(File("config/ASJCore.cfg"))
		}
	}
	
	override fun getASMTransformerClass(): Array<String> {
		return arrayOf(PrimaryClassTransformer::class.java.name, ASJASM::class.java.name, ASJGoto::class.java.name, ASJClassTransformer::class.java.name, ASJPacketCompleter::class.java.name)
	}
	
	override fun registerHooks() {
		FMLRelaunchLog.info("[ASJLib] Loaded coremod. Registering hooks...")
		
		registerHookContainer("alexsocol.patcher.asm.ASJHookHandler")
		registerHookContainer("alexsocol.patcher.asm.BiomeDictionaryForWEHooks")
		
		if (PatcherConfigHandler.topDownButtons) registerHookContainer("alexsocol.patcher.asm.BlockButtonExtender")
		
		if (OBF || System.getProperty("asjcore.fieldhooks", "false").toBoolean()) {
			ASJASM.registerFieldHookContainer("alexsocol.patcher.asm.ASJFieldHookHandler")
			if (PatcherConfigHandler.optifinePostTransform) registerPostTransformer(OptiFinePostTransformer())
		}
		
		registerPostTransformer(SuperWrapperTransformer())
		registerSuperWrapperContainer("alexsocol.patcher.asm.ASJSuperWrapperHandler")
	}
}