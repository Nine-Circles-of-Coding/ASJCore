package alexsocol.patcher

import alexsocol.asjlib.extendables.ASJConfigHandler
import alexsocol.patcher.PatcherPreConfigHandler.CATEGORY_DANGER
import alexsocol.patcher.PatcherPreConfigHandler.CATEGORY_INTEGRATION
import net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL
import java.io.File

object PatcherConfigHandler: ASJConfigHandler() {
	
	var addAir = false
	var addBlocks = false
	var textIDs = false
	
	var anvilLevelLimit = Int.MAX_VALUE
	var bucketSounds = true
	var burningZombieChildren = true
	var c17PacketCustomPayloadUnlimit = true
	var cacheSchemas = true
	var clampPotionLevel = true
	var creativeDamage = false
	var damageMobArmor = true
	var darkMode = true
	var disableStats = false
	var eggs = true
	var endlessSprint = true
	var entityGravityFix = true
	var everythingIsScrewedUpMode = false
	var explosions = true
	var fixInGameModOptions = true
	var floatingTrapDoors = true
	var flyFastDig = true
	var ignoreIllegalStates = false
	var langsRamOptimization = true
	var lightningID = 150
	var maxParticles = 4000
	var orthoProjection = false
	var portalHook = true
	var removeStreamKeys = true
	var rescaleProfiler = true
	var respawnInEnd = false
	var respawnInNether = false
	var ridingHandRotationDisable = false
	var showNbt = true
	var showOreDict = true
	var showRegName = true
	var slotIndices = false
	var tcpNoDelay = true
	var vignette = false
	var WEBiomeID = 40
	var WECustomLighting = true
	var xpCooldown = 0
	
	var blacklistWither = true
	
	init {
		loadConfig(File("config/ASJCore/mod.cfg"))
	}
	
	override fun addCategories() {
		addCategory(CATEGORY_DANGER, "[WARNING!] Backup your world before changing something here!")
		addCategory(CATEGORY_INTEGRATION, "Cross-mods integration")
	}
	
