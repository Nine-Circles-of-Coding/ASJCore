package alexsocol.patcher

import alexsocol.asjlib.extendables.ASJConfigHandler
import net.minecraftforge.common.config.Configuration.*

object PatcherConfigHandler: ASJConfigHandler() {
	
	const val CATEGORY_DANGER = CATEGORY_GENERAL + CATEGORY_SPLITTER + "dangerzone"
	const val CATEGORY_INTEGRATION = CATEGORY_GENERAL + CATEGORY_SPLITTER + "integration"
	
	var addAir = false
	var addBlocks = false
	var textIDs = false
	
	var clearWater = true
	var commands = PatcherMain.Commands.entries.map { it.name }.toTypedArray()
	var creativeDamage = false
	var damageMobArmor = true
	var darkMode = true
	var entityGravityFix = true
	var explosions = true
	var floatingTrapDoors = true
	var flyFastDig = true
	var lightningID = 150
	var maxParticles = 4000
	var portalHook = true
	var showOreDict = true
	var vignette = false
	var voidFog = true
	var WEBiomeID = 150
	
	var blacklistWither = true
	var optifinePostTransform = true
	
	override fun addCategories() {
		addCategory(CATEGORY_DANGER, "[WARNING!] Backup your world before changing something here!")
	}
	
	override fun readProperties() {
		addAir = loadProp(CATEGORY_DANGER, "addAir", addAir, true, "Set this to true to add air item")
		addBlocks = loadProp(CATEGORY_DANGER, "addBlocks", addBlocks, true, "Set this to true to add items for technical blocks")
		textIDs = loadProp(CATEGORY_DANGER, "textIDs", textIDs, false, "Set this to true to enable text item IDs instead of numeric for storing in NBT")
		
		clearWater = loadProp(CATEGORY_GENERAL, "clearWater", clearWater, false, "Set this to true for clear, transparent water")
		commands = loadProp(CATEGORY_GENERAL, "commands", commands, true, "List of commands. Remove any to unregister it. DO NOT add anything new", false)
		creativeDamage = loadProp(CATEGORY_GENERAL, "creativeDamage", creativeDamage, false, "Set this to true to allow taking damage in creative")
		damageMobArmor = loadProp(CATEGORY_GENERAL, "damageMobArmor", damageMobArmor, false, "Set this to false to prevent mob armor getting destroyed from attacks")
		darkMode = loadProp(CATEGORY_GENERAL, "darkMode", darkMode, true, "Set this to false to disable dark mode on minecraft load")
		entityGravityFix = loadProp(CATEGORY_GENERAL, "entityGravityFix", entityGravityFix, false, "Set this to false to disable gravity fix")
		explosions = loadProp(CATEGORY_GENERAL, "explosions", explosions, false, "Set this to false to disable explosions")
		floatingTrapDoors = loadProp(CATEGORY_GENERAL, "floatingTrapDoors", floatingTrapDoors, true, "Set this to false to forbid trapdoors to remain free-floating (as in vanilla, may break some world structures)")
		flyFastDig = loadProp(CATEGORY_GENERAL, "flyFastDig", flyFastDig, false, "Set this to false to make block break speed 5 times slower when flying with creative-like flight not in creative")
		lightningID = loadProp(CATEGORY_GENERAL, "lightningID", lightningID, true, "ID for lightning bolt entity")
		maxParticles = loadProp(CATEGORY_GENERAL, "maxParticles", maxParticles, true, "How many [any] particles can there be at one time (defaults to vanilla value)")
		portalHook = loadProp(CATEGORY_GENERAL, "portalHook", portalHook, false, "Set this to true to disable closing GUI when entering nether portal")
		showOreDict = loadProp(CATEGORY_GENERAL, "showOreDict", showOreDict, false, "Set this to true to show all oredict names in item tooltip when holding SHIFT")
		vignette = loadProp(CATEGORY_GENERAL, "vignette", vignette, false, "Set this to true to enable vignette on fancy graphics")
		voidFog = loadProp(CATEGORY_GENERAL, "voidFog", voidFog, false, "Set this to false to disable void fog")
		WEBiomeID = loadProp(CATEGORY_GENERAL, "WEBiomeID", WEBiomeID, true, "ID for standard WorldEngine biome")
		
		blacklistWither = loadProp(CATEGORY_INTEGRATION, "NEI.blacklistWither", blacklistWither, true, "Set this to false to make Wither spawner visible")
		optifinePostTransform = loadProp(CATEGORY_INTEGRATION, "OF.optifinePostTransform", optifinePostTransform, true, "Set this to false to disable modifications to optifine some code. May break or repair render bugs.")
	}
}