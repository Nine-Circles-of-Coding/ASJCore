@file:Suppress("JoinDeclarationAndAssignment", "UNUSED_VARIABLE", "UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "unused", "VariableNeverRead", "AssignedValueIsNeverRead")

package alexsocol.patcher.asm.hook

import alexsocol.patcher.PatcherConfigHandler
import com.KAIIIAK.classManipulators.HookReplacer
import com.KAIIIAK.classManipulators.HookReplacer.Replacer.*
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


// dark theme for server
@HookReplacer(targetMethod = "paint")
fun darkBackground(stats: StatsComponent, graphics: Graphics) {
	startFROM()
	POPLine();POP(Color(16777215))
	POPLine();startTO()
	POPLine();POP(getStatsBackgroundColor())
	POPLine();stop()
}

fun getStatsBackgroundColor() = if (PatcherConfigHandler.darkMode) Color.DARK_GRAY else Color.WHITE

@HookReplacer(targetMethod = "paint")
fun whiteText(stats: StatsComponent, graphics: Graphics) {
	startFROM()
	POPLine();POP(Color.BLACK)
	POPLine();startTO()
	POPLine();POP(getStatsTextColor())
	POPLine();stop()
}

fun getStatsTextColor() = if (PatcherConfigHandler.darkMode) Color.WHITE else Color.BLACK


// iron bars gap
@HookReplacer(targetMethod = "renderBlockPane")
fun fixGapBig(rb: RenderBlocks, block: BlockPane, x: Int, y: Int, z: Int): Boolean {
	startFROM()
	POPLine();POP(0.01)
	POPLine();startTO()
	POPLine();POP(0.001)
	POPLine();stop()
	
	return false
}

@HookReplacer(targetMethod = "renderBlockPane")
fun fixGapSmall(rb: RenderBlocks, block: BlockPane, x: Int, y: Int, z: Int): Boolean {
	startFROM()
	POPLine();POP(0.005)
	POPLine();startTO()
	POPLine();POP(0.0005)
	POPLine();stop()
	
	return false
}

fun javaStreamsAreShitSB(elements: Array<String>) = ByteArray(elements.size) { elements[it].trim().dropLast(1).toByte() }
fun javaStreamsAreShitSI(elements: Array<String>) = IntArray(elements.size) { elements[it].trim().toInt() }

@HookReplacer(targetMethod = "func_145891_a")
fun fixHopperHoppingZone(tile: TileEntityHopper, hopper: IHopper?): Boolean {
	startFROM()
	POPLine();POP(1.0)
	POPLine();startTO()
	POPLine();POP(0.5)
	POPLine();stop()
	
	return false
}

@HookReplacer(targetMethod = "updateRepairOutput")
fun changeAnvilCostLimit(con: ContainerRepair) {
	startFROM();POP(40);startTO();POP(maxAnvilCost());stop()
}

@HookReplacer(targetMethod = "updateRepairOutput")
fun changeAnvilCostLower(con: ContainerRepair) {
	startFROM();POP(39);startTO();POP(maxAnvilCost() - 1);stop()
}

@HookReplacer(targetMethod = "drawGuiContainerForegroundLayer")
fun changeAnvilCostLimit(gui: GuiRepair, mx: Int, mz: Int) {
	startFROM();POP(40);startTO();POP(maxAnvilCost());stop()
}

fun maxAnvilCost() = PatcherConfigHandler.anvilLevelLimit

@HookReplacer(targetMethod = "onCollideWithPlayer")
fun changeXpCooldown(xp: EntityXPOrb, player: EntityPlayer) {
	startFROM();POP(2);startTO();POP(xpCooldown());stop()
}

fun xpCooldown() = PatcherConfigHandler.xpCooldown

// MC-219981
@HookReplacer(targetMethod = "onSpawnWithEgg")
fun fixLeaderHealth(thiz: EntityZombie, data: IEntityLivingData): IEntityLivingData? {
	startFROM()
	POPLine();thiz.func_146070_a(true)
	POPLine();startTO()
	POPLine();thiz.func_146070_a(true)
	POPLine();thiz.health = thiz.maxHealth
	POPLine();stop()
	
	return null
}

@HookReplacer(targetMethod = "attackEntityFrom")
fun fixPigZombieAidType(thiz: EntityZombie, src: DamageSource?, amount: Float): Boolean {
	startFROM()
	POPLine();POP(EntityZombie(thiz.worldObj))
	POPLine();startTO()
	POPLine();POP(getAidEntity(thiz))
	POPLine();stop()
	
	return false
}

fun getAidEntity(thiz: EntityZombie) = try {
	thiz.javaClass.getConstructor(World::class.java).apply { setAccessible(true) }.newInstance(thiz.worldObj) as EntityZombie
} catch (e: Exception) {
	EntityZombie(thiz.worldObj) // увы
}

@HookReplacer(targetMethod = "onLivingUpdate")
fun fixBabyZombieNotBurning(thiz: EntityZombie) {
	startFROM()
	POPLine();POP(thiz.isChild)
	POPLine();startTO()
	POPLine();POP(checkChildBurning(thiz))
	POPLine();stop()
}

fun checkChildBurning(thiz: EntityZombie) = !PatcherConfigHandler.burningZombieChildren && thiz.isChild
//@formatter:on