	override fun readProperties() {
		addAir = loadProp(CATEGORY_DANGER, "addAir", addAir, true, "Set this to true to add air item")
		addBlocks = loadProp(CATEGORY_DANGER, "addBlocks", addBlocks, true, "Set this to true to add items for technical blocks")
		textIDs = loadProp(CATEGORY_DANGER, "textIDs", textIDs, false, "Set this to true to enable text item IDs instead of numeric for storing in NBT")
		
		anvilLevelLimit = loadProp(CATEGORY_GENERAL, "anvilLevelLimit", anvilLevelLimit, false, "Max level cost for anvil use (default 40)", 40, Int.MAX_VALUE)
		bucketSounds = loadProp(CATEGORY_GENERAL, "bucketSounds", bucketSounds, false, "Set this to false to disable sounds from buckets")
		burningZombieChildren = loadProp(CATEGORY_GENERAL, "burningZombieChildren", burningZombieChildren, false, "Set this to false to disable zombie children burning from sun")
		c17PacketCustomPayloadUnlimit = loadProp(CATEGORY_GENERAL, "c17PacketCustomPayloadUnlimit", c17PacketCustomPayloadUnlimit, true, "Set this to false to disable C17PacketCustomPayload expansion")
		cacheSchemas = loadProp(CATEGORY_GENERAL, "cacheSchemas", cacheSchemas, false, "Set this to false to disable schemas caching - it will save your RAM but worldgen will take longer")
		clampPotionLevel = loadProp(CATEGORY_GENERAL, "clampPotionLevel", clampPotionLevel, false, "Set this to false to disable potion level clamping to 0..255 (you almost never want this disabled)")
		creativeDamage = loadProp(CATEGORY_GENERAL, "creativeDamage", creativeDamage, false, "Set this to true to allow taking damage in creative")
		damageMobArmor = loadProp(CATEGORY_GENERAL, "damageMobArmor", damageMobArmor, false, "Set this to false to prevent mob armor getting destroyed from attacks")
		darkMode = loadProp(CATEGORY_GENERAL, "darkMode", darkMode, true, "Set this to false to disable dark mode on minecraft load")
		disableStats = loadProp(CATEGORY_GENERAL, "disableStats", disableStats, true, "Set this to false to re-enable stats collection")
		eggs = loadProp(CATEGORY_GENERAL, "eggs", eggs, true, "Set this to false to not add eggs for snow and iron golems, giant, ender dragon, wither")
		endlessSprint = loadProp(CATEGORY_GENERAL, "endlessSprint", endlessSprint, false, "Set this to true to remove sprinting cancellation after 30 seconds")
		entityGravityFix = loadProp(CATEGORY_GENERAL, "entityGravityFix", entityGravityFix, false, "Set this to false to disable gravity fix")
		everythingIsScrewedUpMode = loadProp(CATEGORY_GENERAL, "everythingIsScrewedUpMode", everythingIsScrewedUpMode, false, "Set this to true to enable: the player spins around slowly around (0, 68.5, 0). The GUI is disabled, the view is set to first person, and both chat and menu are disabled. Unless the server is configured to ignore illegal stances, attempting to enter a world at all will result in an immediate kick due to an illegal stance.")
		explosions = loadProp(CATEGORY_GENERAL, "explosions", explosions, false, "Set this to false to disable explosions")
		fixInGameModOptions = loadProp(CATEGORY_GENERAL, "fixInGameModOptions", fixInGameModOptions, false, "Set this to false to disable in-game mod options gui fix")
		floatingTrapDoors = loadProp(CATEGORY_GENERAL, "floatingTrapDoors", floatingTrapDoors, true, "Set this to false to forbid trapdoors to remain free-floating (as in vanilla, may break some world structures)")
		flyFastDig = loadProp(CATEGORY_GENERAL, "flyFastDig", flyFastDig, false, "Set this to false to make block break speed 5 times slower when flying with creative-like flight not in creative")
		ignoreIllegalStates = loadProp(CATEGORY_GENERAL, "ignoreIllegalStates", ignoreIllegalStates, false, "Set this to true to ignore illegal player states (players won't be kicked)")
		langsRamOptimization = loadProp(CATEGORY_GENERAL, "langsRamOptimization", langsRamOptimization, true, "Set this to false to disable langs RAM optimization (if some lang is not loaded properly)")
		lightningID = loadProp(CATEGORY_GENERAL, "lightningID", lightningID, true, "ID for lightning bolt entity")
		maxParticles = loadProp(CATEGORY_GENERAL, "maxParticles", maxParticles, true, "How many [any] particles can there be at one time (defaults to vanilla value)")
		orthoProjection = loadProp(CATEGORY_GENERAL, "orthoProjection", orthoProjection, false, "Set this to true to enable orthographic projection (no perspective)")
		portalHook = loadProp(CATEGORY_GENERAL, "portalHook", portalHook, false, "Set this to true to disable closing GUI when entering nether portal")
		removeStreamKeys = loadProp(CATEGORY_GENERAL, "removeStreamKeys", removeStreamKeys, true, "Set this to false to keep useless streamer keys (compat with ReBind mod)")
		rescaleProfiler = loadProp(CATEGORY_GENERAL, "rescaleProfiler", rescaleProfiler, false, "Set this to false to keep profiler HUD not scalable with GUI scale setting")
		respawnInEnd = loadProp(CATEGORY_GENERAL, "respawnInEnd", respawnInEnd, false, "Set this to true to allow respawning in the end")
		respawnInNether = loadProp(CATEGORY_GENERAL, "respawnInNether", respawnInNether, false, "Set this to true to allow respawning in the nether")
		ridingHandRotationDisable = loadProp(CATEGORY_GENERAL, "ridingHandRotationDisable", ridingHandRotationDisable, false, "Set this to true to disable FPV hand rotation while riding")
		showNbt = loadProp(CATEGORY_GENERAL, "showNbt", showNbt, false, "Set this to false to not show stack NBT in item tooltip when holding SHIFT AND when advanced tooltips are enabled (F3+H)")
		showOreDict = loadProp(CATEGORY_GENERAL, "showOreDict", showOreDict, false, "Set this to false to not show oredict names in item tooltip when holding SHIFT")
		showRegName = loadProp(CATEGORY_GENERAL, "showRegName", showRegName, false, "Set this to false to not show registry name in item tooltip when holding SHIFT")
		slotIndices = loadProp(CATEGORY_GENERAL, "slotIndices", slotIndices, false, "Set this to true to display slot indices and slot numbers in inventories")
		tcpNoDelay = loadProp(CATEGORY_GENERAL, "tcpNoDelay", tcpNoDelay, true, "Set this to false to disable TCP No Delay network feature")
		vignette = loadProp(CATEGORY_GENERAL, "vignette", vignette, false, "Set this to true to enable vignette on fancy graphics")
		WEBiomeID = loadProp(CATEGORY_GENERAL, "WEBiomeID", WEBiomeID, true, "ID for standard WorldEngine biome")
		WECustomLighting = loadProp(CATEGORY_GENERAL, "WECustomLighting", WECustomLighting, false, "Set this to false to use default lighting calculation for WE worlds")
		xpCooldown = loadProp(CATEGORY_GENERAL, "xpCooldown", xpCooldown, false, "Ticks between two xp orb pickup (default 2)", 0)
		
		blacklistWither = loadProp(CATEGORY_INTEGRATION, "NEI.blacklistWither", blacklistWither, true, "Set this to false to make Wither spawner visible")
	}
}