package alexsocol.asjlib.render

import alexsocol.asjlib.eventForge
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.relauncher.*
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.RenderWorldLastEvent

object RenderPostShaders {
	
	var allowShaders = true
	
	val shaders = ArrayList<ShadedObject>()
	private var renderObjectMaterialID = 0
	
	val nextAvailableRenderObjectMaterialID: Int
		get() = renderObjectMaterialID++
	
	init {
		eventForge()
	}
	
	fun registerShadedObject(renobj: ShadedObject) {
		shaders.add(renobj)
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	fun onWorldLastRender(e: RenderWorldLastEvent) {
		dispatchObjects()
	}
	
	fun dispatchObjects() {
		if (shaders.isEmpty()) return
		shaders.sort()
		
		var post = false
		var prevShader = 0
		val prevMaterial = -1
		var prevObj = shaders[0]
		
		val pt = prevObj.texture
		if (pt != null) {
			if (pt is ResourceLocationAnimated)
				pt.bind()
			else
				Minecraft.getMinecraft().renderEngine.bindTexture(pt)
		}
		
		for (obj in shaders) {
			if (obj.translations.isEmpty()) continue
			if (post && obj.materialID != prevObj.materialID) {
				prevObj.postRender()
			}
			if (obj.shaderID != prevShader) {
				if (allowShaders) ASJShaderHelper.useShader(obj.shaderID)
				prevShader = obj.shaderID
			}
			if (obj.materialID != prevMaterial) obj.preRender()
			
			val ot = obj.texture
			if (ot != null && ot !== prevObj.texture) {
				if (ot is ResourceLocationAnimated)
					ot.bind()
				else
					Minecraft.getMinecraft().renderEngine.bindTexture(ot)
			}
			
			obj.doRender()
			obj.translations.clear()
			prevObj = obj
			post = true
		}
		if (post) prevObj.postRender()
		
		if (allowShaders) ASJShaderHelper.releaseShader()
	}
}