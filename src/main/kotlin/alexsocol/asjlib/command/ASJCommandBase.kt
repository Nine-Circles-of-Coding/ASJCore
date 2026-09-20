package alexsocol.asjlib.command

import net.minecraft.command.*

abstract class ASJCommandBase: CommandBase() {
	override fun getCommandUsage(sender: ICommandSender?) = "asjcore.commands.$commandName.usage"
}