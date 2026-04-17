package alexsocol.patcher.handler

import alexsocol.asjlib.mc
import alexsocol.patcher.PatcherConfigHandler
import alexsocol.patcher.asm.hook.NoEntityInteractionHandler
import cpw.mods.fml.client.registry.ClientRegistry
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent
import cpw.mods.fml.relauncher.*
import net.minecraft.client.settings.KeyBinding
import org.lwjgl.input.Keyboard

object KeyBindingHandler {
	
	private var toggleOrtho = false
	private var toggleNI = false
	
	val keyOrtho = KeyBinding("asjcore.orthoProjection", Keyboard.KEY_F6, "key.categories.misc")
	val keyNI = KeyBinding("asjcore.noEntityInteract", Keyboard.KEY_F10, "key.categories.misc")
	
	var orthoProjectionState = false
	
	init {
		if (PatcherConfigHandler.orthoProjectionOn) ClientRegistry.registerKeyBinding(keyOrtho)
		ClientRegistry.registerKeyBinding(keyNI)
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	fun parseKeybinding(e: ClientTickEvent) {
		if (mc.thePlayer == null || mc.theWorld == null) return
		
		if (isPressed(keyNI)) {
			if (!toggleNI) {
				toggleNI = true
				NoEntityInteractionHandler.noInteract = !NoEntityInteractionHandler.noInteract
			}
		} else if (toggleNI) {
			toggleNI = false
		}
		
		if (PatcherConfigHandler.orthoProjectionOn) {
			if (isPressed(keyOrtho)) {
				if (!toggleOrtho) {
					toggleOrtho = true
					orthoProjectionState = !orthoProjectionState
				}
			} else if (toggleOrtho) {
				toggleOrtho = false
			}
		}
	}
	
	private fun isPressed(key: KeyBinding): Boolean {
		return if (key.keyCode == 0) false else try {
			Keyboard.isKeyDown(key.keyCode)
		} catch (_: IndexOutOfBoundsException) {
			false
		}
	}
}