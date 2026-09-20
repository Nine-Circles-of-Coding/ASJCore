package ru.vamig.worldengine.standardcustomgen;

import net.minecraft.world.World;
import ru.vamig.worldengine.WE_ChunkProvider;

import java.util.Random;

public abstract class StructureBaseClass {
	
	public abstract boolean generate(World world, Random rand, int x, int y, int z, WE_ChunkProvider chunkProvider);
}
