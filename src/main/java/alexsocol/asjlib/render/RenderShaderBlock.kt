package alexsocol.asjlib.render

import alexsocol.asjlib.*
import alexsocol.asjlib.math.BlockPos
import alexsocol.asjlib.render.RenderGlowingLayerBlock.Companion.renderers
import com.google.common.collect.HashMultimap
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler
import cpw.mods.fml.common.Loader
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraft.block.Block
import net.minecraft.client.renderer.*
import net.minecraft.world.IBlockAccess
import net.minecraftforge.client.event.RenderWorldEvent
import net.minecraftforge.common.util.ForgeDirection

open class RenderShaderBlock(@get:JvmName("getRenderIdProp") /* stupid kotlin -_- */ val renderId: Int, val shaderId: Int, val getShaderCallback: ((block: Boolean) -> ((Int) -> Unit)?)? = null): ISimpleBlockRenderingHandler {
	
	val cache: HashMultimap<Int, BlockPos> = HashMultimap.create()
	
	init {
		eventForge()
	}
	
	override fun renderWorldBlock(world: IBlockAccess, x: Int, y: Int, z: Int, block: Block, modelId: Int, renderer: RenderBlocks): Boolean {
		if (angelicaLoaded) { // no shader in world :(
			block.setBlockBoundsBasedOnState(world, x, y, z)
			renderer.setRenderBoundsFromBlock(block)
			renderer.renderStandardBlock(block, x, y, z)
		} else
			cache.put(block.renderBlockPass, BlockPos(block, x, y, z))
		
		return true
	}
	
	override fun renderInventoryBlock(block: Block, metadata: Int, modelId: Int, renderer: RenderBlocks) {
		val tes = Tessellator.instance
		
		block.setBlockBoundsForItemRender()
		renderer.setRenderBoundsFromBlock(block)
		
		glTranslatef(-0.5f)
		
		ASJShaderHelper.useShader(shaderId, getShaderCallback?.invoke(false))
		tes.startDrawingQuads()
		
		tes.setNormal(0f, -1f, 0f)
		renderer.renderFaceYNeg(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 0, metadata))
		tes.setNormal(0f, 1f, 0f)
		renderer.renderFaceYPos(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 1, metadata))
		tes.setNormal(0f, 0f, -1f)
		renderer.renderFaceZNeg(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 2, metadata))
		tes.setNormal(0f, 0f, 1f)
		renderer.renderFaceZPos(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 3, metadata))
		tes.setNormal(-1f, 0f, 0f)
		renderer.renderFaceXNeg(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 4, metadata))
		tes.setNormal(1f, 0f, 0f)
		renderer.renderFaceXPos(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 5, metadata))
		
		tes.draw()
		ASJShaderHelper.releaseShader()
		
		glTranslatef(0.5f)
	}
	
	override fun shouldRender3DInInventory(modelId: Int) = true
	
	override fun getRenderId() = renderId
	
	@SubscribeEvent
	fun postRenderWorld(e: RenderWorldEvent.Post) {
		if (cache.isEmpty) return
		
		val passCache = cache.get(e.pass) ?: return
		if (passCache.isEmpty()) return
		
		ASJShaderHelper.useShader(shaderId, getShaderCallback?.invoke(true))
		
		val tes = Tessellator.instance
		tes.startDrawingQuads()
		
		val dirs = ForgeDirection.VALID_DIRECTIONS.withIndex()
		passCache.forEach { (block, x, y, z) ->
			block.setBlockBoundsBasedOnState(e.renderBlocks.blockAccess, x, y, z)
			e.renderBlocks.setRenderBoundsFromBlock(block)
			
			for ((side, d) in dirs) {
				if (!block.shouldSideBeRendered(e.renderer.worldObj, x + d.offsetX, y + d.offsetY, z + d.offsetZ, side)) continue
				
				when (side) {
					0 -> tes.setNormal(0f, -1f, 0f)
					1 -> tes.setNormal(0f, 1f, 0f)
					2 -> tes.setNormal(0f, 0f, -1f)
					3 -> tes.setNormal(0f, 0f, 1f)
					4 -> tes.setNormal(-1f, 0f, 0f)
					5 -> tes.setNormal(1f, 0f, 0f)
				}
				
				renderers[side].invoke(e.renderBlocks, block, x.D, y.D, z.D, e.renderBlocks.getBlockIcon(block, e.renderBlocks.blockAccess, x, y, z, side))
			}
		}
		tes.draw()
		
		ASJShaderHelper.releaseShader()
		
		passCache.clear()
		cache.removeAll(e.pass)
	}
	
	companion object {
		val angelicaLoaded by lazy { Loader.isModLoaded("angelica") }
	}
}