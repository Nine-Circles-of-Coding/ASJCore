@file:Suppress("JoinDeclarationAndAssignment", "UNUSED_VARIABLE", "UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "unused", "VariableNeverRead", "AssignedValueIsNeverRead")

package alexsocol.patcher.asm.hook

import alexsocol.patcher.PatcherConfigHandler
import com.KAIIIAK.classManipulators.HookReplacer
import com.KAIIIAK.classManipulators.HookReplacer.Replacer.*
import cpw.mods.fml.client.GuiModList
import cpw.mods.fml.common.ModContainer
import net.minecraft.block.BlockPane
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.*
import net.minecraft.entity.*
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.ContainerRepair
import net.minecraft.potion.Potion
import net.minecraft.server.gui.StatsComponent
import net.minecraft.tileentity.*
import net.minecraft.util.DamageSource
import net.minecraft.world.World
import net.minecraft.world.biome.BiomeGenJungle
import net.minecraft.world.gen.feature.*
import java.awt.*
import java.util.*

//@formatter:off
// fix oak leaves on jungle shrubs
@HookReplacer(removePop = true)
fun func_150567_a(target: BiomeGenJungle, rand: Random): WorldGenAbstractTree? {
	startFROM()
	WorldGenShrub(3, 0)
	startTO()
	WorldGenShrub(3, 3)
	stop()
	
	return null
}

// add more blindness
@HookReplacer(targetMethod = "setupFog", isMandatory = false)
fun blindnessDegree(er: EntityRenderer, fogMode: Int, ticks: Float) {
	startFROM()
	POP(5f); FSTORE("7")
	startTO()
	POP(5f / (ALOAD<EntityLivingBase>("3").getActivePotionEffect(Potion.blindness).amplifier + 1)); FSTORE("7")
	stop()
}

@HookReplacer(targetMethod = "setupFog", isMandatory = false)
fun blindnessDegreeOF(er: EntityRenderer, fogMode: Int, ticks: Float) {
	startFROM()
	POP(5f); FSTORE("8")
	startTO()
	POP(5f / (ALOAD<EntityLivingBase>("3").getActivePotionEffect(Potion.blindness).amplifier + 1)); FSTORE("8")
	stop()
}

@HookReplacer(targetMethod = "setupFog")
fun lavaFog(er: EntityRenderer, fogMode: Int, ticks: Float) {
	startFROM()
	POP(2.0F)
	startTO()
	POP(lavaFog(ILOAD("4")))
	stop()
}

// flag: actually boolean
fun lavaFog(flag: Int) = if (flag > 0) 0.05f else 2f

@Suppress("unused") // used in class transformer
fun printMissingData(locallyMissing: List<String>) = "Fatally missing blocks and items for mods:\n${locallyMissing.mapTo(HashSet()) { it.split(':')[0] }}"

@HookReplacer(targetMethod = "<init>")
fun GuiSnooperList(list: GuiSnooper.List, parent: GuiSnooper) {
	startFROM()
	POP(80)
	startTO()
	POP(24)
	stop()
}

@HookReplacer
fun renderString(fr: FontRenderer, text: String, x: Int, y: Int, color: Int, shadow: Boolean): Int {
	startFROM()
	POP(-67108864)
	startTO()
	POP(-16777216)
	stop()
	
	return 0
}

@HookReplacer(targetMethod = "<init>", removePop = true)
fun EntityLightningBolt(thiz: EntityLightningBolt, world: World, x: Double, y: Double, z: Double) {
	startFROM()
	world.gameRules.getGameRuleBooleanValue("doFireTick")
	startTO()
	cutOutByASJCore()
	stop()
}

@HookReplacer(targetMethod = "<init>", removePop = true, isMandatory = false) // Bukkit compatibility
fun EntityLightningBolt(thiz: EntityLightningBolt, world: World, x: Double, y: Double, z: Double, isEffect: Boolean) {
	startFROM()
	world.gameRules.getGameRuleBooleanValue("doFireTick")
	startTO()
	cutOutByASJCore()
	stop()
}

fun cutOutByASJCore() = false


// dark theme for server
@HookReplacer(targetMethod = "paint", removePop = true)
fun darkBackground(stats: StatsComponent, graphics: Graphics) {
	startFROM()
	Color(16777215)
	startTO()
	getStatsBackgroundColor()
	stop()
}

fun getStatsBackgroundColor() = if (PatcherConfigHandler.darkMode) Color.DARK_GRAY!! else Color.WHITE!!

@HookReplacer(targetMethod = "paint", removePop = true)
fun whiteText(stats: StatsComponent, graphics: Graphics) {
	startFROM()
	Color.BLACK
	startTO()
	getStatsTextColor()
	stop()
}

fun getStatsTextColor() = if (PatcherConfigHandler.darkMode) Color.WHITE!! else Color.BLACK!!


// iron bars gap
@HookReplacer(targetMethod = "renderBlockPane")
fun fixGapBig(rb: RenderBlocks, block: BlockPane, x: Int, y: Int, z: Int): Boolean {
	startFROM()
	POP(0.01)
	startTO()
	POP(0.001)
	stop()
	
	return false
}

@HookReplacer(targetMethod = "renderBlockPane")
fun fixGapSmall(rb: RenderBlocks, block: BlockPane, x: Int, y: Int, z: Int): Boolean {
	startFROM()
	POP(0.005)
	startTO()
	POP(0.0005)
	stop()
	
	return false
}

fun javaStreamsAreShitSB(elements: Array<String>) = ByteArray(elements.size) { elements[it].trim().dropLast(1).toByte() }
fun javaStreamsAreShitSI(elements: Array<String>) = IntArray(elements.size) { elements[it].trim().toInt() }

@HookReplacer(targetMethod = "func_145891_a")
fun fixHopperHoppingZone(tile: TileEntityHopper, hopper: IHopper?): Boolean {
	startFROM()
	POP(1.0)
	startTO()
	POP(0.5)
	stop()
	
	return false
}

@HookReplacer(targetMethod = "updateRepairOutput")
fun changeAnvilCostLimit(con: ContainerRepair) {
	startFROM()
	POP(40)
	startTO()
	POP(maxAnvilCost())
	stop()
}

@HookReplacer(targetMethod = "updateRepairOutput")
fun changeAnvilCostLower(con: ContainerRepair) {
	startFROM()
	POP(39)
	startTO()
	POP(maxAnvilCost() - 1)
	stop()
}

@HookReplacer(targetMethod = "drawGuiContainerForegroundLayer")
fun changeAnvilCostLimit(gui: GuiRepair, mx: Int, mz: Int) {
	startFROM()
	POP(40)
	startTO()
	POP(maxAnvilCost())
	stop()
}

fun maxAnvilCost() = PatcherConfigHandler.anvilLevelLimit

@HookReplacer(targetMethod = "onCollideWithPlayer")
fun changeXpCooldown(xp: EntityXPOrb, player: EntityPlayer) {
	startFROM()
	POP(2)
	startTO()
	POP(xpCooldown())
	stop()
}

fun xpCooldown() = PatcherConfigHandler.xpCooldown

// MC-219981
@HookReplacer(targetMethod = "onSpawnWithEgg")
fun fixLeaderHealth(thiz: EntityZombie, data: IEntityLivingData): IEntityLivingData? {
	startFROM()
	thiz.func_146070_a(true)
	startTO()
	thiz.func_146070_a(true)
	thiz.health = thiz.maxHealth
	stop()
	
	return null
}

@HookReplacer(targetMethod = "attackEntityFrom", removePop = true)
fun fixPigZombieAidType(thiz: EntityZombie, src: DamageSource?, amount: Float): Boolean {
	startFROM()
	EntityZombie(thiz.worldObj)
	startTO()
	getAidEntity(thiz)
	stop()
	
	return false
}

fun getAidEntity(thiz: EntityZombie) = try {
	thiz.javaClass.getConstructor(World::class.java).apply { setAccessible(true) }.newInstance(thiz.worldObj) as EntityZombie
} catch (e: Exception) {
	EntityZombie(thiz.worldObj) // увы
}

@HookReplacer(targetMethod = "onLivingUpdate", removePop = true)
fun fixBabyZombieNotBurning(thiz: EntityZombie) {
	startFROM()
	thiz.isChild
	startTO()
	checkChildBurning(thiz)
	stop()
}

fun checkChildBurning(thiz: EntityZombie) = !PatcherConfigHandler.burningZombieChildren && thiz.isChild

@HookReplacer(targetMethod = "drawScreen")
fun addDependenciesInfo(thiz: GuiModList, mouseX: Int, mouseY: Int, ticks: Float) {
	startFROM()
	POP(thiz.width - ILOAD("4") - 20)
	startTO()
	POP(addDependenciesInfo(thiz, ILOAD("4"), ILOAD("5"), thiz.selectedMod))
	ISTORE("5")
	POP(thiz.width - ILOAD("4") - 21) // slight change to not trigger injection again -_-
	stop()
}

fun addDependenciesInfo(thiz: GuiModList, offset: Int, shifty: Int, mod: ModContainer): Int {
	var shift = shifty
	
	fun draw(list: Set<String>, label: String) {
		shift = thiz.drawLine(list.joinToString(", ", label), offset, shift)
	}
	
	val reqs = mod.requirements.mapTo(HashSet()) { it.label }.apply { remove("Forge") }
	if (reqs.isNotEmpty()) draw(reqs, "Requirements: ")
	
	val deps = mod.dependencies.mapTo(HashSet()) { it.label }.apply { remove("Forge") }.apply { removeAll(reqs) }
	if (deps.isNotEmpty()) draw(deps, "Dependencies: ")
	
	val dets = mod.dependants.mapTo(HashSet()) { it.label }.apply { remove("Forge") }
	if (dets.isNotEmpty()) draw(dets, "Dependants: ")
	
	return shift
}

@HookReplacer(targetMethod = "drawScreen")
fun alwaysDrawInfo(thiz: GuiModList, mouseX: Int, mouseY: Int, ticks: Float) {
	startFROM()
	POP(thiz.selectedMod.metadata.autogenerated)
	startTO()
	POP(false)
	stop()
}
//@formatter:on