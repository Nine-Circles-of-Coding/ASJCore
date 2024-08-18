package alexsocol.asjlib.render

import alexsocol.asjlib.*
import cpw.mods.fml.common.Loader
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.resources.*
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.GL_FALSE
import org.lwjgl.opengl.GL20.*
import java.time.*
import java.util.concurrent.*

/**
 * Almost all code is by Vazkii - ShaderHelper, I just ported it to GL20 and made lib-style
 */
object ASJShaderHelper: IResourceManagerReloadListener {
	
	var crashOnError = true
	
	private const val FRAG = GL_FRAGMENT_SHADER
	private const val VERT = GL_VERTEX_SHADER
	
	internal fun registerHandlers() {
		eventFML()
		(mc.resourceManager as? IReloadableResourceManager)?.registerReloadListener(this)
	}
	
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
	@JvmOverloads
	fun createProgram(vertLocation: String?, fragLocation: String?, modid: String = Loader.instance().activeModContainer().modId): Int {
		return try {
			createProgramInner(vertLocation, fragLocation, modid)
		} catch (e: Throwable) {
			if (crashOnError) throw e
			else 0
		}
	}
	
	private fun createProgramInner(vertLocation: String?, fragLocation: String?, modid: String): Int {
		if (!OpenGlHelper.shadersSupported) return 0
		
		val programID = glCreateProgram()
		
		if (programID == 0) return 0
		
		val shader = Shader(programID, modid, vertLocation, fragLocation)
		
		var vertID: Int? = null
		var fragID: Int? = null
		
		if (!vertLocation.isNullOrEmpty()) {
			vertID = createShader(vertLocation, VERT, modid, mc.resourceManager)
			glAttachShader(programID, vertID)
			shader.vertexID = vertID
		}
		
		if (!fragLocation.isNullOrEmpty()) {
			fragID = createShader(fragLocation, FRAG, modid, mc.resourceManager)
			glAttachShader(programID, fragID)
			shader.fragmentID = fragID
		}
		
		glLinkProgram(programID)
		if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE) {
			val info = getProgramLogInfo(programID)
			glDeleteProgram(programID)
			vertID?.let { glDeleteShader(it) }
			fragID?.let { glDeleteShader(it) }
			throw RuntimeException("Error Linking program [$vertLocation x $fragLocation]: $info")
		}
		
		glValidateProgram(programID)
		if (glGetProgrami(programID, GL_VALIDATE_STATUS) == GL_FALSE) {
			val info = getProgramLogInfo(programID)
			glDeleteProgram(programID)
			vertID?.let { glDeleteShader(it) }
			fragID?.let { glDeleteShader(it) }
			throw RuntimeException("Error Validating program [$vertLocation x $fragLocation]: $info")
		}
		
		shaders += shader
		
		return programID
	}
	
	private fun createShader(filename: String, shaderType: Int, modid: String, manager: IResourceManager): Int {
		var shaderID = 0
		try {
			shaderID = glCreateShader(shaderType)
			
			if (shaderID == 0) return 0
			
			glShaderSource(shaderID, readFileAsString(filename, modid, manager))
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
	private fun readFileAsString(filename: String, modid: String, manager: IResourceManager): String {
		return mc.resourceManager.getResource(ResourceLocation(modid, filename)).inputStream.readBytes().decodeToString()
	}
	
	// inspired by Vazkii's ClientTickHandler:
	
	private var gameTicks = 0
	private val total get() = gameTicks + mc.timer.renderPartialTicks
	
	@SubscribeEvent
	fun clientTickEnd(event: ClientTickEvent) {
		if (event.phase != TickEvent.Phase.END || mc.isGamePaused) return
		gameTicks++
	}
	
	// reload handling
	
	val shaders = HashSet<Shader>()
	
	override fun onResourceManagerReload(manager: IResourceManager) {
		shaders.forEach { shader ->
			val (programID, modid, vertLocation, fragLocation, oldVertexID, oldFragmentID) = shader
			
			oldVertexID?.let { glDetachShader(programID, it) }
			oldFragmentID?.let { glDetachShader(programID, it) }
			
			var vertID: Int? = null
			var fragID: Int? = null
			
			try {
				if (!vertLocation.isNullOrEmpty()) {
					vertID = createShader(vertLocation, VERT, modid, mc.resourceManager)
					glAttachShader(programID, vertID)
				}
				
				if (!fragLocation.isNullOrEmpty()) {
					fragID = createShader(fragLocation, FRAG, modid, mc.resourceManager)
					glAttachShader(programID, fragID)
				}
				
				glLinkProgram(programID)
				if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE) {
					val info = getProgramLogInfo(programID)
					vertID?.let { glDeleteShader(it) }
					fragID?.let { glDeleteShader(it) }
					throw RuntimeException("Error Linking program [$vertLocation x $fragLocation]: $info")
				}
				
				glValidateProgram(programID)
				if (glGetProgrami(programID, GL_VALIDATE_STATUS) == GL_FALSE) {
					val info = getProgramLogInfo(programID)
					vertID?.let { glDeleteShader(it) }
					fragID?.let { glDeleteShader(it) }
					throw RuntimeException("Error Validating program [$vertLocation x $fragLocation]: $info")
				}
			} catch (e: RuntimeException) {
				ASJUtilities.error("Exception during shaders reload:", e)
				
				oldVertexID?.let { glAttachShader(programID, it) }
				oldFragmentID?.let { glAttachShader(programID, it) }
				
				return@forEach
			}
			
			oldVertexID?.let { glDeleteShader(it) }
			oldFragmentID?.let { glDeleteShader(it) }
			
			shader.vertexID = vertID
			shader.fragmentID = fragID
		}
	}
	
	data class Shader(val programId: Int, val modid: String, val vertexPath: String?, val fragmentPath: String?) {
		var vertexID: Int? = null
		var fragmentID: Int? = null
		operator fun component5() = vertexID
		operator fun component6() = fragmentID
	}
}