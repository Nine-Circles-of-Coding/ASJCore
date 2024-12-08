package alexsocol.patcher

import alexsocol.asjlib.extendables.ASJConfigHandler
import net.minecraftforge.common.config.Configuration.*

object PatcherConfigHandler: ASJConfigHandler() {
	
	const val CATEGORY_DANGER = CATEGORY_GENERAL + CATEGORY_SPLITTER + "dangerzone"
	const val CATEGORY_DKC = CATEGORY_GENERAL + CATEGORY_SPLITTER + "derkatercore"
	const val CATEGORY_INTEGRATION = CATEGORY_GENERAL + CATEGORY_SPLITTER + "integration"
	
	var addAir = false
	var addBlocks = false
	var textIDs = false
	
	var bucketSounds = true
	var c17PacketCustomPayloadUnlimit = true
	var cacheSchemas = true
	var clampPotionLevel = true
	var clearWater = true
	var creativeDamage = false
	var damageMobArmor = true
	var darkMode = true
	var endlessSprint = true
	var entityGravityFix = true
	var everythingIsScrewedUpMode = false
	var explosions = true
	var floatingTrapDoors = true
	var flyFastDig = true
	var ignoreIllegalStates = false
	var lightningID = 150
	var logDebug = true
	var logTrace = false
	var maxParticles = 4000
	var portalHook = true
	var respawnInEnd = true
	var respawnInNether = true
	var showNbt = true
	var showOreDict = true
	var showRegName = true
	var tcpNoDelay = true
	var topDownButtons = true
	var vignette = false
	var voidFog = true
	var WEBiomeID = 40
	var WECustomLighting = true
	
	var allPublic = false
	var ignoredClasses = arrayOf(
		"org.spigotmc.SpigotConfig" // motherfucking shit eaters (c) KAIIIAK
	)
	
	var blacklistWither = true
	var optifinePostTransform = true
	
	override fun addCategories() {
		addCategory(CATEGORY_DANGER, "[WARNING!] Backup your world before changing something here!")
	}
	
