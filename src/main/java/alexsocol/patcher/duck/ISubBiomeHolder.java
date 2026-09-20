package alexsocol.patcher.duck;

/**
 * Implemented on {@link net.minecraft.world.chunk.Chunk} by {@code alexsocol.mixins.MixinChunk}.
 * <p>
 * Holds the WorldEngine sub-biome names for the chunk, 16x16 entries indexed as
 * {@code (x & 15) * 16 + (z & 15)}. Individual entries may be null. See {@link IFoodStatsHost} for
 * why this is an interface rather than a directly accessible field.
 */
public interface ISubBiomeHolder {

	String[] getAsjSubBiomeList();

	void setAsjSubBiomeList(String[] subBiomeList);
}
