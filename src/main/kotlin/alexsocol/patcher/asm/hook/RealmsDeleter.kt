package alexsocol.patcher.asm.hook

import net.minecraft.client.gui.*

// Called from alexsocol.mixins.client.MixinGuiMainMenu.
// The former func_140005_i hook body was an empty method that existed only to carry
// @Hook(returnCondition = ALWAYS); the mixin cancels the callback instead, so it is gone.
@Suppress("unused")
object RealmsDeleter {
	
	@JvmStatic
	fun addSingleplayerMultiplayerButtons(gui: GuiMainMenu, i: Int, j: Int) {
		gui.buttonList.removeAll { (it as GuiButton).id == 14 } // realms button
		gui.buttonList.find { (it as GuiButton).id == 6 }?.let { // mods button
			it as GuiButton
			it.width = 200
			it.xPosition = gui.width / 2 - 100
		}
	}
}
