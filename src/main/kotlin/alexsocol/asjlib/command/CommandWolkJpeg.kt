package alexsocol.asjlib.command

import alexsocol.patcher.network.*
import net.minecraft.command.*
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer

object CommandWolkJpeg: ASJCommandBase() {
	
	override fun getCommandName() = "wolk.jpeg"
	
	override fun getRequiredPermissionLevel() = 0
	
	override fun canCommandSenderUseCommand(sender: ICommandSender?) = true
	
	override fun processCommand(sender: ICommandSender, args: Array<out String?>) {
		val target = if (args.isEmpty()) sender as? EntityPlayerMP ?: throw WrongUsageException(getCommandUsage(sender)) else getPlayer(sender, args[0])
		NetworkHandler.network.sendTo(MessageWolkJpeg(), target)
	}
	
	override fun addTabCompletionOptions(sender: ICommandSender?, args: Array<out String?>): List<*>? {
		return if (args.size != 1) getListOfStringsMatchingLastWord(args, *MinecraftServer.getServer().allUsernames) else null
	}
}