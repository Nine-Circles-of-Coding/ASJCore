package alexsocol.patcher.client

import alexsocol.asjlib.*
import alexsocol.patcher.PatcherMain
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.Tessellator
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.RenderGameOverlayEvent

object WolkJpegOverlay {
	
	var ticksActive = 0
	val texture = ResourceLocation(PatcherMain.MODID, "textures/wolk.jpeg")
	
	init {
		eventForge().eventFML()
	}
	
	@SubscribeEvent
	fun drawWolk(event: RenderGameOverlayEvent.Post) {
		if (event.type != RenderGameOverlayEvent.ElementType.HELMET) return
		
		if (ticksActive <= 0) return
		
		val uC = (ticksActive / 5) % 2
		val UC = 1 - (ticksActive / 5) % 2
		
		mc.renderEngine.bindTexture(texture)
		val res = ScaledResolution(mc, mc.displayWidth, mc.displayHeight)
		val u = res.scaledWidth
		val v = res.scaledHeight
		val tes = Tessellator.instance
		tes.startDrawingQuads()
		tes.addVertexWithUV(0.0, 0.0, 0.0, uC.D, 0.0)
		tes.addVertexWithUV(0.0, v.D, 0.0, uC.D, 1.0)
		tes.addVertexWithUV(u.D, v.D, 0.0, UC.D, 1.0)
		tes.addVertexWithUV(u.D, 0.0, 0.0, UC.D, 0.0)
		tes.draw()
	}
	
	@SubscribeEvent
	fun tick(event: TickEvent.ClientTickEvent) {
		if (ticksActive > 0) --ticksActive
	}
}
