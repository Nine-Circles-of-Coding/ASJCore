package alexsocol.patcher.duck;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Implemented on {@link net.minecraft.util.FoodStats} by {@code alexsocol.mixins.MixinFoodStats}.
 * <p>
 * The backing field used to be added at runtime by {@code @HookField}, which meant the sources had
 * to be compiled against a hand-patched Minecraft jar. The mixin adds it instead, and this interface
 * is what gives the rest of the mod a typed way to reach it.
 */
public interface IFoodStatsHost {

	EntityPlayer getAsjHost();

	void setAsjHost(EntityPlayer host);
}
