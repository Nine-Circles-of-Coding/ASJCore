package alexsocol.patcher.network

import alexsocol.asjlib.*
import alexsocol.asjlib.network.ASJPacket
import cpw.mods.fml.common.network.simpleimpl.*

class MessageTpRequest(var x: Int, var y: Int, var z: Int): ASJPacket() {
	
	companion object: IMessageHandler<MessageTpRequest, IMessage?> {
		
		override fun onMessage(msg: MessageTpRequest, ctx: MessageContext): IMessage? {
			if (!ctx.side.isServer) return null
			val player = ctx.serverHandler.playerEntity
			val y = if (msg.y == -1) player.worldObj.getTopSolidOrLiquidBlock(msg.x, msg.z) else msg.y
			ASJUtilities.sendToDimensionWithoutPortal(player, player.worldObj.provider.dimensionId, msg.x + 0.5, y.D, msg.z + 0.5)
			return null
		}
	}
}