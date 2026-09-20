@file:Suppress("unused")

package alexsocol.mixins.client

import alexsocol.patcher.asm.hook.ASJHookHandler
import net.minecraft.client.gui.GuiIngame
import net.minecraft.client.renderer.EntityRenderer
import net.minecraft.client.renderer.InventoryEffectRenderer
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.entity.Render
import net.minecraft.client.renderer.entity.RenderPlayer
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

/*
 * Rendering fixes. Every `ON_TRUE` here targets a void method, so it means "skip the vanilla body".
 */

@Mixin(Render::class)
abstract class MixinRender {
	
	@Inject(method = ["doRenderShadowAndFire"], at = [At("HEAD")], cancellable = true)
	fun asjDoRenderShadowAndFire(entity: Entity, x: Double, y: Double, z: Double, yaw: Float, ticks: Float, ci: CallbackInfo?) {
		if (ASJHookHandler.doRenderShadowAndFire(this as Any as Render, entity, x, y, z, yaw, ticks)) ci?.cancel()
	}
	
	@Inject(method = ["renderShadow"], at = [At("HEAD")])
	fun asjRenderShadowPre(entity: Entity, x: Double, y: Double, z: Double, shadowAlpha: Float, ticks: Float, ci: CallbackInfo?) {
		ASJHookHandler.renderShadowPre(this as Any as Render, entity, x, y, z, shadowAlpha, ticks)
	}
	
	@Inject(method = ["renderShadow"], at = [At("RETURN")])
	fun asjRenderShadowPost(entity: Entity, x: Double, y: Double, z: Double, shadowAlpha: Float, ticks: Float, ci: CallbackInfo?) {
		ASJHookHandler.renderShadowPost(this as Any as Render, entity, x, y, z, shadowAlpha, ticks)
	}
}

@Mixin(RenderGlobal::class)
abstract class MixinRenderGlobal {
	
	@Inject(method = ["renderSky"], at = [At("HEAD")], cancellable = true)
	fun asjRenderSky(partialTickTime: Float, ci: CallbackInfo?) {
		if (ASJHookHandler.renderSky(this as Any as RenderGlobal, partialTickTime)) ci?.cancel()
	}
}

@Mixin(RenderPlayer::class)
abstract class MixinRenderPlayer {
	
	@Inject(method = ["renderFirstPersonArm"], at = [At("HEAD")])
	fun asjRenderFirstPersonArm(player: EntityPlayer, ci: CallbackInfo?) {
		ASJHookHandler.renderFirstPersonArm(this as Any as RenderPlayer, player)
	}
}

@Mixin(EntityRenderer::class)
abstract class MixinEntityRendererExtra {
	
	/** Private float target; was `@Hook(returnCondition = ALWAYS)`. */
	@Inject(method = ["getNightVisionBrightness"], at = [At("HEAD")], cancellable = true)
	fun asjGetNightVisionBrightness(player: EntityPlayer, partialTicks: Float, cir: CallbackInfoReturnable<Float>?) {
		cir?.returnValue = ASJHookHandler.getNightVisionBrightness(this as Any as EntityRenderer, player, partialTicks)
	}
	
	@Inject(method = ["activateNextShader"], at = [At("HEAD")])
	fun asjActivateNextShader(ci: CallbackInfo?) {
		ASJHookHandler.activateNextShader(this as Any as EntityRenderer)
	}
}

@Mixin(InventoryEffectRenderer::class)
abstract class MixinInventoryEffectRenderer {
	
	@Inject(method = ["func_147044_g"], at = [At("HEAD")])
	fun asjDrawActivePotionEffectsPre(ci: CallbackInfo?) {
		ASJHookHandler.drawActivePotionEffectsPre(this as Any as InventoryEffectRenderer)
	}
}

@Mixin(GuiIngame::class)
abstract class MixinGuiIngame {
	
	@Inject(method = ["renderVignette"], at = [At("HEAD")], cancellable = true)
	fun asjRenderVignette(brightness: Float, width: Int, height: Int, ci: CallbackInfo?) {
		if (ASJHookHandler.renderVignette(this as Any as GuiIngame, brightness, width, height)) ci?.cancel()
	}
}
