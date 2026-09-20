package alexsocol.mixins.client

import alexsocol.patcher.asm.hook.ASJHookHandler
import net.minecraft.block.BlockWall
import net.minecraft.client.renderer.RenderBlocks
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

/** Wall rendering that understands the extra connection interfaces. Was `@Hook(returnCondition = ALWAYS)`. */
@Mixin(RenderBlocks::class)
abstract class MixinRenderBlocks {

	@Inject(method = ["renderBlockWall"], at = [At("HEAD")], cancellable = true)
	fun asjRenderBlockWall(block: BlockWall, x: Int, y: Int, z: Int, cir: CallbackInfoReturnable<Boolean>?) {
		cir?.returnValue = ASJHookHandler.renderBlockWall(this as Any as RenderBlocks, block, x, y, z)
	}
}
