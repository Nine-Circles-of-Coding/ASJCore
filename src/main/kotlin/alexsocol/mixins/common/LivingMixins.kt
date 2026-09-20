@file:Suppress("unused", "UNCHECKED_CAST")

package alexsocol.mixins.common

import alexsocol.patcher.asm.hook.ASJHookHandler
import com.llamalad7.mixinextras.injector.ModifyReturnValue
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.passive.EntityMooshroom
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.entity.projectile.EntityEgg
import net.minecraft.item.ItemStack
import net.minecraft.potion.PotionEffect
import net.minecraft.util.DamageSource
import net.minecraft.util.MovingObjectPosition
import net.minecraft.world.IBlockAccess
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

/*
 * Entity-level fixes. Every potion-effect target and `damageArmor` and `moveEntityWithHeading`
 * return void, so the old `ON_TRUE` conditions mean "skip the vanilla body", not "return true".
 */

@Mixin(EntityLivingBase::class)
abstract class MixinEntityLivingBase {

	@Inject(method = ["damageArmor"], at = [At("HEAD")])
	fun asjDamageArmor(damage: Float, ci: CallbackInfo?) {
		ASJHookHandler.damageArmor(this as Any as EntityLivingBase, damage)
	}

	@Inject(method = ["onNewPotionEffect"], at = [At("HEAD")], cancellable = true)
	fun asjOnNewPotionEffectPre(pe: PotionEffect, ci: CallbackInfo?) {
		if (ASJHookHandler.onNewPotionEffectPre(this as Any as EntityLivingBase, pe)) ci?.cancel()
	}

	@Inject(method = ["onNewPotionEffect"], at = [At("RETURN")])
	fun asjOnNewPotionEffectPost(pe: PotionEffect, ci: CallbackInfo?) {
		ASJHookHandler.onNewPotionEffectPost(this as Any as EntityLivingBase, pe)
	}

	@Inject(method = ["onChangedPotionEffect"], at = [At("HEAD")], cancellable = true)
	fun asjOnChangedPotionEffectPre(pe: PotionEffect, was: Boolean, ci: CallbackInfo?) {
		if (ASJHookHandler.onChangedPotionEffectPre(this as Any as EntityLivingBase, pe, was)) ci?.cancel()
	}

	@Inject(method = ["onChangedPotionEffect"], at = [At("RETURN")])
	fun asjOnChangedPotionEffectPost(pe: PotionEffect, was: Boolean, ci: CallbackInfo?) {
		ASJHookHandler.onChangedPotionEffectPost(this as Any as EntityLivingBase, pe, was)
	}

	@Inject(method = ["onFinishedPotionEffect"], at = [At("HEAD")], cancellable = true)
	fun asjOnFinishedPotionEffectPre(pe: PotionEffect, ci: CallbackInfo?) {
		if (ASJHookHandler.onFinishedPotionEffectPre(this as Any as EntityLivingBase, pe)) ci?.cancel()
	}

	@Inject(method = ["onFinishedPotionEffect"], at = [At("RETURN")])
	fun asjOnFinishedPotionEffectPost(pe: PotionEffect, ci: CallbackInfo?) {
		ASJHookHandler.onFinishedPotionEffectPost(this as Any as EntityLivingBase, pe)
	}

	/** Was `ALWAYS` + `injectOnExit` + `@ReturnValue`; the target is private, which Mixin handles. */
	@ModifyReturnValue(method = ["getArmSwingAnimationEnd"], at = [At("RETURN")])
	fun asjGetArmSwingAnimationEnd(original: Int): Int =
		ASJHookHandler.getArmSwingAnimationEnd(this as Any as EntityLivingBase, original)

	@Inject(method = ["moveEntityWithHeading"], at = [At("HEAD")], cancellable = true)
	fun asjMoveEntityWithHeading(moveStrafe: Float, moveForward: Float, ci: CallbackInfo?) {
		if (ASJHookHandler.moveEntityWithHeading(this as Any as EntityLivingBase, moveStrafe, moveForward)) ci?.cancel()
	}
}

@Mixin(EntityArrow::class)
abstract class MixinEntityArrow {

	@Inject(method = ["onCollideWithPlayer"], at = [At("HEAD")])
	fun asjOnCollideWithPlayer(player: EntityPlayer, ci: CallbackInfo?) {
		ASJHookHandler.onCollideWithPlayer(this as Any as EntityArrow, player)
	}
}

@Mixin(EntityDragon::class)
abstract class MixinEntityDragon {

	/** Routes damage to the head part, so the dragon can actually be hurt by anything. */
	@Inject(method = ["attackEntityFrom"], at = [At("HEAD")], cancellable = true)
	fun asjAttackEntityFrom(source: DamageSource?, amount: Float, cir: CallbackInfoReturnable<Boolean>?) {
		cir?.returnValue = ASJHookHandler.attackEntityFrom(this as Any as EntityDragon, source, amount)
	}
}

@Mixin(EntityEgg::class)
abstract class MixinEntityEgg {

	@Inject(method = ["onImpact"], at = [At("HEAD")])
	fun asjOnImpactPre(mop: MovingObjectPosition?, ci: CallbackInfo?) {
		ASJHookHandler.onImpactPre(this as Any as EntityEgg, mop)
	}

	@Inject(method = ["onImpact"], at = [At("RETURN")])
	fun asjOnImpactPost(mop: MovingObjectPosition?, ci: CallbackInfo?) {
		ASJHookHandler.onImpactPost(this as Any as EntityEgg, mop)
	}
}

@Mixin(Entity::class)
abstract class MixinEntity {

	@Inject(method = ["isInsideOfMaterial"], at = [At("HEAD")], cancellable = true)
	fun asjIsInsideOfMaterial(material: Material, cir: CallbackInfoReturnable<Boolean>?) {
		cir?.returnValue = ASJHookHandler.isInsideOfMaterial(this as Any as Entity, material)
	}
}

@Mixin(EntityMooshroom::class)
abstract class MixinEntityMooshroom {

	/** `onSheared` comes from Forge's IShearable, so no SRG name. Was `ON_NOT_NULL`. */
	@Inject(method = ["onSheared"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjOnSheared(item: ItemStack?, world: IBlockAccess?, x: Int, y: Int, z: Int, fortune: Int, cir: CallbackInfoReturnable<ArrayList<ItemStack>>?) {
		ASJHookHandler.onSheared(this as Any as EntityMooshroom, item, world, x, y, z, fortune)
			?.let { cir?.returnValue = it as ArrayList<ItemStack> }
	}
}
