package alexsocol.patcher.asm

import alexsocol.asjlib.I
import codechicken.lib.vec.BlockCoord
import codechicken.lib.vec.Cuboid6
import codechicken.multipart.minecraft.*
import gloomyfolken.hooklib.asm.*
import net.minecraft.block.*
import net.minecraft.world.*
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.common.util.ForgeDirection.*

@Suppress("unused")
object BlockButtonExtender {
	
	private val ForgeDirection.buttonMeta get() = 6 - ordinal
	private val Int.buttonSide get() = ForgeDirection.entries[6 - this]
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS)
	fun canPlaceBlockOnSide(button: BlockButton, world: World, x: Int, y: Int, z: Int, side: Int): Boolean {
		val dir = getOrientation(side).opposite
		return world.isSideSolid(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir.opposite)
	}
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS)
	fun canPlaceBlockAt(button: BlockButton, world: World, x: Int, y: Int, z: Int) = VALID_DIRECTIONS.any { world.isSideSolid(x + it.offsetX, y + it.offsetY, z + it.offsetZ, it) }
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS)
	fun onBlockPlaced(button: BlockButton, world: World, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float, hitZ: Float, metadata: Int): Int {
		var meta = world.getBlockMetadata(x, y, z)
		val pressedFlag = meta and 8
		
		val dir = getOrientation(side).opposite
		meta = if (world.isSideSolid(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir.opposite))
			dir.opposite.buttonMeta
		else
			pickValidMeta(button, world, x, y, z)
		
		return meta + pressedFlag
	}
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS, targetMethod = "func_150045_e")
	fun pickValidMeta(button: BlockButton, world: World, x: Int, y: Int, z: Int) =
		VALID_DIRECTIONS.firstOrNull {
			val op = it.opposite
			world.isSideSolid(x + op.offsetX, y + op.offsetY, z + op.offsetZ, it)
		}?.buttonMeta ?: 0
	
	val addedDirs = arrayOf(UP, DOWN)
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS)
	fun onNeighborBlockChange(button: BlockButton, world: World, x: Int, y: Int, z: Int, block: Block?) {
		val meta = world.getBlockMetadata(x, y, z)
		val metaBase = meta and 7
		
		if (canPlaceBlockOnSide(button, world, x, y, z, metaBase.buttonSide.ordinal)) return
		
		button.dropBlockAsItem(world, x, y, z, meta, 0)
		world.setBlockToAir(x, y, z)
	}
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_TRUE, targetMethod = "func_150043_b")
	fun setBlockBoundsFromMeta(button: BlockButton, meta: Int): Boolean {
		val metaBase = meta and 7
		val isPressed = meta and 8 > 0
		
		val xzOffset = 6f/16/2
		val yOffset = if (isPressed) 0.0625f else 0.125f
		
		when (metaBase) {
			DOWN.buttonMeta -> button.setBlockBounds(0.5f - xzOffset, 1f - yOffset, 0.5f - xzOffset, 0.5f + xzOffset, 1f, 0.5f + xzOffset)
			UP.buttonMeta   -> button.setBlockBounds(0.5f - xzOffset, 0f, 0.5f - xzOffset, 0.5f + xzOffset, yOffset, 0.5f + xzOffset)
			else            -> return false
		}
		
		return true
	}
	
	@JvmStatic // why there already was a check for UP direction?
	@Hook(returnCondition = ReturnCondition.ON_TRUE, intReturnConstant = 15)
	fun isProvidingStrongPower(button: BlockButton, world: IBlockAccess, x: Int, y: Int, z: Int, side: Int): Boolean {
		val meta = world.getBlockMetadata(x, y, z)
		return if (meta and 8 == 0) false else meta and 7 == 6 && side == 0
	}
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS, targetMethod = "func_150042_a")
	fun updateNeighbor(button: BlockButton, world: World, x: Int, y: Int, z: Int, metaBase: Int) {
		world.notifyBlocksOfNeighborChange(x, y, z, button)
		
		val dir = metaBase.buttonSide.opposite
		world.notifyBlocksOfNeighborChange(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, button)
	}
	
	// for Forge Multipart:
	@JvmStatic
	@Hook(injectOnExit = true, targetMethod = "<clinit>")
	fun `ButtonPart$clinit`(static: ButtonPart?) {
		ButtonPart.sideMetaMap = intArrayOf(6, 5, 3, 4, 1, 2)
		ButtonPart.metaSideMap = intArrayOf(-1, 4, 5, 2, 3, 0, 1)
	}
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL)
	fun getBounds(button: ButtonPart): Cuboid6? {
		val meta = button.meta.I
		val metaBase = meta and 7
		val isPressed = button.pressed()
		
		val xzOffset = 6f/16/2
		val yOffset = if (isPressed) 0.0625 else 0.125
		
		return when (metaBase) {
			DOWN.buttonMeta -> Cuboid6(0.5 - xzOffset, 1.0 - yOffset, 0.5 - xzOffset, 0.5 + xzOffset, 1.0, 0.5 + xzOffset)
			UP.buttonMeta   -> Cuboid6(0.5 - xzOffset, 0.0, 0.5 - xzOffset, 0.5 + xzOffset, yOffset, 0.5 + xzOffset)
			else            -> null
		}
	}
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL)
	fun placement(button: ButtonPart?, world: World, pos: BlockCoord, side: Int, type: Int): McBlockPart? {
		if (side != 0 && side != 1) return null
		val newPos = pos.copy().offset(side xor 1)
		return if (!world.isSideSolid(newPos.x, newPos.y, newPos.z, getOrientation(side))) null else ButtonPart(ButtonPart.sideMetaMap[side] or (type shl 4))
	}
}