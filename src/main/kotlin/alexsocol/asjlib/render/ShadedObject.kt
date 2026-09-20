package alexsocol.asjlib.render

import net.minecraft.util.ResourceLocation
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.util.vector.Matrix4f
import java.util.*

abstract class ShadedObject(val shaderID: Int, val materialID: Int, val texture: ResourceLocation?): Comparable<ShadedObject> {
	
	val translations = ArrayList<Pair<Matrix4f, Array<out Any?>>>()
	
	abstract fun preRender()
	
	fun addTranslation(vararg additionalData: Any?) {
		glGetFloat(GL_MODELVIEW_MATRIX, usableBuffer)
		translations.add(Matrix4f().load(usableBuffer) as Matrix4f to additionalData)
		usableBuffer.clear()
	}
	
	open fun drawMesh(data: Array<out Any?>) = Unit
	
	fun doRender() {
		glMatrixMode(GL_MODELVIEW)
		glPushMatrix()
		for ((translation, data) in translations) {
			glLoadIdentity()
			translation.store(usableBuffer)
			usableBuffer.flip()
			glMultMatrix(usableBuffer)
			usableBuffer.clear()
				drawMesh(data)
		}
		glPopMatrix()
	}
	
	abstract fun postRender()
	
	override fun compareTo(other: ShadedObject): Int {
		if (shaderID < other.shaderID) return -1
		if (shaderID > other.shaderID) return 1
		if (materialID < other.materialID) return -1
		return if (materialID > other.materialID) 1 else 0
	}
	
	override fun equals(other: Any?) =
		if (other is ShadedObject) compareTo((other as ShadedObject?)!!) == 0 else super.equals(other)
	
	override fun hashCode() =
		Objects.hash(shaderID, materialID, texture)
	
	companion object {
		private val usableBuffer = BufferUtils.createFloatBuffer(32)
	}
}