	override fun readProperties() {
		addAir = loadProp(CATEGORY_DANGER, "addAir", addAir, true, "Set this to true to add air item")
		addBlocks = loadProp(CATEGORY_DANGER, "addBlocks", addBlocks, true, "Set this to true to add items for technical blocks")
		textIDs = loadProp(CATEGORY_DANGER, "textIDs", textIDs, false, "Set this to true to enable text item IDs instead of numeric for storing in NBT")
		
		bucketSounds = loadProp(CATEGORY_GENERAL, "bucketSounds", bucketSounds, false, "Set this to false to disable sounds from buckets")
		c17PacketCustomPayloadUnlimit = loadProp(CATEGORY_GENERAL, "c17PacketCustomPayloadUnlimit", c17PacketCustomPayloadUnlimit, true, "Set this to false to disable C17PacketCustomPayload expansion")
		cacheSchemas = loadProp(CATEGORY_GENERAL, "cacheSchemas", cacheSchemas, false, "Set this to false to disable schemas caching - it will save your RAM but worldgen will take longer")
		clampPotionLevel = loadProp(CATEGORY_GENERAL, "clampPotionLevel", clampPotionLevel, false, "Set this to false to disable potion level clamping to 0..255 (you almost never want this disabled)")
		clearWater = loadProp(CATEGORY_GENERAL, "clearWater", clearWater, false, "Set this to true for clear, transparent water (will be overridden by OptiFine setting)")
		creativeDamage = loadProp(CATEGORY_GENERAL, "creativeDamage", creativeDamage, false, "Set this to true to allow taking damage in creative")
		damageMobArmor = loadProp(CATEGORY_GENERAL, "damageMobArmor", damageMobArmor, false, "Set this to false to prevent mob armor getting destroyed from attacks")
		darkMode = loadProp(CATEGORY_GENERAL, "darkMode", darkMode, true, "Set this to false to disable dark mode on minecraft load")
		endlessSprint = loadProp(CATEGORY_GENERAL, "endlessSprint", endlessSprint, false, "Set this to true to remove sprinting cancellation after 30 seconds")
		entityGravityFix = loadProp(CATEGORY_GENERAL, "entityGravityFix", entityGravityFix, false, "Set this to false to disable gravity fix")
		everythingIsScrewedUpMode = loadProp(CATEGORY_GENERAL, "everythingIsScrewedUpMode", everythingIsScrewedUpMode, false, "Set this to true to enable: the player spins around slowly around (0, 68.5, 0). The GUI is disabled, the view is set to first person, and both chat and menu are disabled. Unless the server is configured to ignore illegal stances, attempting to enter a world at all will result in an immediate kick due to an illegal stance.")
		explosions = loadProp(CATEGORY_GENERAL, "explosions", explosions, false, "Set this to false to disable explosions")
		floatingTrapDoors = loadProp(CATEGORY_GENERAL, "floatingTrapDoors", floatingTrapDoors, true, "Set this to false to forbid trapdoors to remain free-floating (as in vanilla, may break some world structures)")
		flyFastDig = loadProp(CATEGORY_GENERAL, "flyFastDig", flyFastDig, false, "Set this to false to make block break speed 5 times slower when flying with creative-like flight not in creative")
		ignoreIllegalStates = loadProp(CATEGORY_GENERAL, "ignoreIllegalStates", ignoreIllegalStates, false, "Set this to true to ignore illegal player states (players won't be kicked)")
		lightningID = loadProp(CATEGORY_GENERAL, "lightningID", lightningID, true, "ID for lightning bolt entity")
		logDebug = loadProp(CATEGORY_GENERAL, "logDebug", logDebug, false, "Set this to false to disable debug logging")
		logTrace = loadProp(CATEGORY_GENERAL, "logTrace", logTrace, false, "Set this to true to enable thorough logging")
		maxParticles = loadProp(CATEGORY_GENERAL, "maxParticles", maxParticles, true, "How many [any] particles can there be at one time (defaults to vanilla value)")
		portalHook = loadProp(CATEGORY_GENERAL, "portalHook", portalHook, false, "Set this to true to disable closing GUI when entering nether portal")
		respawnInEnd = loadProp(CATEGORY_GENERAL, "respawnInEnd", respawnInEnd, false, "Set this to true to allow respawning in the edn")
		respawnInNether = loadProp(CATEGORY_GENERAL, "respawnInNether", respawnInNether, false, "Set this to true to allow respawning in the nether")
		showNbt = loadProp(CATEGORY_GENERAL, "showNbt", showNbt, false, "Set this to false to not show stack NBT in item tooltip when holding SHIFT AND when advanced tooltips are enabled (F3+H)")
		showOreDict = loadProp(CATEGORY_GENERAL, "showOreDict", showOreDict, false, "Set this to false to not show oredict names in item tooltip when holding SHIFT")
		showRegName = loadProp(CATEGORY_GENERAL, "showRegName", showRegName, false, "Set this to false to not show registry name in item tooltip when holding SHIFT")
		tcpNoDelay = loadProp(CATEGORY_GENERAL, "tcpNoDelay", tcpNoDelay, true, "Set this to false to disable TCP No Delay network feature")
		topDownButtons = loadProp(CATEGORY_GENERAL, "topDownButtons", topDownButtons, true, "Set this to false to disable functionality allowing buttons to be placed on block top or bottom")
		vignette = loadProp(CATEGORY_GENERAL, "vignette", vignette, false, "Set this to true to enable vignette on fancy graphics")
		voidFog = loadProp(CATEGORY_GENERAL, "voidFog", voidFog, false, "Set this to false to disable void fog (will be overridden by OptiFine setting)")
		WEBiomeID = loadProp(CATEGORY_GENERAL, "WEBiomeID", WEBiomeID, true, "ID for standard WorldEngine biome")
		WECustomLighting = loadProp(CATEGORY_GENERAL, "WECustomLighting", WECustomLighting, false, "Set this to false to use default lighting calculation for WE worlds")
		
		allPublic = loadProp(CATEGORY_DKC, "allPublic", allPublic, true, "Set this to true to make all fields and functions public")
		ignoredClasses = loadProp(CATEGORY_DKC, "ignoredClasses", ignoredClasses, true, "Array of classes or packages (or just parts) that won't be touched when making everything public", false)
		
		blacklistWither = loadProp(CATEGORY_INTEGRATION, "NEI.blacklistWither", blacklistWither, true, "Set this to false to make Wither spawner visible")
		optifinePostTransform = loadProp(CATEGORY_INTEGRATION, "OF.optifinePostTransform", optifinePostTransform, true, "Set this to false to disable modifications to glass panes visual connections to other blocks changes made by optifine. May break or fix render bugs.")
	}
}