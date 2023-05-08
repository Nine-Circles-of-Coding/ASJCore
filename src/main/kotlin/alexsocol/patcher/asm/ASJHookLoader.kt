package alexsocol.patcher.asm

import alexsocol.asjlib.ASJReflectionHelper
import alexsocol.asjlib.asm.*
import alexsocol.patcher.PatcherConfigHandler
import cpw.mods.fml.relauncher.*
import gloomyfolken.hooklib.minecraft.*
import java.io.File

// -Dfml.coreMods.load=alexsocol.patcher.asm.ASJHookLoader
@IFMLLoadingPlugin.MCVersion("1.7.10")
class ASJHookLoader: HookLoader() {
	
	companion object {
		
		val OBF = ASJReflectionHelper.getStaticValue<CoreModManager, Boolean>(CoreModManager::class.java, "deobfuscatedEnvironment") != true
		
		init {
			PatcherConfigHandler.loadConfig(File("config/ASJCore.cfg"))
		}
	}
	
	override fun getASMTransformerClass(): Array<String> {
		return arrayOf(PrimaryClassTransformer::class.java.name, ASJASM::class.java.name, ASJClassTransformer::class.java.name, ASJPacketCompleter::class.java.name)
	}
	
	override fun registerHooks() {
		FMLRelaunchLog.info("[ASJLib] Loaded coremod. Registering hooks...")
		
		registerHookContainer("alexsocol.patcher.asm.ASJHookHandler")
		
		if (OBF) ASJASM.registerFieldHookContainer("alexsocol.patcher.asm.ASJFieldHookHandler")
	}
}