package alexsocol.asjlib.render

import alexsocol.asjlib.*
import cpw.mods.fml.common.Loader
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.GL_FALSE
import org.lwjgl.opengl.GL20.*
import java.time.*
import java.util.concurrent.*

/**
 * Almost all code is by Vazkii - ShaderHelper, I just ported it to GL20 and made lib-style
 */
object ASJShaderHelper {
	
	var crashOnError = true
	
	private const val FRAG = GL_FRAGMENT_SHADER
	private const val VERT = GL_VERTEX_SHADER
	
	@JvmOverloads
	fun useShader(shaderID: Int, callback: ((Int) -> Unit)? = null) {
		if (!OpenGlHelper.shadersSupported) return
		
		glUseProgram(shaderID)
		
		if (shaderID != 0) {
			glUniform1f(glGetUniformLocation(shaderID, "ftime"), total / 20f)
			
			callback?.invoke(shaderID)
		}
	}
	
	fun releaseShader() {
		useShader(0)
	}
	
	// Most of the code taken from the LWJGL wiki
	// http://lwjgl.org/wiki/index.php?title=GLSL_Shaders_with_LWJGL
	/**
	 * Creates shader bundle for future using.
	 * Put your shaders to /assets/modid/.
	 * @param vertLocation Vertex shader location
	 * @param fragLocation Fragment shader location
	 */
	fun createProgram(vertLocation: String?, fragLocation: String?): Int {
		return try {
			createProgramInner(vertLocation, fragLocation)
		} catch (e: Throwable) {
			if (crashOnError) throw e
			else 0
		}
	}
	
	private fun createProgramInner(vertLocation: String?, fragLocation: String?): Int {
		if (!OpenGlHelper.shadersSupported) return 0
		
		val vertID: Int
		val fragID: Int
		val programID = glCreateProgram()
		
		if (programID == 0) return 0
		
		if (!vertLocation.isNullOrEmpty()) {
			vertID = createShader(vertLocation, VERT)
			glAttachShader(programID, vertID)
		}
		
		if (!fragLocation.isNullOrEmpty()) {
			fragID = createShader(fragLocation, FRAG)
			glAttachShader(programID, fragID)
		}
		
		glLinkProgram(programID)
		if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE) {
			val info = getProgramLogInfo(programID)
			glDeleteProgram(programID)
			throw RuntimeException("Error Linking program [$vertLocation x $fragLocation]: $info")
		}
		
		glValidateProgram(programID)
		if (glGetProgrami(programID, GL_VALIDATE_STATUS) == GL_FALSE) {
			val info = getProgramLogInfo(programID)
			glDeleteProgram(programID)
			throw RuntimeException("Error Validating program [$vertLocation x $fragLocation]: $info")
		}
		
		return programID
	}
	
	private fun createShader(filename: String, shaderType: Int): Int {
		var shaderID = 0
		try {
			shaderID = glCreateShader(shaderType)
			
			if (shaderID == 0) return 0
			
			glShaderSource(shaderID, readFileAsString(filename))
			glCompileShader(shaderID)
			
			if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE) throw RuntimeException("Error Compiling shader [$filename]: " + getShaderLogInfo(shaderID))
			
			return shaderID
		} catch (e: Exception) {
			glDeleteShader(shaderID)
			e.printStackTrace()
			return -1
		}
	}
	
	private fun getShaderLogInfo(obj: Int): String {
		return glGetShaderInfoLog(obj, glGetShaderi(obj, GL_INFO_LOG_LENGTH))
	}
	
	private fun getProgramLogInfo(obj: Int): String {
		return glGetProgramInfoLog(obj, glGetProgrami(obj, GL_INFO_LOG_LENGTH))
	}
	
	@Throws(Exception::class)
	private fun readFileAsString(filename: String): String {
		return mc.resourceManager.getResource(ResourceLocation(Loader.instance().activeModContainer().modId, filename)).inputStream.readBytes().decodeToString()
	}
	
	// inspired by Vazkii's ClientTickHandler:
	
	private var gameTicks = 0
	private val total get() = gameTicks + mc.timer.renderPartialTicks
	
	init {
		eventFML()
	}
	
	@SubscribeEvent
	fun clientTickEnd(event: ClientTickEvent) {
		if (event.phase != TickEvent.Phase.END || mc.isGamePaused) return
		gameTicks++
	}
}