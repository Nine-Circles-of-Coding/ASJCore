package alexsocol.patcher.helper

import net.minecraft.entity.DataWatcher

@Suppress("UNCHECKED_CAST")
object FuckingSpigotFix {
	val dataWatcher_dataTypes get(): Map<Class<*>, Int> = DataWatcher.dataTypes as Map<Class<*>, Int>
}