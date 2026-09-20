package alexsocol.asjlib.command

import alexsocol.asjlib.*
import net.minecraft.command.ICommandSender
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.util.*
import net.minecraft.world.*
import net.minecraftforge.common.util.ForgeDirection
import java.util.concurrent.*

object CommandRtp: ASJCommandBase() {
	
	val executor = Executors.newSingleThreadExecutor()
	
	override fun getCommandName() = "rtp"
	
	override fun processCommand(sender: ICommandSender?, a: Array<out String>) {
		if (sender !is Entity) return
		
		val forced = a.any { it == "f" }
		val args = a.filter { it != "f" }
		
		val world = sender.worldObj
		
		val (i, _, k) = if (args.size >= 3) ChunkCoordinates(args[1].toInt(), 0, args[2].toInt()) else world.spawnPoint
		
		val max = 30_000_000
		val radius = args.getOrElse(0) { "1000" }.toInt()
		
		var x: Int
		var z: Int
		var y: Int
		
		if (forced) {
			x = ASJUtilities.randInBounds(-radius, radius, world.rand).plus(i).clamp(-max, max)
			z = ASJUtilities.randInBounds(-radius, radius, world.rand).plus(k).clamp(-max, max)
			
			tp(sender, x, world.getTopSolidOrLiquidBlock(x, z), z)
		} else {
			executor.execute {
				var retries = 1000
				
				do {
					x = ASJUtilities.randInBounds(-radius, radius, world.rand).plus(i).clamp(-max, max)
					z = ASJUtilities.randInBounds(-radius, radius, world.rand).plus(k).clamp(-max, max)
					y = searchSuitableYLevel(world, x, z)
					retries--
				} while (y <= 0 && retries > 0)
				
				if (y <= 0)
					ASJUtilities.say(sender, "asjcore.commands.rtp.fail", EnumChatFormatting.RED)
				else
					tp(sender, x, y + 1, z)
			}
		}
	}
	
	private fun tp(sender: Entity, x: Int, y: Int, z: Int) {
		if (sender is EntityPlayerMP)
			sender.field_147101_bU = 60
		
		ASJUtilities.sendToDimensionWithoutPortal(sender, sender.worldObj.provider.dimensionId, x + 0.5, y.D, z + 0.5)
		func_152373_a(sender as ICommandSender, this, "commands.tp.success.coordinates", sender.commandSenderName, x, y, z)
	}
	
	// https://github.com/KasperFranz/KaisRandomTeleport/blob/master/src/main/java/net/kaikk/mc/rtp/KaisRandomTP.java#L109
	fun searchSuitableYLevel(world: World, x: Int, z: Int): Int {
		if (world.provider.terrainType === WorldType.FLAT)
			return world.getTopSolidOrLiquidBlock(x, z) - 1
		
		var y = world.provider.actualHeight - 2 // Max y level
		var fastforward = 8 // default fastforward
		
		if (world.provider.dimensionId == 1) 
			fastforward = 2 // fastforward for the end
		
		var block = BlockWCoords(world, x, y, z)
		
		var c: Int
		var solid = Int.MAX_VALUE
		var shift = 0
		
		do {
			if (block.isSolid) {
				if (shift == fastforward && solid > y) { // Stop fastforward
					solid = y
					y += fastforward - 1
					block = block.getRelative(ForgeDirection.UP, fastforward - 1)
					continue
				} else {
					var upperBlock = block.getRelative(ForgeDirection.UP)
					
					c = 0
					while (c < 4) { // a y level is suitable if there are 4 empty blocks above a valid solid block
						if (!upperBlock.isAir) { // if it's not empty
							if (upperBlock.block.material.isLiquid) { // if you get a liquid, it could be an ocean or a lava lake... there won't be any good y level here.
								return -1
							}
							break
						}
						upperBlock = upperBlock.getRelative(ForgeDirection.UP)
						c++
					}
					
					if (c == 4) {
						return y // Found a suitable y level
					} else {
						shift = 4 // 
					}
				}
			} else {
				// fastforward mode
				shift = if (solid > y) fastforward else 1
			}
			
			y -= shift
			block = block.getRelative(ForgeDirection.DOWN, shift)
		} while (y > 0)
		
		return -1
	}
	
	private data class BlockWCoords(val world: World, val x: Int, val y: Int, val z: Int) {
		
		val block = world.getBlock(x, y, z)!!
		val isSolid by lazy { block.material.isSolid && world.isSideSolid(x, y, z, ForgeDirection.UP) }
		val isAir by lazy { block.isAir(world, x, y, z) }
		
		fun getRelative(dir: ForgeDirection, offset: Int = 1) = BlockWCoords(world, x + offset * dir.offsetX, y + offset * dir.offsetY, z + offset * dir.offsetZ)
	}
}
