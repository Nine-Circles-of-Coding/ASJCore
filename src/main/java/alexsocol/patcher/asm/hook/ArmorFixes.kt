package alexsocol.patcher.asm.hook

import gloomyfolken.hooklib.asm.*
import net.minecraft.entity.*
import net.minecraft.item.*
import net.minecraft.util.*
import net.minecraftforge.common.ISpecialArmor.*

@Suppress("unused", "UNUSED_PARAMETER")
object ArmorFixes {
	
	// IC2
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL, targetMethod = "getProperties", targetClass = "ic2.core.item.armor.ItemArmorHazmat", injectOnExit = true)
	fun getPropertiesItemArmorHazmat(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int, @Hook.ReturnValue result: ArmorProperties) =
		if (source.isUnblockable && result.Priority == 0) ArmorProperties(0, 0.0, 0) else null
	
	// M&B:B2
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL, targetMethod = "getProperties", targetClass = "mods.battlegear2.items.ItemKnightArmour")
	fun getPropertiesItemKnightArmour(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	// Thaumic Tinkerer
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL, targetMethod = "getProperties", targetClass = "thaumic.tinkerer.common.item.kami.armor.ItemIchorclothArmor")
	fun getPropertiesItemIchorclothArmor(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	// Witchery
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL, targetMethod = "getProperties", targetClass = "com.emoniph.witchery.item.ItemHunterClothes", injectOnExit = true)
	fun getPropertiesItemHunterClothes(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL, targetMethod = "getProperties", targetClass = "com.emoniph.witchery.item.ItemVampireClothes")
	fun getPropertiesItemVampireClothes(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	// Advanced Botany
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL, targetMethod = "getProperties", targetClass = "ab.common.item.equipment.armor.ItemNebulaArmor")
	fun getPropertiesItemNebulaArmor(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	// GT:NH TiC
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL, targetMethod = "getProperties", targetClass = "tconstruct.gadgets.item.ItemSlimeBoots")
	fun getPropertiesItemSlimeBoots(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	// GraviSuit
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL, targetMethod = "getProperties", targetClass = "gravisuite.ItemAdvancedJetPack")
	fun getPropertiesItemAdvancedJetPack(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL, targetMethod = "getProperties", targetClass = "gravisuite.ItemAdvancedLappack")
	fun getPropertiesItemAdvancedLappack(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL, targetMethod = "getProperties", targetClass = "gravisuite.ItemAdvancedNanoChestPlate")
	fun getPropertiesItemAdvancedNanoChestPlate(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL, targetMethod = "getProperties", targetClass = "gravisuite.ItemGraviChestPlate")
	fun getPropertiesItemGraviChestPlate(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	// BetterStorage
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL, targetMethod = "getProperties", targetClass = "net.mcft.copy.betterstorage.item.cardboard.ItemCardboardArmor")
	fun getPropertiesItemCardboardArmor(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL, targetMethod = "getProperties", targetClass = "net.mcft.copy.betterstorage.item.ItemBackpack")
	fun getPropertiesItemBackpack(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable) ArmorProperties(0, 0.0, 0) else null
	
	// Thaumic Revelations
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL, targetMethod = "getProperties", targetClass = "thaumrev.item.armor.ItemWardenArmor", injectOnExit = true)
	fun getPropertiesItemWardenArmor(item: Any, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int) =
		if (source.isUnblockable && !source.isMagicDamage) ArmorProperties(0, 0.0, 0) else null
}