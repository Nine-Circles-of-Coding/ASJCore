package alexsocol.asjlib.command

import alexsocol.asjlib.ASJUtilities
import com.KAIIIAK.KASMLib.util.KASMUtil
import net.minecraft.command.*
import net.minecraft.launchwrapper.*

object CommandPrintTransformers: ASJCommandBase() {
	
	override fun getCommandName() = "printtransformers"
	
	@Suppress("UNCHECKED_CAST")
	override fun processCommand(sender: ICommandSender, args: Array<String?>?) {
		for (iClassTransformer in KASMUtil.transformers.get(Launch.classLoader) as MutableList<IClassTransformer>)
			ASJUtilities.say(sender, iClassTransformer.javaClass.getName())
	}
}
