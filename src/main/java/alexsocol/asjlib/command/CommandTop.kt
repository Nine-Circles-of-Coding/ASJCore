package alexsocol.asjlib.command

import alexsocol.asjlib.*
import alexsocol.asjlib.math.Vector3
import net.minecraft.command.*
import net.minecraft.entity.Entity

object CommandTop: ASJCommandBase() {
	
	override fun getCommandName() = "top"
	
	override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
		if (sender !is Entity) return
		
		val world = sender.entityWorld
		val (x, _, z) = Vector3.fromEntity(sender).mf()
		val y = world.getTopSolidOrLiquidBlock(x, z)
		
		ASJUtilities.sendToDimensionWithoutPortal(sender, world.provider.dimensionId, x + 0.5, y.D, z + 0.5)
		func_152373_a(sender, this, "commands.tp.success.coordinates", sender.getCommandSenderName(), x, y, z)
	}
}
