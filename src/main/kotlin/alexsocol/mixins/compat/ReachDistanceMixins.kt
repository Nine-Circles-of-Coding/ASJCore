@file:Suppress("unused")

package alexsocol.mixins.compat

import alexsocol.patcher.asm.hook.ReachDistanceHooks
import com.gildedgames.the_aether.client.renders.AetherEntityRenderer
import com.google.common.collect.Multimap
import mods.battlegear2.items.ItemDagger
import mods.battlegear2.items.ItemSpear
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.item.ItemStack
import net.minecraftforge.event.entity.player.AttackEntityEvent
import com.llamalad7.mixinextras.injector.ModifyReturnValue
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Pseudo
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

/*
 * Makes other mods stop managing reach distance themselves, so ASJCore's own attribute-based
 * handler is the only thing that sets it.
 *
 * All @Pseudo: none of these mods has a usable coordinate, and two of them (Battlegear2, The Aether)
 * are covered by compile-time stubs in src/api/java that model the *post-AT* shape of the real
 * classes - asjlib_at.cfg publicises `ItemDagger.reach`, `ItemSpear.reach` and
 * `AetherEntityRenderer *`. That is exactly why they cannot be replaced by real dependencies: the
 * real jars have those members private, and the mod only ever sees them after the AT has run.
 *
 * Handlers that were pure no-ops or constants under HookLib (they existed only to carry
 * `returnCondition = ALWAYS`) are inlined here and gone from ReachDistanceHooks.
 *
 * Registration is by target-class presence, see [alexsocol.patcher.asm.ASJLateMixins].
 */

// #### Botania - it would otherwise fight us over the reach attribute

@Pseudo
@Mixin(targets = ["vazkii.botania.common.core.proxy.CommonProxy"], remap = false)
abstract class MixinBotaniaCommonProxy {
	@Inject(method = ["setExtraReach"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjNoExtraReach(entity: EntityLivingBase?, reach: Float, ci: CallbackInfo?) {
		ci?.cancel()
	}
}

@Pseudo
@Mixin(targets = ["vazkii.botania.client.core.proxy.ClientProxy"], remap = false)
abstract class MixinBotaniaClientProxy {
	@Inject(method = ["setExtraReach"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjNoExtraReach(entity: EntityLivingBase?, reach: Float, ci: CallbackInfo?) {
		ci?.cancel()
	}
}

/** Re-implements the ring as an attribute modifier instead of Botania's own reach field. */
@Pseudo
@Mixin(targets = ["vazkii.botania.common.item.equipment.bauble.ItemReachRing"], remap = false)
abstract class MixinItemReachRing {

	@Inject(method = ["onEquippedOrLoadedIntoWorld"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjOnEquipped(stack: ItemStack?, player: EntityLivingBase?, ci: CallbackInfo?) {
		ReachDistanceHooks.onEquippedOrLoadedIntoWorld(this, stack, player)
		ci?.cancel()
	}

	@Inject(method = ["onUnequipped"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjOnUnequipped(stack: ItemStack?, player: EntityLivingBase?, ci: CallbackInfo?) {
		ReachDistanceHooks.onUnequipped(this, stack, player)
		ci?.cancel()
	}
}

// #### StarMiner

@Pseudo
@Mixin(targets = ["jp.mc.ancientred.starminer.core.entity.EntityLivingGravitized"], remap = false)
abstract class MixinEntityLivingGravitized {
	@Inject(method = ["init"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjInit(ci: CallbackInfo?) {
		ReachDistanceHooks.init(this)
		ci?.cancel()
	}
}

// #### The Aether

@Pseudo
@Mixin(targets = ["com.gildedgames.the_aether.client.renders.AetherEntityRenderer"], remap = false)
abstract class MixinAetherEntityRenderer {
	@Inject(method = ["getMouseOver"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjGetMouseOver(ticks: Float, ci: CallbackInfo?) {
		ReachDistanceHooks.getMouseOver(this as Any as AetherEntityRenderer, ticks)
		ci?.cancel()
	}
}

// #### Mine & Blade: Battlegear 2

/** Its "short hands" handling only covers attacking, not digging, so drop it entirely. */
@Pseudo
@Mixin(targets = ["mods.battlegear2.BattlemodeHookContainerClass"], remap = false)
abstract class MixinBattlemodeHookContainerClass {
	@Inject(method = ["attackEntity"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjNoAttackReach(event: AttackEntityEvent?, ci: CallbackInfo?) {
		ci?.cancel()
	}
}

@Pseudo
@Mixin(targets = ["mods.battlegear2.items.ItemDagger"], remap = false)
abstract class MixinB2ItemDagger {

	@Inject(method = ["getReachModifierInBlocks"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjNoReachModifier(stack: ItemStack?, cir: CallbackInfoReturnable<Float>?) {
		cir?.returnValue = 0f
	}

	/**
	 * Was `@Hook(injectOnExit = true)` with no return condition: the handler mutates the multimap in
	 * place, so returning the same instance is equivalent.
	 */
	@ModifyReturnValue(method = ["getAttributeModifiers"], at = [At("RETURN")], remap = false)
	fun asjReachAttribute(original: Multimap<String, AttributeModifier>?, stack: ItemStack?): Multimap<String, AttributeModifier>? =
		original?.let { ReachDistanceHooks.getAttributeModifiers(this as Any as ItemDagger, stack, it) } ?: original
}

@Pseudo
@Mixin(targets = ["mods.battlegear2.items.ItemSpear"], remap = false)
abstract class MixinB2ItemSpear {

	@Inject(method = ["getReachModifierInBlocks"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjNoReachModifier(stack: ItemStack?, cir: CallbackInfoReturnable<Float>?) {
		cir?.returnValue = 0f
	}

	@ModifyReturnValue(method = ["getAttributeModifiers"], at = [At("RETURN")], remap = false)
	fun asjReachAttribute(original: Multimap<String, AttributeModifier>?, stack: ItemStack?): Multimap<String, AttributeModifier>? =
		original?.let { ReachDistanceHooks.getAttributeModifiers(this as Any as ItemSpear, stack, it) } ?: original
}

// #### MineFantasy 2

@Pseudo
@Mixin(targets = ["minefantasy.mf2.mechanics.ExtendedReachMF"], remap = false)
abstract class MixinExtendedReachMF {
	@Inject(method = ["tickEnd"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjNoTickReach(player: net.minecraft.entity.player.EntityPlayer?, ci: CallbackInfo?) {
		ci?.cancel()
	}
}
