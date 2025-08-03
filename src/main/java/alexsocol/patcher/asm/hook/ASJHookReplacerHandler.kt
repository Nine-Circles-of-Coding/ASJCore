@file:Suppress("JoinDeclarationAndAssignment", "UNUSED_VARIABLE", "UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "unused", "VariableNeverRead", "AssignedValueIsNeverRead")

package alexsocol.patcher.asm.hook

import com.KAIIIAK.classManipulators.HookReplacer
import com.KAIIIAK.classManipulators.HookReplacer.Replacer.*
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.EntityRenderer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.potion.Potion
import net.minecraft.world.World
import net.minecraft.world.biome.BiomeGenJungle
import net.minecraft.world.gen.feature.*
import java.util.*

// fix oak leaves on jungle shrubs
@HookReplacer
fun func_150567_a(target: BiomeGenJungle, rand: Random): WorldGenAbstractTree? {
	startFROM()
	POPLine();POP(WorldGenShrub(3, 0))
	POPLine();startTO()
	POPLine();POP(WorldGenShrub(3, 3))
	POPLine();stop()
	
	return null
}

// add more blindness
@HookReplacer(targetMethod = "setupFog")
fun blindnessDegree(er: EntityRenderer, fogMode: Int, ticks: Float) {
	val entitylivingbase: EntityLivingBase = er.mc.renderViewEntity
	val flag = false
	val block = null
	val event = null
	var f1: Float
	startFROM()
	POPLine();f1 = 5.0f
	POPLine();startTO()
	POPLine();f1 = 5f / (entitylivingbase.getActivePotionEffect(Potion.blindness).amplifier + 1)
	POPLine();stop()
}

@HookReplacer(targetMethod = "setupFog")
fun lavaFog(er: EntityRenderer, fogMode: Int, ticks: Float) {
	val entitylivingbase: EntityLivingBase = er.mc.renderViewEntity
	val flag = false
	startFROM()
	POPLine();POP(2.0F)
	POPLine();startTO()
	POPLine();POP(lavaFog(flag))
	POPLine();stop()
}

fun lavaFog(flag: Boolean) = if (flag) 0.05f else 2f

@Suppress("unused") // used in class transformer
fun printMissingData(locallyMissing: List<String>) = "Fatally missing blocks and items for mods:\n${locallyMissing.mapTo(HashSet()) { it.split(':')[0] }}"

@HookReplacer(targetMethod = "<init>")
fun GuiSnooperList(list: GuiSnooper.List, parent: GuiSnooper) {
	startFROM()
	POPLine();POP(80)
	POPLine();startTO()
	POPLine();POP(24)
	POPLine();stop()
}

@HookReplacer
fun renderString(fr: FontRenderer, text: String, x: Int, y: Int, color: Int, shadow: Boolean): Int {
	startFROM()
	POPLine();POP(-67108864)
	POPLine();startTO()
	POPLine();POP(-16777216)
	POPLine();stop()
	
	return 0
}

@HookReplacer(targetMethod = "<init>")
fun EntityLightningBolt(thiz: EntityLightningBolt, world: World, x: Double, y: Double, z: Double) {
	startFROM()
	POPLine();POP(world.gameRules.getGameRuleBooleanValue("doFireTick"))
	POPLine();startTO()
	POPLine();POP(cutOutByASJCore())
	POPLine();stop()
}

@HookReplacer(targetMethod = "<init>") // Bukkit compatibility
fun EntityLightningBolt(thiz: EntityLightningBolt, world: World, x: Double, y: Double, z: Double, isEffect: Boolean) {
	startFROM()
	POPLine();POP(world.gameRules.getGameRuleBooleanValue("doFireTick"))
	POPLine();startTO()
	POPLine();POP(cutOutByASJCore())
	POPLine();stop()
}

fun cutOutByASJCore() = false