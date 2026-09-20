package alexsocol.mixins;

import alexsocol.patcher.duck.IFoodStatsHost;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.FoodStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FoodStats.class)
public class MixinFoodStats implements IFoodStatsHost {

	/**
	 * Kept public and under its original name on purpose: this field used to be spliced in by
	 * {@code @HookField}, so downstream mods may still look it up by name.
	 */
	@Unique
	public EntityPlayer ASJCore_host;

	@Override
	public EntityPlayer getAsjHost() {
		return ASJCore_host;
	}

	@Override
	public void setAsjHost(EntityPlayer host) {
		ASJCore_host = host;
	}
}
