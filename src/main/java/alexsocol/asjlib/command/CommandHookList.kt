package alexsocol.asjlib.command

import alexsocol.asjlib.ASJUtilities
import com.KAIIIAK.classManipulators.HookReplacerWorker
import com.KAIIIAK.classManipulators.IMandatoryCheck.CheckState.*
import gloomyfolken.hooklib.asm.HookClassTransformer
import net.minecraft.command.*
import net.minecraft.util.EnumChatFormatting

object CommandHookList: CommandBase() {
	
	override fun getRequiredPermissionLevel() = 2
	
	override fun getCommandName() = "hooklist"
	
	override fun getCommandUsage(sender: ICommandSender?) = "/$commandName"
	
	override fun processCommand(sender: ICommandSender, args: Array<out String>) {
		HookClassTransformer.notInjectedHooks.forEach {
			ASJUtilities.say(sender, "$it")
		}
		
		HookReplacerWorker.registeredChangesHolders.values.flatMap { it.values }.flatten().forEach {
			val check = it.successor.check()
			
			if (check != APPLIED)
				ASJUtilities.say(sender, "${if (check == FAILED) "[${EnumChatFormatting.DARK_RED}FAILED${EnumChatFormatting.RESET}] " else ""}$it")
		}
	}
}