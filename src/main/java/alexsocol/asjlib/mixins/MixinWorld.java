package alexsocol.asjlib.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mixin(World.class)
public abstract class MixinWorld {

    @SuppressWarnings("unchecked, rawtypes")
    @ModifyReturnValue(method = "getCollidingBoundingBoxes(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;", at = @At("RETURN"))
    public ArrayList<AxisAlignedBB> collidingBoundingBoxes(List original) {
        ArrayList<AxisAlignedBB> result = new ArrayList<AxisAlignedBB>(original);
        result.removeIf(Objects::isNull);
        return result;
    }
}
