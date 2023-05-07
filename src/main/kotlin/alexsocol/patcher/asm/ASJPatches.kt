@file:Suppress("unused")

package alexsocol.patcher.asm

import alexsocol.asjlib.ASJReflectionHelper
import alexsocol.patcher.PatcherConfigHandler
import net.minecraft.entity.EntityList
import net.minecraft.entity.boss.EntityWither

object ASJPatches {
	
	@Suppress("LocalVariableName", "unused")
	@JvmStatic
	fun patchNeiNoWither() {
		if (!PatcherConfigHandler.blacklistWither) return
		
		try {
			val ItemMobSpawner: Class<*> = Class.forName("codechicken.nei.ItemMobSpawner")
			val IDtoNameMap: MutableMap<Int, String> = ASJReflectionHelper.getStaticValue(ItemMobSpawner, "IDtoNameMap") ?: return
			
			IDtoNameMap.remove(EntityList.classToIDMapping[EntityWither::class.java])
		} catch (e: Throwable) {
			return
		}
	}
}