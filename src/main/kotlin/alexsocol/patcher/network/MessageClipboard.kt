package alexsocol.patcher.network

import alexsocol.asjlib.network.ASJPacket
import cpw.mods.fml.common.network.simpleimpl.*
import java.awt.*
import java.awt.datatransfer.StringSelection

class MessageClipboard(var content: String): ASJPacket() {
	
	companion object: IMessageHandler<MessageClipboard, IMessage?> {
		override fun onMessage(msg: MessageClipboard, ctx: MessageContext): IMessage? {
			if (ctx.side.isClient && Desktop.isDesktopSupported())
				Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(msg.content), null)
			
			return null
		}
	}
}