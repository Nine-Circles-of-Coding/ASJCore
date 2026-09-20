package alexsocol.asjlib.extendables

import net.minecraft.block.material.*

class MaterialPublic(color: MapColor, val blocksWater: Boolean = true, val blocksLight: Boolean = true, val liquid: Boolean = false, val opaque: Boolean = true, val solid: Boolean = true, val allowBreakInAdventureMode: Boolean = false, val burnable: Boolean = false, val replaceable: Boolean = false, val requiresNoTool: Boolean = true): Material(color) {
	
	override fun blocksMovement() = blocksWater
	
	override fun getCanBlockGrass() = blocksLight
	
	override fun isLiquid() = liquid
	
	override fun isOpaque() = opaque
	
	override fun isSolid() = solid
	
	override fun isAdventureModeExempt() = allowBreakInAdventureMode
	
	override fun getCanBurn() = burnable
	
	override fun isReplaceable() = replaceable
	
	override fun isToolNotRequired() = requiresNoTool
	
	public override fun setNoPushMobility() = super.setNoPushMobility()
	
	public override fun setImmovableMobility() = super.setImmovableMobility()
}
