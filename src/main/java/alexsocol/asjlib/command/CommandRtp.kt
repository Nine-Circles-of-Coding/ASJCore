package alexsocol.asjlib.command

import alexsocol.asjlib.*
import net.minecraft.command.ICommandSender
import net.minecraft.entity.Entity
import net.minecraft.util.ChunkCoordinates

object CommandRtp: ASJCommandBase() {
	
	override fun getCommandName() = "rtp"
	
	override fun processCommand(sender: ICommandSender?, args: Array<out String>) {
		if (sender !is Entity) return
		
		val world = sender.worldObj
		
		val (i, _, k) = if (args.size >= 3) ChunkCoordinates(args[1].toInt(), 0, args[2].toInt()) else world.spawnPoint
		
		val max = 30_000_000
		val radius = args.getOrElse(0) { "1000" }.toInt()
		
		val x = ASJUtilities.randInBounds(-radius, radius, world.rand).plus(i).clamp(-max, max)
		val z = ASJUtilities.randInBounds(-radius, radius, world.rand).plus(k).clamp(-max, max)
		val y = world.getTopSolidOrLiquidBlock(x, z)
		
		ASJUtilities.sendToDimensionWithoutPortal(sender, world.provider.dimensionId, x + 0.5, y.D, z + 0.5)
		func_152373_a(sender, this, "commands.tp.success.coordinates", sender.getCommandSenderName(), x, y, z)
	}
}
