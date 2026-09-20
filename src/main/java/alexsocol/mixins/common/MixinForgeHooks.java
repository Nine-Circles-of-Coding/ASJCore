package alexsocol.mixins.common;

import alexsocol.patcher.asm.hook.ASJHookHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stops ladders grabbing a flying player. Java because the target is static; {@code remap = false}
 * because ForgeHooks is a Forge class with no SRG names.
 * <p>
 * Was {@code ON_TRUE} with {@code booleanReturnConstant = false}.
 */
@Mixin(value = ForgeHooks.class, remap = false)
public abstract class MixinForgeHooks {

	@Inject(method = "isLivingOnLadder", at = @At("HEAD"), cancellable = true, remap = false)
	private static void asjIsLivingOnLadder(Block block, World world, int x, int y, int z, EntityLivingBase entity, CallbackInfoReturnable<Boolean> cir) {
		if (ASJHookHandler.isLivingOnLadder(null, block, world, x, y, z, entity)) cir.setReturnValue(false);
	}
}
