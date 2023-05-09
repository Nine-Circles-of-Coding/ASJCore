package alexsocol.asjlib.mixins;

import alexsocol.patcher.PatcherConfigHandler;
import com.gtnewhorizon.mixinextras.injector.wrapoperation.Operation;
import com.gtnewhorizon.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiIngame.class)
public class MixinGuiIngame {

    @WrapOperation(
        method = "renderVignette(FII)V",
        at = @At("INVOKE")
    )
    public void wrap(GuiIngame instance, float vignetteBrightness, int width, int height, Operation<Void> original) {
        if (PatcherConfigHandler.INSTANCE.getVignette()) {
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        } else original.call(instance, vignetteBrightness, width, height);
    }
}
