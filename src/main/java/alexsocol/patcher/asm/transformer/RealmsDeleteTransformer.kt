package alexsocol.patcher.asm.transformer

import alexsocol.patcher.PatcherConfigHandler
import net.minecraft.launchwrapper.IClassTransformer

class RealmsDeleteTransformer: IClassTransformer {
	
	override fun transform(name: String?, transformedName: String?, basicClass: ByteArray?): ByteArray? {
		if (!PatcherConfigHandler.deleteRealms) return basicClass
		
		val deleted = transformedName?.startsWith("net.minecraft.realms") == true ||
					  transformedName?.startsWith("com.mojang.realmsclient") == true ||
		              transformedName == "net.minecraft.client.gui.GuiButtonRealmsProxy"
		              transformedName == "net.minecraft.client.gui.GuiScreenRealmsProxy"
		              transformedName == "net.minecraft.client.gui.GuiSlotRealmsProxy"
		
		if (deleted) {
			throw ClassNotFoundException("Realms were deleted by ASJCore. Disable deletion in configs if needed.")
		}
		
		return basicClass
	}
}
