package alexsocol.patcher.network

import alexsocol.asjlib.mc
import alexsocol.asjlib.network.ASJPacket
import cpw.mods.fml.common.network.simpleimpl.*
import java.util.*

class MessageUUID(var id: Int, var uuid: String): ASJPacket() {
	
	companion object: IMessageHandler<MessageUUID, IMessage?> {
		override fun onMessage(msg: MessageUUID, ctx: MessageContext): IMessage? {
			mc.theWorld.getEntityByID(msg.id)?.entityUniqueID = UUID.fromString(msg.uuid)
			return null
		}
	}
}