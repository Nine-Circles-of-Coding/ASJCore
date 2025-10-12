package alexsocol.patcher

import alexsocol.asjlib.*
import alexsocol.asjlib.command.*
import alexsocol.asjlib.render.ASJShaderHelper
import alexsocol.patcher.PatcherMain.MODID
import alexsocol.patcher.asm.ASJHookLoader
import alexsocol.patcher.asm.hook.NoEntityInteractionHandler
import alexsocol.patcher.crafting.CraftingHandler
import alexsocol.patcher.event.*
import alexsocol.patcher.handler.*
import alexsocol.patcher.network.NetworkHandler
import cpw.mods.fml.client.config.GuiUtils
import cpw.mods.fml.common.*
import cpw.mods.fml.common.event.*
import cpw.mods.fml.common.registry.GameData
import net.minecraft.block.*
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.util.Facing
import net.minecraft.world.biome.BiomeGenBase
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import ru.vamig.worldengine.WE_Biome

@Mod(modid = MODID, useMetadata = true, guiFactory = "alexsocol.patcher.client.GUIFactory", modLanguageAdapter = KotlinAdapter.className)
object PatcherMain {
	
	const val MODID = "asjpatcher"
	
	@Mod.Metadata(MODID)
	lateinit var meta: ModMetadata
	
	@Mod.EventHandler
	fun construct(e: FMLConstructionEvent) {
		PatcherConfigHandler.registerChangeHandler(MODID)
		
		fixPistonsCrash()
	}
	
	@Mod.EventHandler
	fun preInit(e: FMLPreInitializationEvent) {
		fixGuiColors()
		
		Blocks.melon_stem.setBlockName("melonStem")
		Blocks.piston_head.setBlockName("pistonHead")
		Blocks.piston_extension.setBlockName("pistonExtension")
		Blocks.end_portal.setBlockName("endPortal")
		
		if (!PatcherConfigHandler.addBlocks) return
		
		val noItems = hashSetOf(Blocks.brewing_stand, Blocks.bed, Blocks.nether_wart, Blocks.cauldron, Blocks.flower_pot, Blocks.wheat, Blocks.reeds, Blocks.cake, Blocks.skull, Blocks.piston_head, Blocks.piston_extension, Blocks.lit_redstone_ore, Blocks.powered_repeater, Blocks.pumpkin_stem, Blocks.standing_sign, Blocks.powered_comparator, Blocks.tripwire, Blocks.lit_redstone_lamp, Blocks.melon_stem, Blocks.unlit_redstone_torch, Blocks.unpowered_comparator, Blocks.redstone_wire, Blocks.wall_sign, Blocks.unpowered_repeater, Blocks.iron_door, Blocks.wooden_door)
		if (PatcherConfigHandler.addAir) noItems += Blocks.air
		noItems.forEach { block ->
			GameData.getMain().registerItem(ItemBlock(block), Block.blockRegistry.getNameForObject(block) + "_item", Block.getIdFromBlock(block))
		}
	}
	
	@Mod.EventHandler
	fun init(e: FMLInitializationEvent) {
		PatcherEventHandler.eventForge().eventFML()
		PlayerReachDistanceHandler.eventForge()
		
		NetworkHandler
		
		BlockTrapDoor.disableValidation = PatcherConfigHandler.floatingTrapDoors
		
		if (ASJUtilities.isClient) {
			NoEntityInteractionHandler.eventFML()
			PatcherEventHandlerClient.eventForge()
			ASJShaderHelper.registerHandlers()
			if (!ASJHookLoader.OBF) ClientCommandHandler.instance.registerCommand(CommandResources)
		}
	}
	
	@Mod.EventHandler
	fun postInit(e: FMLPostInitializationEvent) {
		CraftingHandler
	}
	
	@Mod.EventHandler
	fun onServerStarting(e: FMLServerStartingEvent) {
		e.registerServerCommand(CommandDimInfo)
		e.registerServerCommand(CommandDimTP)
		e.registerServerCommand(CommandExplode)
		e.registerServerCommand(CommandHeal)
		e.registerServerCommand(CommandHookList)
		e.registerServerCommand(CommandKillAll)
		e.registerServerCommand(CommandRtp)
		e.registerServerCommand(CommandSchema)
		e.registerServerCommand(CommandTop)
		e.registerServerCommand(CommandWolkJpeg)
		
		MinecraftForge.EVENT_BUS.post(ServerStartingEvent(e))
	}
	
	@Mod.EventHandler
	fun onServerStarted(e: FMLServerStartedEvent) {
		MinecraftForge.EVENT_BUS.post(ServerStartedEvent(e))
		
		if (WE_Biome.biomeList.isNotEmpty())
			BiomeGenBase.getBiome(PatcherConfigHandler.WEBiomeID)?.let {
				throw IllegalArgumentException("[$MODID] WEBiomeID is set to ${PatcherConfigHandler.WEBiomeID} - this ID is occupied with ${it.biomeName} (${it::class.java.name}). Change that in configs!")
			}
	}
	
	@Mod.EventHandler
	fun onServerStopping(e: FMLServerStoppingEvent) {
		MinecraftForge.EVENT_BUS.post(ServerStoppingEvent(e))
	}
	
	@Mod.EventHandler
	fun onServerStopped(e: FMLServerStoppedEvent) {
		MinecraftForge.EVENT_BUS.post(ServerStoppedEvent(e))
	}
	
	private fun fixGuiColors() {
		val colors = GuiUtils.colorCodes
		colors[0] = 0x010101
		colors[16] = 0x010101
	}
	
	private fun fixPistonsCrash() {
		Facing.oppositeSide    += IntArray(10) { 0 }
		Facing.offsetsXForSide += IntArray(10) { 0 }
		Facing.offsetsYForSide += IntArray(10) { 0 }
		Facing.offsetsZForSide += IntArray(10) { 0 }
	}
}