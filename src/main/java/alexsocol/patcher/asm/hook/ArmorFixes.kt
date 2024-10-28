package alexsocol.patcher.asm.hook

import ab.common.item.equipment.armor.ItemNebulaArmor
import com.emoniph.witchery.item.ItemHunterClothes
import com.emoniph.witchery.item.ItemVampireClothes
import gravisuite.*
import ic2.core.item.armor.ItemArmorHazmat
import mods.battlegear2.items.ItemKnightArmour
import net.mcft.copy.betterstorage.item.ItemBackpack
import net.mcft.copy.betterstorage.item.cardboard.ItemCardboardArmor
import tconstruct.gadgets.item.ItemSlimeBoots
import thaumic.tinkerer.common.item.kami.armor.ItemIchorclothArmor
import thaumrev.item.armor.ItemWardenArmor

import gloomyfolken.hooklib.asm.*
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.DamageSource
import net.minecraftforge.common.ISpecialArmor.ArmorProperties

@Suppress("unused", "UNUSED_PARAMETER")
object ArmorFixes {
	
	// IC2
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL, injectOnExit = true)
	fun getProperties(item: ItemArmorHazmat, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int, @Hook.ReturnValue result: ArmorProperties) =
		if (source.isUnblockable && result.Priority == 0) ArmorProperties(0, 0.0, 0) else null
	
	// M&B:B2
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL)
	fun getProperties(item: ItemKnightArmour, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	// Thaumic Tinkerer
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL)
	fun getProperties(item: ItemIchorclothArmor, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	// Witchery
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL, injectOnExit = true)
	fun getProperties(item: ItemHunterClothes, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL)
	fun getProperties(item: ItemVampireClothes, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	// Advanced Botany
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL)
	fun getProperties(item: ItemNebulaArmor, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	// GT:NH TiC
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL)
	fun getProperties(item: ItemSlimeBoots, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	// GraviSuit
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL)
	fun getProperties(item: ItemAdvancedJetPack, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL)
	fun getProperties(item: ItemAdvancedLappack, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL)
	fun getProperties(item: ItemAdvancedNanoChestPlate, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL)
	fun getProperties(item: ItemGraviChestPlate, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	// BetterStorage
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL)
	fun getProperties(item: ItemCardboardArmor, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL)
	fun getProperties(item: ItemBackpack, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	// Thaumic Revelations
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL, injectOnExit = true)
	fun getProperties(item: ItemWardenArmor, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable && !source.isMagicDamage) ArmorProperties(0, 0.0, 0) else null
}