package alexsocol.asjlib.render

import alexsocol.asjlib.*
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.client.resources.IResource
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.InputStream
import javax.imageio.ImageIO
import kotlin.streams.toList

class ResourceLocationAnimated: ResourceLocation {
	
	lateinit var frameList: IntArray
	var framerate: Int = 1
	
	private constructor(): super("textures/entity/steve.png") {
		frameList = IntArray(0)
	}
	
	private constructor(mod: String, path: String): super(mod, path) {
		init(getResourceSafe(ResourceLocation(resourceDomain, "$resourcePath.meta"))?.inputStream, mc.resourceManager.getResource(this).inputStream)
	}
	
	private fun init(metaStream: InputStream?, imageStream: InputStream): ResourceLocationAnimated {
		val meta = metaStream?.bufferedReader()?.lines()?.toList()
		framerate = getMeta(meta, MARKER_FRAMERATE, 1)
		frameList = loadImage(getMeta(meta, MARKER_HEIGHT, 0), imageStream, getMeta(meta, MARKER_INTERPOLATION_STEPS, 1), getMeta(meta, MARKER_BLUR, false), getMeta(meta, MARKER_CLAMP, false))
		return this
	}
	
	private fun getMeta(meta: List<String>?, marker: String, default: Int) = (meta?.firstOrNull { it.startsWith(marker) } ?: "$marker$default").replace(marker, "").toInt()
	
	private fun getMeta(meta: List<String>?, marker: String, default: Boolean) = (meta?.firstOrNull { it.startsWith(marker) } ?: "$marker$default").replace(marker, "").toBoolean()
	
	private fun loadImage(h: Int, imageStream: InputStream, interpolationFrames: Int, blur: Boolean, clamp: Boolean): IntArray {
		val image = ImageIO.read(imageStream)
		val height = if (h == 0) image.width else h
		val frameList = ArrayList<Int>()
		
		var first: BufferedImage? = null
		var prev: BufferedImage? = null
		
		val interpolate = interpolationFrames > 1
		
		for (i in 0 until image.height step height) {
			val part = image.getSubimage(0, i, image.width, height)
			
			if (interpolate) {
				if (first == null) {
					first = part
					prev = first
					put(frameList, first, blur, clamp)
					continue
				}
				
				doInterpolation(prev!!, part, frameList, interpolationFrames, false, blur, clamp)
				prev = part
			} else {
				put(frameList, part, blur, clamp)
			}
		}
		
		if (interpolate) doInterpolation(prev!!, first!!, frameList, interpolationFrames, true, blur, clamp)
		
		return frameList.toIntArray()
	}
	
	private fun doInterpolation(prev: BufferedImage, cur: BufferedImage, frameList: ArrayList<Int>, step: Int, last: Boolean, blur: Boolean, clamp: Boolean) {
		val fraction = 1.0 / step
		val width = cur.width
		val height = cur.height
		
		for (i in 1 until step) {
			val interstep = BufferedImage(width, height, cur.type)
			
			for (u in 0 until width) {
				for (v in 0 until height) {
					interstep.setRGB(u, v, interpolateColor(prev.getRGB(u, v), cur.getRGB(u, v), fraction * i))
				}
			}
			
			put(frameList, interstep, blur, clamp)
		}
		
		if (!last) put(frameList, cur, blur, clamp)
	}
	
	private fun interpolateColor(color1: Int, color2: Int, fraction: Double): Int {
		val c1 = Color(color1, true)
		val c2 = Color(color2, true)
		return Color(interpolateChannel(c1.red, c2.red, fraction), interpolateChannel(c1.green, c2.green, fraction), interpolateChannel(c1.blue, c2.blue, fraction), interpolateChannel(c1.alpha, c2.alpha, fraction)).rgb
	}
	
	private fun interpolateChannel(channel1: Int, channel2: Int, fraction: Double) = ((channel2 - channel1) * fraction + channel1).I
	
	private fun put(frameList: ArrayList<Int>, part: BufferedImage, blur: Boolean, clamp: Boolean) = frameList.add(TextureUtil.uploadTextureImageAllocate(GL11.glGenTextures(), part, blur, clamp))
	
	fun getCurrentFrame(): Int {
		if (ASJUtilities.isServer) return 0
		return frameList[(((mc.theWorld?.totalWorldTime ?: 0L) % (framerate * frameList.size)) / framerate).I]
	}
	
	fun bind() {
		if (ASJUtilities.isClient)
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, getCurrentFrame())
	}
	
	private fun getResourceSafe(loc: ResourceLocation): IResource? {
		return try {
			mc.resourceManager.getResource(loc)
		} catch (e: Throwable) {
			ASJUtilities.error("Error loading $loc", e)
			null
		}
	}
	
	companion object {
		
		private const val MARKER_BLUR = "blur="
		private const val MARKER_CLAMP = "clamp="
		private const val MARKER_FRAMERATE = "framerate="
		private const val MARKER_HEIGHT = "height="
		private const val MARKER_INTERPOLATION_STEPS = "interpolation="
		
		fun custom(metaStream: InputStream, imageStream: InputStream): ResourceLocationAnimated {
			val rla = ResourceLocationAnimated()
			if (ASJUtilities.isClient) rla.init(metaStream, imageStream)
			return rla
		}
		
		fun local(mod: String, path: String): ResourceLocationAnimated {
			if (ASJUtilities.isServer) return ResourceLocationAnimated()
			return ResourceLocationAnimated(mod, path)
		}
	}
}