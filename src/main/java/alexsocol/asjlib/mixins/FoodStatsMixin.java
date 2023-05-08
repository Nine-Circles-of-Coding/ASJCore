package alexsocol.asjlib.mixins;

import alexsocol.asjlib.mixinifaces.IHostedFoodStats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.FoodStats;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FoodStats.class)
public abstract class FoodStatsMixin implements IHostedFoodStats {
    public EntityPlayer host;

    @Override
    public EntityPlayer getHost() {
        return host;
    }

    @Override
    public void setHost(EntityPlayer _host) {
        host = _host;
    }
}
