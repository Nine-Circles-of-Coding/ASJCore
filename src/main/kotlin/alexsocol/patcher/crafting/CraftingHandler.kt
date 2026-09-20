package alexsocol.patcher.crafting

import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary

object CraftingHandler {
	
	init {
		for (stack in OreDictionary.getOres("logWood")) {
			val log = stack.item
			
			for (meta in 0..3)
				GameRegistry.addShapedRecipe(ItemStack(log, 4, meta + 12),
											 "LL", "LL",
											 'L', ItemStack(log, 1, meta))
		}
	}
}