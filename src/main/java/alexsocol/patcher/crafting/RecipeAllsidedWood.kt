package alexsocol.patcher.crafting

import alexsocol.asjlib.*
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.world.World
import net.minecraftforge.oredict.OreDictionary

object RecipeAllsidedWood: IRecipe {
	
	@Suppress("LocalVariableName")
	override fun matches(inv: InventoryCrafting, world: World?): Boolean {
		val W = inv.inventoryWidth
		val H = inv.sizeInventory / W
		
		val slots = arrayOf<Array<ItemStack?>>(arrayOfNulls(2), arrayOfNulls(2))
		var topLeft = -1 to -1
		
		outer@ for (w in 0 until W)
			for (h in 0 until H) {
				slots[0][0] = inv.getStackInRowAndColumn(w, h) ?: continue
				topLeft = w to h
				break@outer
			}
		
		val main = slots[0][0] ?: return false
		
		// match by oredict
		if (OreDictionary.getOreID("logWood") !in OreDictionary.getOreIDs(main)) return false
		
		val (w, h) = topLeft
		slots[0][1] = inv.getStackInRowAndColumn(w, h + 1) ?: return false
		slots[1][1] = inv.getStackInRowAndColumn(w + 1, h + 1) ?: return false
		slots[1][0] = inv.getStackInRowAndColumn(w + 1, h) ?: return false
		
		// all 4 must match by id and meta
		return slots.all { line -> line.all { it!!.isItemEqual(main) } }
	}
	
	@Suppress("LocalVariableName")
	override fun getCraftingResult(inv: InventoryCrafting): ItemStack? {
		val W = inv.inventoryWidth
		val H = inv.sizeInventory / W
		
		for (w in 0 until W)
			for (h in 0 until H) {
				val topLeft = inv.getStackInRowAndColumn(w, h)?.copy() ?: continue
				topLeft.meta = topLeft.meta and 0b0011 or 0b1100
				topLeft.stackSize = 4
				return topLeft
			}
		
		return null
	}
	
	override fun getRecipeOutput() = null
	
	override fun getRecipeSize() = 4
}