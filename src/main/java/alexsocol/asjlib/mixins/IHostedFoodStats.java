package alexsocol.asjlib.mixins;

import net.minecraft.entity.player.EntityPlayer;

public interface IHostedFoodStats {
     EntityPlayer getHost();
     void setHost(EntityPlayer _host);
}
