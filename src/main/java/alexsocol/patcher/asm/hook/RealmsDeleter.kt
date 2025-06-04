package alexsocol.patcher.asm.hook

import gloomyfolken.hooklib.asm.*
import net.minecraft.client.gui.*

@Suppress("unused")
object RealmsDeleter {
	
	@JvmStatic
	@Hook(injectOnExit = true)
	fun addSingleplayerMultiplayerButtons(gui: GuiMainMenu, i: Int, j: Int) {
		gui.buttonList.removeAll { (it as GuiButton).id == 14 } // realms button
		gui.buttonList.find { (it as GuiButton).id == 6 }?.let { // mods button
			it as GuiButton
			it.width = 200
			it.xPosition = gui.width / 2 - 100
		} 
	}
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS)
	fun func_140005_i(gui: GuiMainMenu) = Unit
}