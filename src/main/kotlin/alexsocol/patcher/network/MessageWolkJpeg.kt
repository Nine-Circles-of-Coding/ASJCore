package alexsocol.patcher.network

import alexsocol.asjlib.mc
import alexsocol.asjlib.network.ASJPacket
import alexsocol.patcher.client.WolkJpegOverlay
import cpw.mods.fml.common.network.simpleimpl.*

class MessageWolkJpeg: ASJPacket() {
	
	companion object: IMessageHandler<MessageWolkJpeg, IMessage?> {
		override fun onMessage(msg: MessageWolkJpeg, ctx: MessageContext): IMessage? {
			mc.displayGuiScreen(null)
			WolkJpegOverlay.ticksActive = 20
			return null
		}
	}
}