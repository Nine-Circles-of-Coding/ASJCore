package alexsocol.asjlib.command

import alexsocol.asjlib.*
import alexsocol.patcher.*
import alexsocol.patcher.asm.hook.*
import com.KAIIIAK.KASMLib.util.*
import net.minecraft.command.*

class CommandTickrate(val client: Boolean): ASJCommandBase() {
	
	override fun getCommandName() = "tickrate${if (client) 'c' else ""}"
	
	override fun getRequiredPermissionLevel() = if (client) 0 else super.getRequiredPermissionLevel()
	
	override fun processCommand(sender: ICommandSender?, args: Array<String>) {
		if (!client && KASMUtil.findLoadedClass("fastcraft.u") != null)
			return ASJUtilities.say(sender, "asjcore.commands.tickrate.fail.fastcraft")
		
		if (args.size != 1) throw WrongUsageException(getCommandUsage(sender))
		
		if (client) {
			PatcherConfigHandler.tps = 1000f / parseIntBounded(sender, args[0], 1, 1000)
			mc.timer.ticksPerSecond = getTPS()
		}
		else
			PatcherConfigHandler.msPerTick = parseIntBounded(sender, args[0], 1, 1000)
		
		PatcherConfigHandler.config.save()
	}
}