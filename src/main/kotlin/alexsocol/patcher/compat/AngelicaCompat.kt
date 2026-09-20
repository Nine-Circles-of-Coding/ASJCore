package alexsocol.patcher.compat

import alexsocol.asjlib.D
import alexsocol.patcher.handler.KeyBindingHandler
import net.minecraft.client.renderer.*
import org.lwjgl.opengl.GL11.*

/**
 * Intermediate class for calls that should be remapped by Angelica
 * Invoked from code in @TransformerExclusions
 * Exctracted calls to:
 * - org.lwjgl.opengl.GL11
 * - ...
 */
object AngelicaCompat {
	
	fun drawActivePotionEffectsPre() {
		glEnable(GL_BLEND)
		OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
	}
	
	fun renderVignette() {
		OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0)
	}
	
	fun deleteDisplayLists(id: Int) {
		glDeleteLists(id, GLAllocation.mapDisplayLists.remove(id) as Int)
	}
	
	@JvmStatic
	fun switchToOrtho(er: EntityRenderer, f: Float, clipDistance: Float) {
		if (!KeyBindingHandler.orthoProjectionState) return
		
		glLoadIdentity()
		val mod = er.getFOVModifier(f, true) * 2.0
		glOrtho(er.mc.displayWidth / -mod, er.mc.displayWidth / mod, er.mc.displayHeight / -mod, er.mc.displayHeight / mod, 0.05, clipDistance.D)
	}
	
	fun glFogiHook(pname: Int, param: Int) {
		if (pname != GL_FOG_MODE) return
		
		if (param == GL_LINEAR) {
			glFogf(GL_FOG_DENSITY, 0f)
		} else if (param == GL_EXP || param == GL_EXP2) {
			glFogf(GL_FOG_START, 0f)
			glFogf(GL_FOG_END, 0f)
		}
	}
}