@file:Suppress("unused")

package alexsocol.mixins.compat

import alexsocol.patcher.asm.hook.ArmorFixes
import com.llamalad7.mixinextras.injector.ModifyReturnValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.DamageSource
import net.minecraftforge.common.ISpecialArmor.ArmorProperties
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Pseudo
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import tconstruct.gadgets.item.ItemSlimeBoots
import thaumic.tinkerer.common.item.kami.armor.ItemIchorclothArmor

/*
 * Makes third-party ISpecialArmor implementations stop soaking up unblockable damage.
 *
 * All of these were `@Hook(returnCondition = ON_NOT_NULL, targetMethod = "getProperties", ...)`:
 * if the handler returns a value, it becomes the return value, otherwise the original stands.
 *   - plain HEAD hooks  -> @Inject(HEAD, cancellable) + setReturnValue when non-null
 *   - `injectOnExit`    -> @ModifyReturnValue, i.e. `handler(...) ?: original`
 *
 * `getProperties` comes from Forge's ISpecialArmor, so every injector is remap = false.
 *
 * Which mixins apply is decided by [alexsocol.patcher.asm.ASJLateMixins] from target-class presence,
 * and they live in mixins.asjlib.late.json so the check happens after mod discovery.
 *
 * Two of these target mods published on the GTNH maven and are declared as compileOnly in
 * dependencies.gradle, so the annotation processor validates the class and method for real. The
 * rest are @Pseudo, which means Mixin can only match a method *declared directly* on the target -
 * the same restriction HookLib had, so this is parity rather than a regression.
 */

// ---- validated against a real compile dependency -------------------------------------------

