package alexsocol.patcher.crafting

import alexsocol.patcher.PatcherMain
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraftforge.oredict.RecipeSorter

object CraftingHandler {
	
	init {
		GameRegistry.addRecipe(RecipeAllsidedWood)
		RecipeSorter.register("${PatcherMain.MODID}:allsidedwood", RecipeAllsidedWood::class.java, RecipeSorter.Category.SHAPED, "")
	}
}