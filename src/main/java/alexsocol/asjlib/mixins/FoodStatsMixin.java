package alexsocol.asjlib.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.FoodStats;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FoodStats.class)
public class FoodStatsMixin extends FoodStats implements IHostedFoodStats {
    public EntityPlayer host;

    public EntityPlayer getHost()
    {
        return host;
    }

    public void setHost(EntityPlayer _host)
    {
        host = _host;
    }
}
