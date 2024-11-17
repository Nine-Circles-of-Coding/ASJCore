package alexsocol.patcher.asm.hook

import com.KAIIIAK.classManipulators.HookReplacer
import com.KAIIIAK.classManipulators.HookReplacer.Replacer.*
import net.minecraft.world.biome.BiomeGenJungle
import net.minecraft.world.gen.feature.*
import java.util.*

@Suppress("unused")
object ASJHookReplacerHandler {
	
	// fix oak leaves on jungle shrubs
	@JvmStatic
	@HookReplacer
	fun func_150567_a(target: BiomeGenJungle, rand: Random): WorldGenAbstractTree? {
		startFROM()
		POPLine();POP(WorldGenShrub(3, 0))
		POPLine();startTO()
		POPLine();POP(WorldGenShrub(3, 3))
		POPLine();stop()
		
		return null
	}
}