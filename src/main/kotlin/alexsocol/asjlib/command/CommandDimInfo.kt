package alexsocol.asjlib.command

import alexsocol.asjlib.ASJUtilities
import net.minecraft.command.*
import net.minecraft.server.MinecraftServer
import net.minecraftforge.common.DimensionManager

object CommandDimInfo: CommandBase() {
	
	const val ALL_TAG = "all"
	
	override fun getRequiredPermissionLevel() = 0
	
	override fun getCommandName() = "diminfo"
	
	override fun getCommandUsage(sender: ICommandSender) = "/$commandName [$ALL_TAG]"
	
	override fun processCommand(sender: ICommandSender, args: Array<String>) {
		if (args.getOrNull(0)?.lowercase() == ALL_TAG) {
			for ((id, it) in DimensionManager.getStaticDimensionIDs().withIndex()) {
				val provider = MinecraftServer.getServer().worldServerForDimension(it)?.provider
				
				if (provider == null)
					ASJUtilities.say(sender, "asjcore.commands.diminfo.list.fail", id, it)
				else
					ASJUtilities.say(sender, "asjcore.commands.diminfo.list", id, provider.dimensionName, provider.dimensionId)
			}
		} else {
			val provider = sender.entityWorld.provider
			ASJUtilities.say(sender, "asjcore.commands.diminfo.current", provider.dimensionName, provider.dimensionId)
		}
	}
	
	override fun addTabCompletionOptions(sender: ICommandSender?, args: Array<out String>): List<String> {
		return if (args.size == 1) listOf(ALL_TAG) else emptyList()
	}
}
