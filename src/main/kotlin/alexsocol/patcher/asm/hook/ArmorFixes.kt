package alexsocol.patcher.asm.hook

import net.minecraft.entity.*
import net.minecraft.item.*
import net.minecraft.util.*
import net.minecraftforge.common.ISpecialArmor.*

// Called from alexsocol.mixins.compat.ArmorFixMixins; which ones apply is decided by ASJLateMixins.
// The leading `item` parameter is the HookLib calling convention for the receiver.
@Suppress("unused", "UNUSED_PARAMETER")
object ArmorFixes {
	
	// IC2
	@JvmStatic
	fun getPropertiesItemArmorHazmat(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int, result: ArmorProperties) =
		if (source.isUnblockable && result.Priority == 0) ArmorProperties(0, 0.0, 0) else null
	
	// M&B:B2
	@JvmStatic
	fun getPropertiesItemKnightArmour(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	// Thaumic Tinkerer
	@JvmStatic
	fun getPropertiesItemIchorclothArmor(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	// Witchery
	@JvmStatic
	fun getPropertiesItemHunterClothes(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	@JvmStatic
	fun getPropertiesItemVampireClothes(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	// Advanced Botany
	@JvmStatic
	fun getPropertiesItemNebulaArmor(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	// GT:NH TiC
	@JvmStatic
	fun getPropertiesItemSlimeBoots(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	// GraviSuit
	@JvmStatic
	fun getPropertiesItemAdvancedJetPack(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	@JvmStatic
	fun getPropertiesItemAdvancedLappack(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	@JvmStatic
	fun getPropertiesItemAdvancedNanoChestPlate(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	@JvmStatic
	fun getPropertiesItemGraviChestPlate(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	// BetterStorage
	@JvmStatic
	fun getPropertiesItemCardboardArmor(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	@JvmStatic
	fun getPropertiesItemBackpack(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	// Thaumic Revelations
	@JvmStatic
	fun getPropertiesItemWardenArmor(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable && !source.isMagicDamage) ArmorProperties(0, 0.0, 0) else null
}