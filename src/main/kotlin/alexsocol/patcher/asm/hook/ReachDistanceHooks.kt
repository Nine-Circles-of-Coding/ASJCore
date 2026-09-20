package alexsocol.patcher.asm.hook

import alexsocol.asjlib.*
import alexsocol.patcher.handler.*
import com.KAIIIAK.classManipulators.*
import com.KAIIIAK.classManipulators.HookReplacer.Replacer.*
import com.gildedgames.the_aether.client.renders.*
import com.google.common.collect.*
import cpw.mods.fml.relauncher.*
import gloomyfolken.hooklib.asm.*
import gloomyfolken.hooklib.asm.ReturnCondition.*
import lotr.common.enchant.LOTREnchantmentHelper
import mods.battlegear2.items.*
import net.minecraft.client.multiplayer.*
import net.minecraft.client.renderer.EntityRenderer
import net.minecraft.entity.*
import net.minecraft.entity.ai.attributes.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.network.*
import net.minecraft.network.play.client.*
import net.minecraft.server.management.*
import net.minecraftforge.event.entity.player.*
import java.lang.reflect.*
import java.util.*
import kotlin.math.*

// Partly migrated. The plain hooks are now mixins - see alexsocol.mixins.client.MixinPlayerControllerMP
// and alexsocol.mixins.compat.ReachDistanceMixins / MixinAbilityCalls - and this object keeps their
// bodies plus the @HookReplacer entries, which are a separate mechanism and stay on KASMLib.
//
// The ten @Hook methods left below all route through ASJSuperWrapperHandler to reach a super
// implementation, and @SuperWrapper is deliberately out of scope for the mixin migration. Nine of
// them are also createMethod = true, i.e. they add a method the target does not declare, on mods
// that have no published coordinate - so they would have to be @Pseudo mixins that both merge a
// new method and call super, which is past what @Pseudo reliably supports and is not testable
// here. They stay on HookLib until @SuperWrapper itself is dealt with.
@Suppress("unused")
object ReachDistanceHooks {
	
	// ######## VANILLA ########
	
	@SideOnly(Side.CLIENT)
	@JvmStatic
	fun getBlockReachDistance(pcmp: PlayerControllerMP) = PlayerReachDistanceHandler.getReachDistance(mc.thePlayer).F
	
	@JvmStatic
	@HookReplacer(removePop = true)
	fun processUseEntity(nhps: NetHandlerPlayServer, packet: C02PacketUseEntity?) {
		startFROM()
		DLOAD("5")
		startTO()
		PlayerReachDistanceHandler.getReachDistance(nhps.playerEntity).pow(2.0)
		stop()
	}
	
	@JvmStatic
	@HookReplacer(removePop = true) // fucking Crucible bitches overwriting my changes so post-transforming -_-
	fun getBlockReachDistance(iiwm: ItemInWorldManager): Double {
		startFROM()
		iiwm.blockReachDistance
		startTO()
		PlayerReachDistanceHandler.getReachDistance(iiwm.thisPlayerMP)
		stop()
		
		return 0.0
	}
	
	@JvmStatic
	@HookReplacer // fucking Crucible bitches overwriting my changes so post-transforming -_-
	fun setBlockReachDistance(iiwm: ItemInWorldManager, distance: Double) {
		startFROM()
		iiwm.blockReachDistance = distance
		startTO()
		// probably better to just do nothing here and integrate any other mods manually
		logCancel()
		// PlayerReachDistanceHandler.setReachDistance(iiwm.thisPlayerMP, distance);
		stop()
	}
	
	private val setBlockReachDistanceCallers: MutableSet<String?> = HashSet<String?>()
	fun logCancel() {
		val caller = Thread.currentThread().stackTrace[3].toString()
		if (caller in setBlockReachDistanceCallers) return  // no spam as some do this every tick
		
		setBlockReachDistanceCallers += caller
		ASJUtilities.warn("Cancelling attempt to set block reach distance from $caller")
	}
	
	
	
	// ######## COMPAT  ########
	
	// #### Botania
	private val botaniaMod = HashMultimap.create<String, AttributeModifier>().apply {
		put(PlayerReachDistanceHandler.reachDistance.attributeUnlocalizedName, AttributeModifier(UUID.fromString("a4e0e453-8efd-4177-8636-f8913eaaf213"), "Botania ItemReachRing", 3.5, 0))
	}
	
