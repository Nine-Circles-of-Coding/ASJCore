//- By Vamig Aliev.
//- https://vk.com/win_vista.

package ru.vamig.worldengine;

import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;

public abstract class WE_WorldProvider extends WorldProvider {
	
	public float rainfall = 0.1F;
	public WE_ChunkProvider cp = null;
	
	@Override
	public void registerWorldChunkManager() {
		worldChunkMgr = new WE_WorldChunkManager(getDefaultBiome(), getChunkProvider(), rainfall);
	}
	
	@Override
	public IChunkProvider createChunkGenerator() {
		return getChunkProvider();
	}
	
	@Override
	public BiomeGenBase getBiomeGenForCoords(int x, int z) {
		return WE_Biome.getBiomeAt(getChunkProvider(), x, z);
	}
	
	public WE_ChunkProvider getChunkProvider() {
		if (cp == null) cp = new WE_ChunkProvider(this);
		return cp;
	}
	
	public abstract void genSettings(WE_ChunkProvider cp);
	
	public abstract WE_Biome getDefaultBiome();
}