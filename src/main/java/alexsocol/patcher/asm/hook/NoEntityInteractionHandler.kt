package alexsocol.patcher.asm.hook

import alexsocol.asjlib.*
import cpw.mods.fml.client.registry.ClientRegistry
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent
import cpw.mods.fml.relauncher.*
import gloomyfolken.hooklib.asm.*
import net.minecraft.client.renderer.EntityRenderer
import net.minecraft.client.settings.KeyBinding
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.world.World
import org.lwjgl.input.*
import java.util.ArrayList

@Suppress("unused")
object NoEntityInteractionHandler {
	
	var hookEntities = false
	var noInteract = false
	var toggleNI = false
	
	val keyNI = KeyBinding("key.keyNI.desc", Keyboard.KEY_F10, "key.categories.misc")
	
	init {
		ClientRegistry.registerKeyBinding(keyNI)
	}
	
	@JvmStatic
	@Hook(targetMethod = "getMouseOver")
	@SideOnly(Side.CLIENT)
	fun getMouseOverPre(er: EntityRenderer, ticks: Float) {
		if (noInteract) hookEntities = true
	}
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL)
	@SideOnly(Side.CLIENT)
	fun getEntitiesWithinAABBExcludingEntity(world: World, from: Entity?, aabb: AxisAlignedBB?) = if (hookEntities) ArrayList<Entity>() else null
	
	@JvmStatic
	@Hook(targetMethod = "getMouseOver", injectOnExit = true)
	@SideOnly(Side.CLIENT)
	fun getMouseOverPost(er: EntityRenderer, ticks: Float) {
		hookEntities = false
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	fun parseKeybinding(e: ClientTickEvent) {
		if (mc.thePlayer == null || mc.theWorld == null) return
		
		if (isPressed()) {
			if (!toggleNI) {
				toggleNI = true
				noInteract = !noInteract
				
				ASJUtilities.say(mc.thePlayer, "key.keyNI.is.$noInteract")
			}
		} else if (toggleNI) {
			toggleNI = false
		}
	}
	
	private fun isPressed(): Boolean {
		return try {
			Keyboard.isKeyDown(keyNI.keyCode)
		} catch (e: IndexOutOfBoundsException) {
			false
		}
	}
}