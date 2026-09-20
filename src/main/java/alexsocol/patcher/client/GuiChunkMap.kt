package alexsocol.patcher.client

import alexsocol.asjlib.*
import alexsocol.patcher.client.GuiChunkMap.ChunkType.*
import alexsocol.patcher.network.MessageTpRequest
import alexsocol.patcher.network.NetworkHandler
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.Tessellator
import net.minecraft.util.*
import net.minecraft.world.ChunkCoordIntPair
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.*
import alexsocol.asjlib.mc as smc

class GuiChunkMap(val loaded: Map<ChunkCoordIntPair, String>, val forced: Map<ChunkCoordIntPair, String>, val spawnpoint: ChunkCoordinates?, val worldSpawn: ChunkCoordinates?, val forceWorldSpawn: Boolean): GuiScreen() {
	
	val current = ChunkCoordIntPair(smc.thePlayer.chunkCoordX, smc.thePlayer.chunkCoordZ)
	var selected: ChunkCoordIntPair? = null
	
	var offsetX = -3.5f - current.chunkXPos * 8
	var offsetY = -3.5f - current.chunkZPos * 8
	
	var scale = 1f
	
	override fun drawScreen(mouseX: Int, mouseY: Int, ticks: Float) {
		drawDefaultBackground()
		
		var selectedForced = false
		var selectedBy = ""
		
		val lmX = (mouseX - width / 2 - offsetX) / scale
		val lmY = (mouseY - height / 2 - offsetY) / scale
		
		fun draw(chunks: Map<ChunkCoordIntPair, String>, forced: Boolean) {
			chunks.forEach { (it, v) ->
				val x = it.chunkXPos * 8
				val y = it.chunkZPos * 8
				
				Tessellator.instance.startDrawingQuads()
				Tessellator.instance.setColorOpaque_I(getTypes(it, current, forced).first().color)
				Tessellator.instance.addVertex(x + 0.0, y + 0.0, 0.0)
				Tessellator.instance.addVertex(x + 0.0, y + 7.0, 0.0)
				Tessellator.instance.addVertex(x + 7.0, y + 7.0, 0.0)
				Tessellator.instance.addVertex(x + 7.0, y + 0.0, 0.0)
				Tessellator.instance.draw()
				
				if (lmX in x.F..x + 6f && lmY in y.F..y + 6f) {
					selected = it
					selectedForced = forced
					selectedBy = v
				}
			}
		}
		
		selected = null
		
		glPushMatrix()
		glTranslatef(width / 2 + offsetX, height / 2 + offsetY, 0f)
		glScalef(scale)
		
		glDisable(GL_TEXTURE_2D)
		draw(loaded, false)
		draw(forced, true)
		glEnable(GL_TEXTURE_2D)
		
		glPopMatrix()
		
		fontRendererObj.drawString("\u2B06", width / 2 - 2, 3, 0x00FF00)
		fontRendererObj.drawString("N", width / 2 - 3, 13, 0x00FF00)
		
		fontRendererObj.drawString("\u2B07", width / 2 - 2, height - 10, 0x00FF00)
		fontRendererObj.drawString("S", width / 2 - 3, height - 20, 0x00FF00)
		
		fontRendererObj.drawString("\u2B05", 3, height / 2 - 5, 0x00FF00)
		fontRendererObj.drawString("W", 13, height / 2 - 4, 0x00FF00)
		
		fontRendererObj.drawString("E\u27A1", width - 16, height / 2 - 4, 0x00FF00)
		
		if (selected != null) {
			val list = getTypes(selected!!, current, selectedForced).mapTo(ArrayList()) { StatCollector.translateToLocalFormatted("gui.chunkmap.type.${it.name.lowercase()}", selectedBy) }
			list.add(0, StatCollector.translateToLocalFormatted("gui.chunkmap.chunk", selected.toString()))
			list.add("")
			list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("gui.chunkmap.tp"))
			drawHoveringText(list, mouseX, mouseY, fontRendererObj)
		}
	}
	
	var dragStartX = -1
	var dragStartY = -1
	
	override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
		if (button == 0) {
			dragStartX = mouseX
			dragStartY = mouseY
		} else if (button == 1 && selected != null) {
			NetworkHandler.network.sendToServer(MessageTpRequest(selected!!.chunkXPos * 16 + 8, -1, selected!!.chunkZPos * 16 + 8))
			mc.displayGuiScreen(null)
		}
	}
	
	override fun mouseClickMove(mouseX: Int, mouseY: Int, button: Int, timeSinceMouseClick: Long) {
		if (button != 0 || dragStartX == -1) return
		
		offsetX += mouseX - dragStartX
		offsetY += mouseY - dragStartY
		
		dragStartX = mouseX
		dragStartY = mouseY
	}
	
	override fun mouseMovedOrUp(mouseX: Int, mouseY: Int, button: Int) {
		if (button != 0) return
		
		dragStartX = -1
		dragStartY = -1
	}
	
	override fun handleInput() {
		super.handleInput()
		
		if (!Mouse.isCreated()) return
		
		val prev = scale
		scale = max(0.1f, min(scale + Mouse.getDWheel() * 0.001f, 1f))
		
		offsetX *= scale / prev
		offsetY *= scale / prev
	}
	
	private fun getTypes(target: ChunkCoordIntPair, current: ChunkCoordIntPair, forced: Boolean): List<ChunkType> {
		val list = ArrayList<ChunkType>()
		
		if (target == current) list.add(CURRENT)
		if (spawnpoint in target) list.add(SPAWNPOINT)
		if (forced) list.add(FORCED)
		if (worldSpawn in target) list.add(WORLDSPAWN)
		if (mc.theWorld.getChunkFromChunkCoords(target.chunkXPos, target.chunkZPos).getRandomWithSeed(987234911L).nextInt(10) == 0) list.add(SLIMECHUNK)
		if (isSpawnArea(target) && worldSpawn !in target) list.add(SPAWNAREA)
		
		return if (list.isEmpty()) listOf(LOADED) else list
	}
	
	private fun isSpawnArea(target: ChunkCoordIntPair) = if (forceWorldSpawn && worldSpawn != null) {
		val x = target.chunkXPos * 16 + 8 - worldSpawn.posX
		val z = target.chunkZPos * 16 + 8 - worldSpawn.posZ
		val range = 128
		
		-range <= x && x <= range && -range <= z && z <= range
	} else false
	
	private enum class ChunkType(val color: Int) {
		CURRENT(Color(0x0000FF).rgb),
		FORCED(Color(0xFF0000).rgb),
		LOADED(Color(0xFFFFFF).rgb),
		SPAWNAREA(Color(0xFFFF00).rgb),
		SPAWNPOINT(Color(0xFF00FF).rgb),
		WORLDSPAWN(Color(0x00FFFF).rgb),
		SLIMECHUNK(Color(0x00FF00).rgb),
	}
	
	private operator fun ChunkCoordIntPair.contains(check: ChunkCoordinates?) = if (check == null) false else check.posX shr 4 == chunkXPos && check.posZ shr 4 == chunkZPos
}