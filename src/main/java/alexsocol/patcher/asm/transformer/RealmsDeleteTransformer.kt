package alexsocol.patcher.asm.transformer

import alexsocol.asjlib.ASJUtilities
import alexsocol.patcher.PatcherPreConfigHandler
import net.minecraft.launchwrapper.IClassTransformer

class RealmsDeleteTransformer: IClassTransformer {
	
	override fun transform(name: String?, transformedName: String?, basicClass: ByteArray?): ByteArray? {
		if (!PatcherPreConfigHandler.deleteRealms) return basicClass
		
		// exclusion for blind modders
		if (transformedName == "com.mojang.realmsclient.gui.ChatFormatting") {
			ASJUtilities.warn("****************************************")
			ASJUtilities.warn("* ASJCore detected that some mod is using Minecraft Realms code")
			ASJUtilities.warn("* That should NOT happen in most cases! Report it to mod author (NOT AlexSocol/ASJCore)")
			ASJUtilities.warn("* Full stacktrace to identify a mod: ")
			val trace = Thread.currentThread().stackTrace
			repeat(trace.size - 2) {
				ASJUtilities.warn("*  at ${trace[it]}")
			}
			ASJUtilities.warn("****************************************")
			
			return basicClass
		}
		
		val deleted = transformedName?.startsWith("net.minecraft.realms") == true ||
					  transformedName?.startsWith("com.mojang.realmsclient") == true ||
		              transformedName == "net.minecraft.client.gui.GuiButtonRealmsProxy" ||
		              transformedName == "net.minecraft.client.gui.GuiScreenRealmsProxy" ||
		              transformedName == "net.minecraft.client.gui.GuiSlotRealmsProxy"
		
		if (deleted) {
			throw ClassNotFoundException("Realms were deleted by ASJCore. Disable deletion in configs if needed.")
		}
		
		return basicClass
	}
}
