package alexsocol.patcher.network

import alexsocol.patcher.PatcherMain.MODID
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
import cpw.mods.fml.relauncher.Side

object NetworkHandler {
	
	val network = SimpleNetworkWrapper(MODID)
	var nextId = 0
		get() = field++
	
	init {
		network.registerMessage(MessageClipboard, MessageClipboard::class.java, nextId, Side.CLIENT)
		network.registerMessage(MessageWolkJpeg, MessageWolkJpeg::class.java, nextId, Side.CLIENT)
		network.registerMessage(MessageUUID, MessageUUID::class.java, nextId, Side.CLIENT)
	}
}