@Mixin(value = [ItemIchorclothArmor::class], remap = false)
abstract class MixinItemIchorclothArmor {
	@Inject(method = ["getProperties"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjUnblockable(player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int, cir: CallbackInfoReturnable<ArmorProperties>?) {
		ArmorFixes.getPropertiesItemIchorclothArmor(this, player, armor, source, damage, slot)?.let { cir?.returnValue = it }
	}
}

@Mixin(value = [ItemSlimeBoots::class], remap = false)
abstract class MixinItemSlimeBoots {
	@Inject(method = ["getProperties"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjUnblockable(player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int, cir: CallbackInfoReturnable<ArmorProperties>?) {
		ArmorFixes.getPropertiesItemSlimeBoots(this, player, armor, source, damage, slot)?.let { cir?.returnValue = it }
	}
}

// ---- @Pseudo: no coordinate available for these mods ----------------------------------------

@Pseudo
@Mixin(targets = ["mods.battlegear2.items.ItemKnightArmour"], remap = false)
abstract class MixinItemKnightArmour {
	@Inject(method = ["getProperties"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjUnblockable(player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int, cir: CallbackInfoReturnable<ArmorProperties>?) {
		ArmorFixes.getPropertiesItemKnightArmour(this, player, armor, source, damage, slot)?.let { cir?.returnValue = it }
	}
}

/** The only one that inspects the original value, so it keeps its `@ReturnValue` shape. */
@Pseudo
@Mixin(targets = ["ic2.core.item.armor.ItemArmorHazmat"], remap = false)
abstract class MixinItemArmorHazmat {
	@ModifyReturnValue(method = ["getProperties"], at = [At("RETURN")], remap = false)
	fun asjUnblockable(original: ArmorProperties?, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int): ArmorProperties? =
		original?.let { ArmorFixes.getPropertiesItemArmorHazmat(this, player, armor, source, damage, slot, it) } ?: original
}

@Pseudo
@Mixin(targets = ["com.emoniph.witchery.item.ItemHunterClothes"], remap = false)
abstract class MixinItemHunterClothes {
	@ModifyReturnValue(method = ["getProperties"], at = [At("RETURN")], remap = false)
	fun asjUnblockable(original: ArmorProperties?, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int): ArmorProperties? =
		ArmorFixes.getPropertiesItemHunterClothes(this, player, armor, source, damage, slot) ?: original
}

@Pseudo
@Mixin(targets = ["thaumrev.item.armor.ItemWardenArmor"], remap = false)
abstract class MixinItemWardenArmor {
	@ModifyReturnValue(method = ["getProperties"], at = [At("RETURN")], remap = false)
	fun asjUnblockable(original: ArmorProperties?, player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int): ArmorProperties? =
		ArmorFixes.getPropertiesItemWardenArmor(this, player, armor, source, damage, slot) ?: original
}

@Pseudo
@Mixin(targets = ["com.emoniph.witchery.item.ItemVampireClothes"], remap = false)
abstract class MixinItemVampireClothes {
	@Inject(method = ["getProperties"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjUnblockable(player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int, cir: CallbackInfoReturnable<ArmorProperties>?) {
		ArmorFixes.getPropertiesItemVampireClothes(this, player, armor, source, damage, slot)?.let { cir?.returnValue = it }
	}
}

@Pseudo
@Mixin(targets = ["ab.common.item.equipment.armor.ItemNebulaArmor"], remap = false)
abstract class MixinItemNebulaArmor {
	@Inject(method = ["getProperties"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjUnblockable(player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int, cir: CallbackInfoReturnable<ArmorProperties>?) {
		ArmorFixes.getPropertiesItemNebulaArmor(this, player, armor, source, damage, slot)?.let { cir?.returnValue = it }
	}
}

@Pseudo
@Mixin(targets = ["gravisuite.ItemAdvancedJetPack"], remap = false)
abstract class MixinItemAdvancedJetPack {
	@Inject(method = ["getProperties"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjUnblockable(player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int, cir: CallbackInfoReturnable<ArmorProperties>?) {
		ArmorFixes.getPropertiesItemAdvancedJetPack(this, player, armor, source, damage, slot)?.let { cir?.returnValue = it }
	}
}

@Pseudo
@Mixin(targets = ["gravisuite.ItemAdvancedLappack"], remap = false)
abstract class MixinItemAdvancedLappack {
	@Inject(method = ["getProperties"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjUnblockable(player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int, cir: CallbackInfoReturnable<ArmorProperties>?) {
		ArmorFixes.getPropertiesItemAdvancedLappack(this, player, armor, source, damage, slot)?.let { cir?.returnValue = it }
	}
}

@Pseudo
@Mixin(targets = ["gravisuite.ItemAdvancedNanoChestPlate"], remap = false)
abstract class MixinItemAdvancedNanoChestPlate {
	@Inject(method = ["getProperties"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjUnblockable(player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int, cir: CallbackInfoReturnable<ArmorProperties>?) {
		ArmorFixes.getPropertiesItemAdvancedNanoChestPlate(this, player, armor, source, damage, slot)?.let { cir?.returnValue = it }
	}
}

@Pseudo
@Mixin(targets = ["gravisuite.ItemGraviChestPlate"], remap = false)
abstract class MixinItemGraviChestPlate {
	@Inject(method = ["getProperties"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjUnblockable(player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int, cir: CallbackInfoReturnable<ArmorProperties>?) {
		ArmorFixes.getPropertiesItemGraviChestPlate(this, player, armor, source, damage, slot)?.let { cir?.returnValue = it }
	}
}

@Pseudo
@Mixin(targets = ["net.mcft.copy.betterstorage.item.cardboard.ItemCardboardArmor"], remap = false)
abstract class MixinItemCardboardArmor {
	@Inject(method = ["getProperties"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjUnblockable(player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int, cir: CallbackInfoReturnable<ArmorProperties>?) {
		ArmorFixes.getPropertiesItemCardboardArmor(this, player, armor, source, damage, slot)?.let { cir?.returnValue = it }
	}
}

@Pseudo
@Mixin(targets = ["net.mcft.copy.betterstorage.item.ItemBackpack"], remap = false)
abstract class MixinItemBackpack {
	@Inject(method = ["getProperties"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjUnblockable(player: EntityLivingBase, armor: ItemStack, source: DamageSource, damage: Double, slot: Int, cir: CallbackInfoReturnable<ArmorProperties>?) {
		ArmorFixes.getPropertiesItemBackpack(this, player, armor, source, damage, slot)?.let { cir?.returnValue = it }
	}
}