	@JvmStatic
	fun onEquippedOrLoadedIntoWorld(item: Any, stack: ItemStack?, player: EntityLivingBase?) {
		if (player is EntityPlayerMP)
			player.getAttributeMap().applyAttributeModifiers(botaniaMod)
	}
	
	@JvmStatic
	fun onUnequipped(item: Any, stack: ItemStack?, player: EntityLivingBase?) {
		if (player is EntityPlayerMP)
			player.getAttributeMap().removeAttributeModifiers(botaniaMod)
	}
	
	
	// #### Starminer
	val EntityLivingGravitized_hasInit: Field? by lazy {
		try_ {
			return@lazy ASJReflectionHelper.getField(
				Class.forName("jp.mc.ancientred.starminer.core.entity.EntityLivingGravitized"),
				"hasInitServerPlayer", "hasInitPrivate"
			)?.apply { setAccessible(true) }
		}
		return@lazy null
	}
	
	@JvmStatic
	fun init(e: Any) {
		EntityLivingGravitized_hasInit?.let { ASJReflectionHelper.setValue(it, e, true, false) }
		
		if (e !is EntityPlayerMP) return
		
		e.getAttributeMap().applyAttributeModifiers(HashMultimap.create<String, AttributeModifier>().apply {
			put(PlayerReachDistanceHandler.reachDistance.attributeUnlocalizedName, AttributeModifier(UUID.fromString("30eb815c-094d-45fb-a6e3-6864482f9bf5"), "StarMiner EntityLivingGravitized", 2.0, 0))
		})
	}
	
	
	// #### The Aether
	@JvmStatic
	@Hook(createMethod = true, returnCondition = ALWAYS, isMandatory = false, targetClass = "com.gildedgames.the_aether.items.tools.ItemValkyrieTool")
	fun getItemAttributeModifiers(tool: Any): Multimap<String, AttributeModifier> {
		val map = ASJSuperWrapperHandler.getItemAttributeModifiers(tool as ItemTool)
		map.put(PlayerReachDistanceHandler.reachDistance.attributeUnlocalizedName, AttributeModifier(UUID.fromString("35224455-039d-4786-b554-d4c9e83905b6"), "Aether ItemValkyrieTool", 3.0, 0))
		return map
	}
	
	@JvmStatic
	fun getMouseOver(aer: AetherEntityRenderer, ticks: Float) {
		aer.previous.getMouseOver(ticks)
		aer.pointedEntity = aer.previous.pointedEntity
	}
	
	
	// #### ChromatiCraft
	private fun chromatiMod(amount: Int) = HashMultimap.create<String, AttributeModifier>().apply {
		put(PlayerReachDistanceHandler.reachDistance.attributeUnlocalizedName, AttributeModifier(UUID.fromString("010bde37-2cba-4cbb-8d5d-22247fe9dbb8"), "ChromatiCraft Ability", amount.D, 0))
	}
	
	@JvmStatic
	fun setReachDistance(static: Any?, player: EntityPlayer, dist: Int) {
		if (player !is EntityPlayerMP) return
		
		val mods = chromatiMod(dist - 5)
		player.getAttributeMap().removeAttributeModifiers(mods)
		
		if (dist > 0) player.getAttributeMap().applyAttributeModifiers(mods)
	}
	
	
	// #### M&B:B2
	@JvmStatic
	fun getAttributeModifiers(target: ItemDagger, stack: ItemStack?, result: Multimap<String, AttributeModifier>) = processMnBB2(result, target.reach)
	
	@JvmStatic
	fun getAttributeModifiers(target: ItemSpear, stack: ItemStack?, result: Multimap<String, AttributeModifier>) = processMnBB2(result, target.reach)
	
	private fun processMnBB2(result: Multimap<String, AttributeModifier>, reach: Float): Multimap<String, AttributeModifier> {
		result.removeAll("weapon.extendedReach")
		result.put(PlayerReachDistanceHandler.reachDistance.attributeUnlocalizedName, AttributeModifier(UUID.fromString("e0ac6d4b-e309-4c9c-9f8c-56991d8e2611"), "battlegear2 IExtendedReachWeapon", reach.D, 0))
		return result
	}
	
	
	// #### LOTR
	
	@JvmStatic // nope
	@Hook(returnCondition = ALWAYS, targetClass = "lotr.client.LOTREntityRenderer")
	fun getMouseOver(target: Any, partialTick: Float) {
		ASJSuperWrapperHandler.getMouseOver(target as EntityRenderer, partialTick)
	}
	
