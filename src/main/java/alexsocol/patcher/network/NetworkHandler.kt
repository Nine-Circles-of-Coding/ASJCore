package alexsocol.patcher.network

import alexsocol.patcher.PatcherMain.MODID
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
import cpw.mods.fml.relauncher.Side

object NetworkHandler {
	
	val network = SimpleNetworkWrapper(MODID)
	
	init {
		network.registerMessage(MessageClipboard, MessageClipboard::class.java, 0, Side.CLIENT)
		network.registerMessage(MessageUUID, MessageUUID::class.java, 1, Side.CLIENT)
	}
}