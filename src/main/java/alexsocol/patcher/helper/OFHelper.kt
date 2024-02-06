package alexsocol.patcher.helper

import Config
import alexsocol.asjlib.ASJReflectionHelper
import alexsocol.patcher.PatcherConfigHandler
import net.minecraft.client.renderer.EntityRenderer
import org.lwjgl.opengl.*
import org.lwjgl.opengl.NVFogDistance.*
import shadersmod.client.Shaders
import kotlin.math.min

object OFHelper {
	
	val optifine by lazy {
		try {
			Class.forName("Config")
			true
		} catch (e: ClassNotFoundException) {
			false
		}
	}
	
	val shadersmod by lazy {
		try {
			Class.forName("shadersmod.client.Shaders")
			true
		} catch (e: ClassNotFoundException) {
			false
		}
	}
	
	val fogStandard by lazy { ASJReflectionHelper.getField(EntityRenderer::class.java, "fogStandard") }
	
	fun setStandardFog(obj: EntityRenderer, fog: Boolean) {
		if (fogStandard == null) return
		ASJReflectionHelper.setValue(fogStandard, obj, fog, false)
	}
	
	fun fancyFogCheck() {
		if (optifine) {
			if (Config.isFogFancy())
				shadersmodSupport(GL_FOG_DISTANCE_MODE_NV, GL_EYE_RADIAL_NV)
		} else if (GLContext.getCapabilities().GL_NV_fog_distance)
			shadersmodSupport(GL_FOG_DISTANCE_MODE_NV, GL_EYE_RADIAL_NV)
	}
	
	fun isFogFancy() = if (optifine) Config.isFogFancy() else true
	
	fun isFogFast() = if (optifine) Config.isFogFast() else false
	
	fun isClearWater() = if (optifine) Config.isClearWater() else PatcherConfigHandler.clearWater
	
	fun isVoidFog() = if (optifine) Config.isDepthFog() else PatcherConfigHandler.voidFog
	
	fun getFogStart() = if (optifine) Config.getFogStart() else 0.75f
	
	fun shadersmodSupport(pname: Int, param: Int) {
		if (shadersmod && Shaders.shaderPackLoaded)
			Shaders.sglFogi(pname, param)
		else
			GL11.glFogi(pname, param)
	}
	
	fun XZFog(farPlane: Float) {
		if (optifine) {
			GL11.glFogf(GL11.GL_FOG_START, farPlane * 0.05f)
			GL11.glFogf(GL11.GL_FOG_END, farPlane)
		} else {
			GL11.glFogf(GL11.GL_FOG_START, farPlane * 0.05f)
			GL11.glFogf(GL11.GL_FOG_END, min(farPlane, 192f) * 0.5f)
		}
	}
}