	@JvmStatic
	@HookReplacer(removePop = true, targetClass = "lotr.common.LOTRNetHandlerPlayServer")
	fun processUseEntity(nhps: Any, packet: C02PacketUseEntity?) {
		startFROM()
		DLOAD("5")
		startTO()
		PlayerReachDistanceHandler.getReachDistance((nhps as NetHandlerPlayServer).playerEntity)
		stop()
	}
	
	@JvmStatic
	@Hook(createMethod = true, returnCondition = ALWAYS, targetClass = "lotr.common.item.LOTRItemDagger", targetMethod = "getAttributeModifiers")
	fun getAttributeModifiersLOTRItemDagger(target: Any, stack: ItemStack?) = processLOTR(target, stack, 0.75)
	
	@JvmStatic
	@Hook(createMethod = true, returnCondition = ALWAYS, targetClass = "lotr.common.item.LOTRItemSpear", targetMethod = "getAttributeModifiers")
	fun getAttributeModifiersLOTRItemSpear(target: Any, stack: ItemStack?) = processLOTR(target, stack, 1.5)
	
	@JvmStatic
	@Hook(createMethod = true, returnCondition = ALWAYS, targetClass = "lotr.common.item.LOTRItemPolearm", targetMethod = "getAttributeModifiers")
	fun getAttributeModifiersLOTRItemPolearm(target: Any, stack: ItemStack?) = processLOTR(target, stack, 1.5)
	
	@JvmStatic
	@Hook(createMethod = true, returnCondition = ALWAYS, targetClass = "lotr.common.item.LOTRItemPolearmLong", targetMethod = "getAttributeModifiers")
	fun getAttributeModifiersLOTRItemPolearmLong(target: Any, stack: ItemStack?) = processLOTR(target, stack, 2.0)
	
	@JvmStatic
	@Hook(createMethod = true, returnCondition = ALWAYS, targetClass = "lotr.common.item.LOTRItemLance", targetMethod = "getAttributeModifiers")
	fun getAttributeModifiersLOTRItemLance(target: Any, stack: ItemStack?) = processLOTR(target, stack, 2.0)
	
	@JvmStatic
	@Hook(createMethod = true, returnCondition = ALWAYS, targetClass = "lotr.common.item.LOTRItemBalrogWhip", targetMethod = "getAttributeModifiers")
	fun getAttributeModifiersLOTRItemBalrogWhip(target: Any, stack: ItemStack?) = processLOTR(target, stack, 1.5)
	
	private fun processLOTR(target: Any, stack: ItemStack?, reach: Double): Multimap<String, AttributeModifier> {
		val result = ASJSuperWrapperHandler.getItemAttributeModifiers(target)
		result.put(PlayerReachDistanceHandler.reachDistance.attributeUnlocalizedName, AttributeModifier(UUID.fromString("6cbd6366-015a-455f-a662-c19369f79c36"), "LOTR LOTRItemSword", (reach.D * LOTREnchantmentHelper.calcMeleeReachFactor(stack) - 1), 2))
		return result
	}
	
	
	// #### MineFantasy
	@JvmStatic
	@Hook(createMethod = true, returnCondition = ALWAYS, targetClass = "minefantasy.mf2.item.weapon.ItemHeavyWeaponMF", targetMethod = "getAttributeModifiers")
	fun getAttributeModifiersItemHeavyWeaponMF(target: Any, stack: ItemStack) = processMF(target, stack, 2.0)

	@JvmStatic
	@Hook(createMethod = true, returnCondition = ALWAYS, targetClass = "minefantasy.mf2.item.weapon.ItemSpearMF", targetMethod = "getAttributeModifiers")
	fun getAttributeModifiersItemSpearMF(target: Any, stack: ItemStack) = processMF(target, stack, 3.0)
	
	private fun processMF(target: Any, stack: ItemStack, reach: Double): Multimap<String, AttributeModifier> {
		val result = ASJSuperWrapperHandler.getAttributeModifiers(target, stack)
		result.put(PlayerReachDistanceHandler.reachDistance.attributeUnlocalizedName, AttributeModifier(UUID.fromString("0a76a443-56cc-47cb-9248-1aa82b2bc62d"), "MineFantasy ItemWeaponMF", reach.D, 0))
		return result
	}
}