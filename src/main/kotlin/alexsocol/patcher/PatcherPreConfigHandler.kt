package alexsocol.patcher

import alexsocol.asjlib.extendables.ASJPreConfigHandler
import net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL
import net.minecraftforge.common.config.Configuration.CATEGORY_SPLITTER
import java.io.File

object PatcherPreConfigHandler: ASJPreConfigHandler() {
	
	const val CATEGORY_DANGER = CATEGORY_GENERAL + CATEGORY_SPLITTER + "dangerzone"
	const val CATEGORY_DKC = CATEGORY_GENERAL + CATEGORY_SPLITTER + "derkatercore"
	const val CATEGORY_INTEGRATION = CATEGORY_GENERAL + CATEGORY_SPLITTER + "integration"
	
	var allowLWJGLTransform = true
	
	var deleteRealms = true
	var fixCapeRotations = true
	var fixItemCollision = true
	var logDebug = true
	var logTrace = false
	var tickrateHooks = true
	var topDownButtons = true
	var transformersForHookReplacerBlacklist = arrayOf(
		"." // everything until KAIIIAK makes isolated classloader for this

//		"Reika.", // whatever
//		"am2.preloader.", // dumbass -_-
	)
	
	// derkatercore
	var allPublic = false
	var ignoredClasses = arrayOf(
		"org.spigotmc.SpigotConfig" // motherfucking shit eaters (c) KAIIIAK
	)
	
	// integration
	var optifinePostTransform = true
	
	init {
		loadPreConfig(File("config/ASJCore/core.cfg"))
	}
	
	override fun addCategories() {
		addCategory(CATEGORY_DANGER, "[WARNING!] Backup your world before changing something here!")
		addCategory(CATEGORY_DKC, "Configs of derkatercore")
		addCategory(CATEGORY_INTEGRATION, "Cross-mods integration")
	}
	
	override fun readProperties() {
		allowLWJGLTransform = loadProp(CATEGORY_DANGER, "allowLWJGLTransform", allowLWJGLTransform, true, "Allows transforming LWJGL classes by changing class loader. Turn this off if you have any visual issues")
		
		deleteRealms = loadProp(CATEGORY_GENERAL, "deleteRealms", deleteRealms, true, "Set this to false to disable realms deletion")
		fixCapeRotations = loadProp(CATEGORY_GENERAL, "fixCapeRotations", fixCapeRotations, true, "Set this to false to disable limits for cape rotations")
		fixItemCollision = loadProp(CATEGORY_GENERAL, "fixItemCollision", fixItemCollision, true, "Set this to false to disable item collision on complex blocks fix")
		logDebug = loadProp(CATEGORY_GENERAL, "logDebug", logDebug, false, "Set this to false to disable debug logging")
		logTrace = loadProp(CATEGORY_GENERAL, "logTrace", logTrace, false, "Set this to true to enable thorough logging")
		tickrateHooks = loadProp(CATEGORY_GENERAL, "tickrateHooks", tickrateHooks, true, "Set this to false to disable tickrate modifications")
		topDownButtons = loadProp(CATEGORY_GENERAL, "topDownButtons", topDownButtons, true, "Set this to false to disable functionality allowing buttons to be placed on block top or bottom")
		transformersForHookReplacerBlacklist = loadProp(CATEGORY_GENERAL, "transformersForHookReplacerBlacklist", transformersForHookReplacerBlacklist, true, "Add here problematic transformers that cause issues on HookReplacers registration", false)
		
		allPublic = loadProp(CATEGORY_DKC, "allPublic", allPublic, true, "Set this to true to make all fields and functions public")
		ignoredClasses = loadProp(CATEGORY_DKC, "ignoredClasses", ignoredClasses, true, "Array of classes or packages (or just parts) that won't be touched when making everything public", false)
		
		optifinePostTransform = loadProp(CATEGORY_INTEGRATION, "OF.optifinePostTransform", optifinePostTransform, true, "Set this to false to disable modifications to glass panes visual connections to other blocks changes made by optifine. May break or fix render bugs.")
	}
}