package alexsocol.asjlib.command

import alexsocol.asjlib.ASJUtilities
import gloomyfolken.hooklib.asm.HookClassTransformer
import net.minecraft.command.*

object CommandHookList: CommandBase() {
	
	override fun getRequiredPermissionLevel() = 2
	
	override fun getCommandName() = "hooklist"
	
	override fun getCommandUsage(sender: ICommandSender?) = "/$commandName"
	
	override fun processCommand(sender: ICommandSender, args: Array<out String>) {
		HookClassTransformer.notInjectedHooks.forEach {
			ASJUtilities.say(sender, "$it")
		}
	}
}