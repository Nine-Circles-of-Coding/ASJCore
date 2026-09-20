package alexsocol.patcher.client

import alexsocol.patcher.PatcherConfigHandler
import cpw.mods.fml.client.IModGuiFactory
import cpw.mods.fml.client.config.GuiConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.common.config.*

class GUIFactory: IModGuiFactory {
	
	override fun initialize(minecraftInstance: Minecraft) = Unit
	override fun mainConfigGuiClass() = GUIConfig::class.java
	override fun runtimeGuiCategories() = null
	override fun getHandlerFor(element: IModGuiFactory.RuntimeOptionCategoryElement) = null
}

class GUIConfig(screen: GuiScreen): GuiConfig(screen, ConfigElement<Any?>(PatcherConfigHandler.config.getCategory(Configuration.CATEGORY_GENERAL)).childElements, "asjpatcher", false, false, getAbridgedConfigPath(PatcherConfigHandler.config.toString()))