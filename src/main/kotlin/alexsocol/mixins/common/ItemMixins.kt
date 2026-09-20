@file:Suppress("unused")

package alexsocol.mixins.common

import alexsocol.patcher.asm.hook.ASJHookHandler
import com.llamalad7.mixinextras.injector.ModifyReturnValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemBucket
import net.minecraft.item.ItemEnderPearl
import net.minecraft.item.ItemNameTag
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagByteArray
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.potion.PotionEffect
import net.minecraft.util.EntityDamageSource
import net.minecraft.util.IChatComponent
import net.minecraft.world.World
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(ItemStack::class)
abstract class MixinItemStack {

	@Inject(method = ["writeToNBT"], at = [At("HEAD")], cancellable = true)
	fun asjWriteToNBT(nbt: NBTTagCompound, cir: CallbackInfoReturnable<NBTTagCompound>?) {
		ASJHookHandler.writeToNBT(this as Any as ItemStack, nbt)?.let { cir?.returnValue = it }
	}

	/** Target returns void, so `ON_TRUE` meant "skip the vanilla body". */
	@Inject(method = ["readFromNBT"], at = [At("HEAD")], cancellable = true)
	fun asjReadFromNBT(nbt: NBTTagCompound, ci: CallbackInfo?) {
		if (ASJHookHandler.readFromNBT(this as Any as ItemStack, nbt)) ci?.cancel()
	}
}

@Mixin(ItemNameTag::class)
abstract class MixinItemNameTag {

	@Inject(method = ["itemInteractionForEntity"], at = [At("HEAD")], cancellable = true)
	fun asjItemInteractionForEntity(stack: ItemStack, player: EntityPlayer?, target: EntityLivingBase?, cir: CallbackInfoReturnable<Boolean>?) {
		if (ASJHookHandler.itemInteractionForEntity(this as Any as ItemNameTag, stack, player, target)) cir?.returnValue = true
	}
}

@Mixin(ItemEnderPearl::class)
abstract class MixinItemEnderPearl {

	@Inject(method = ["onItemRightClick"], at = [At("HEAD")], cancellable = true)
	fun asjOnItemRightClick(stack: ItemStack, world: World, player: EntityPlayer, cir: CallbackInfoReturnable<ItemStack>?) {
		cir?.returnValue = ASJHookHandler.onItemRightClick(this as Any as ItemEnderPearl, stack, world, player)
	}
}

@Mixin(ItemBucket::class)
abstract class MixinItemBucket {

	@Inject(method = ["func_150910_a"], at = [At("HEAD")])
	fun asjFillBucket(stack: ItemStack?, player: EntityPlayer, item: Item, cir: CallbackInfoReturnable<ItemStack>?) {
		ASJHookHandler.func_150910_a(this as Any as ItemBucket, stack, player, item)
	}

	/**
	 * Was `@Hook(injectOnExit = true)` with no return condition, so HookLib threw the result away.
	 * The handler returns `result` unchanged on every path - it only plays a sound - so returning
	 * its value here is equivalent.
	 */
	@ModifyReturnValue(method = ["tryPlaceContainedLiquid"], at = [At("RETURN")])
	fun asjTryPlaceContainedLiquid(original: Boolean, world: World, x: Int, y: Int, z: Int): Boolean =
		ASJHookHandler.tryPlaceContainedLiquid(this as Any as ItemBucket, world, x, y, z, original)
}

@Mixin(NBTTagByteArray::class)
abstract class MixinNBTTagByteArray {

	@Inject(method = ["toString"], at = [At("HEAD")], cancellable = true)
	fun asjToString(cir: CallbackInfoReturnable<String>?) {
		cir?.returnValue = ASJHookHandler.toString(this as Any as NBTTagByteArray)
	}
}

@Mixin(EntityDamageSource::class)
abstract class MixinEntityDamageSource {

	@Inject(method = ["func_151519_b"], at = [At("HEAD")], cancellable = true)
	fun asjGetDeathMessage(victim: EntityLivingBase, cir: CallbackInfoReturnable<IChatComponent>?) {
		cir?.returnValue = ASJHookHandler.func_151519_b(this as Any as EntityDamageSource, victim)
	}
}

@Mixin(PotionEffect::class)
abstract class MixinPotionEffect {

	@Inject(method = ["<init>(IIIZ)V"], at = [At("RETURN")])
	fun asjPotionEffectInit(potionID: Int, duration: Int, amplifier: Int, isAmbient: Boolean, ci: CallbackInfo?) {
		ASJHookHandler.PotionEffect(this as Any as PotionEffect, potionID, duration, amplifier, isAmbient)
	}

	@Inject(method = ["getAmplifier"], at = [At("HEAD")], cancellable = true)
	fun asjGetAmplifier(cir: CallbackInfoReturnable<Int>?) {
		cir?.returnValue = ASJHookHandler.getAmplifier(this as Any as PotionEffect)
	}
}
