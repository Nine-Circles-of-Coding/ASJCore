package alexsocol.patcher.helper

import alexsocol.asjlib.ASJReflectionHelper
import cpw.mods.fml.common.Loader
import net.minecraft.item.ItemStack
import net.minecraft.util.DamageSource

object AlchemicalWizardryIntegration {
	
	val AWLoaded by lazy { Loader.isModLoaded("AWWayofTime") }
	val BoundArmour by lazy { 
		try {
			Class.forName("WayofTime.alchemicalWizardry.common.items.armour.BoundArmour")
		} catch (e: Throwable) {
			null
		}
	}
	val isImmuneToVoid by lazy { 
		if (BoundArmour != null)
			ASJReflectionHelper.getMethod(BoundArmour, "isImmuneToVoid", arrayOf(ItemStack::class.java))
		else
			null
	}
	
	fun hasVoidSigil(inventory: Array<ItemStack?>, source: DamageSource): Boolean {
		if (!AWLoaded || BoundArmour == null || isImmuneToVoid == null || source != DamageSource.outOfWorld) return false
		
		for (item in inventory) {
			if (BoundArmour?.isInstance(item?.item ?: continue) != true) continue
			if (ASJReflectionHelper.invoke<Any, Boolean>(isImmuneToVoid, item?.item, arrayOf(item)) == true) return true
		}
		
		return false
	}
}
