package alexsocol.patcher

import alexsocol.asjlib.*
import alexsocol.asjlib.command.*
import alexsocol.patcher.asm.ASJHookLoader
import alexsocol.patcher.event.*
import alexsocol.patcher.handler.*
import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.event.*
import cpw.mods.fml.common.registry.GameData
import net.minecraft.block.*
import net.minecraft.command.CommandBase
import net.minecraft.enchantment.*
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.potion.Potion
import net.minecraft.world.biome.BiomeGenBase
import net.minecraftforge.common.MinecraftForge
import java.lang.StringBuilder

@Mod(modid = "asjpatcher", modLanguageAdapter = KotlinAdapter.className)
object PatcherMain {
	
	val duplicatedBiomes = mutableListOf<Pair<BiomeGenBase, BiomeGenBase>>()
	val duplicatedEnchantments = mutableListOf<Pair<Enchantment, Enchantment>>()
	val duplicatedPotions = mutableListOf<Pair<Potion, Potion>>()
	
	@Mod.EventHandler
	fun preInit(e: FMLPreInitializationEvent) {
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
		
		BlockTrapDoor.disableValidation = PatcherConfigHandler.floatingTrapDoors
		
		if (ASJUtilities.isClient)
			PatcherEventHandlerClient.eventForge()
	}
	
	private operator fun StringBuilder.plusAssign(s: String) {
		append(s).append('\n')
	}
	
	@Mod.EventHandler
	fun onMCLoaded(event: FMLServerStartedEvent) {
		if (duplicatedBiomes.isEmpty() && duplicatedEnchantments.isEmpty() && duplicatedPotions.isEmpty()) return
		
		val message = StringBuilder("Duplicated IDs were found during modded registration process:\n")
		
		if (duplicatedBiomes.isNotEmpty()) {
			message += " - Duplicated Biome IDs:"
			
			for ((b1, b2) in duplicatedBiomes) {
				message += "\t${b1.biomeID} - ${b1.biomeName} (${b1.biomeClass})\t\tAND\t\t${b2.biomeID} - ${b2.biomeName} (${b2.biomeClass})"
			}
			
			message += "\n\tFree Biome IDs: [${BiomeGenBase.getBiomeGenArray().foldIndexed("") { id, acc, it -> acc + if (it == null) "$id," else "" } }]\n"
		}
		
		if (duplicatedEnchantments.isNotEmpty()) {
			message += " - Duplicated Enchantment IDs:"
			
			for ((e1, e2) in duplicatedEnchantments) {
				message += "\t${e1.effectId} - ${e1.name} (${e1.javaClass})\t\tAND\t\t${e2.effectId} - ${e2.name} (${e2.javaClass})"
			}
			
			message += "\n\tFree Enchantment IDs: [${Enchantment.enchantmentsList.foldIndexed("") { id, acc, it -> acc + if (it == null) "$id," else "" } }]\n"
		}
		
		if (duplicatedPotions.isNotEmpty()) {
			message += " - Duplicated Potion IDs:"
			
			for ((p1, p2) in duplicatedPotions) {
				message += "\t${p1.id} - ${p1.name} (${p1.javaClass})\t\tAND\t\t${p2.id} - ${p2.name} (${p2.javaClass})"
			}
			
			message += "\n\tFree Potion IDs: [${Potion.potionTypes.foldIndexed("") { id, acc, it -> acc + if (it == null) "$id," else "" } }]\n"
		}
		
		throw IllegalArgumentException(message.toString())
	}
	
	@Mod.EventHandler
	fun onServerStarting(e: FMLServerStartingEvent) {
		PatcherConfigHandler.commands.forEach { e.registerServerCommand(Commands.valueOf(it).command()) }
		e.registerServerCommand(CommandSchema)
		
		if (!ASJHookLoader.OBF) e.registerServerCommand(CommandResources)
		
		MinecraftForge.EVENT_BUS.post(ServerStartingEvent(e))
	}
	
	@Mod.EventHandler
	fun onServerStarted(e: FMLServerStartedEvent) {
		MinecraftForge.EVENT_BUS.post(ServerStartedEvent(e))
	}
	
	@Mod.EventHandler
	fun onServerStopping(e: FMLServerStoppingEvent) {
		MinecraftForge.EVENT_BUS.post(ServerStoppingEvent(e))
	}
	
	@Mod.EventHandler
	fun onServerStopped(e: FMLServerStoppedEvent) {
		MinecraftForge.EVENT_BUS.post(ServerStoppedEvent(e))
	}
	
	enum class Commands(val command: () -> CommandBase) {
		DIMTP({ CommandDimTP }), EXPLODE({ CommandExplode }), HEAL({ CommandHeal })
	}
}