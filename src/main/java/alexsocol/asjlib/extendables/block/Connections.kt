package alexsocol.asjlib.extendables.block

import net.minecraft.world.IBlockAccess

interface IFenceConnectable {
	fun canConnectFenceTo(world: IBlockAccess, x: Int, y: Int, z: Int): Boolean
}

interface IFenceGate {
	fun isGate(world: IBlockAccess, x: Int, y: Int, z: Int): Boolean
}

interface IPaneConnectable {
	fun canPaneConnectTo(world: IBlockAccess, x: Int, y: Int, z: Int): Boolean
}

interface IWallConnectable {
	fun canConnectWallTo(world: IBlockAccess, x: Int, y: Int, z: Int): Boolean
}