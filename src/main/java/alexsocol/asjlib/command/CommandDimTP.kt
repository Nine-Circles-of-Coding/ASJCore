package alexsocol.asjlib.command

import alexsocol.asjlib.*
import net.minecraft.command.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.ChunkCoordinates

object CommandDimTP: ASJCommandBase() {
	
	override fun getRequiredPermissionLevel() = 2
	
	override fun getCommandName() = "tpdim"
	
	override fun processCommand(sender: ICommandSender, args: Array<String>) {
		try {
			val target = if (args.size > 1) getPlayer(sender, args[1]) else sender as EntityPlayer
			val id = args[0].toInt()
			val w = MinecraftServer.getServer().worldServerForDimension(id) ?: throw NoWorldException("Loaded dimension is null")
//			val s: ChunkCoordinates = sender.getBedLocation(id) ?: w.spawnPoint ?: ChunkCoordinates(0, w.getHeightValue(0, 0) + 1, 0)
			val s: ChunkCoordinates = w.spawnPoint ?: throw NoWorldException("No spawnpoint")
			// stupid minecraft returns overworld coordinates in ANY dimension
			ASJUtilities.sendToDimensionWithoutPortal(target, id, s.posX + 0.5, s.posY.D, s.posZ + 0.5)
		} catch (e: NoWorldException) {
			throw WrongUsageException("asjcore.commands.tpdim.worlderr", e)
		} catch (e: Throwable) {
			throw WrongUsageException(getCommandUsage(sender), e)
		}
	}
	
	private class NoWorldException(message: String): RuntimeException(message)
}