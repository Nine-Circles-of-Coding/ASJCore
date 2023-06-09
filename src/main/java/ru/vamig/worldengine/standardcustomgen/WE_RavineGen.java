//- By Vamig Aliev.
//- https://vk.com/win_vista.

package ru.vamig.worldengine.standardcustomgen;

import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import ru.vamig.worldengine.additions.*;

import java.util.*;

public class WE_RavineGen extends WE_CreateChunkGen {
	
	public final List<Block> replaceBlocksList = new ArrayList();
	public final List<Byte> replaceBlocksMetaList = new ArrayList();
	//-//
	public final Block caveBlock = null;
	public final byte caveBlockMeta = 0;
	//-//
	public final Block lavaBlock = Blocks.lava;
	public final byte lavaBlockMeta = 0;
	public final int lavaMaxY = 12;
	
	public final int range = 8;
	public final float[] field_75046_d = new float[1024];
	final Random rand = new Random();
	
	public WE_RavineGen() {
		replaceBlocksList.add(Blocks.stone);
		replaceBlocksMetaList.add((byte) 0);
	}
	
	@Override
	public void gen(WE_GeneratorData data) {
		rand.setSeed(data.chunkProvider.world.getSeed());
		long rx = rand.nextLong(), rz = rand.nextLong();
		for (long cx = data.chunk_X / 16L - (long) range; cx <= data.chunk_X / 16L + (long) range; ++cx)
			for (long cz = data.chunk_Z / 16L - (long) range; cz <= data.chunk_Z / 16L + (long) range; ++cz) {
				long nv1 = cx * rx, nv2 = cz * rz;
				rand.setSeed(nv1 ^ nv2 ^ data.chunkProvider.world.getSeed());
				
				if (rand.nextInt(50) == 0) {
					double
						gx = (double) cx * 16.0D + (double) rand.nextInt(16),
						gy = (double) rand.nextInt(rand.nextInt(40) + 8) + 20.0D,
						gz = (double) cz * 16.0D + (double) rand.nextInt(16);
					byte b0 = 1;
					
					for (int i1 = 0; i1 < b0; ++i1) {
						float
							x = rand.nextFloat() * 2.0F * (float) Math.PI,
							x1 = (rand.nextFloat() - 0.5F) * 0.25F,
							x2 = (rand.nextFloat() * 2.0F + rand.nextFloat()) * 2.0F;
						
						caveGen_func(data, rand.nextLong(), gx, gy, gz, x2, x, x1, 0, 0, 3.0D);
					}
				}
			}
	}
	
	public void caveGen_func(WE_GeneratorData data, long rs, double gx, double gy, double gz, float fn1, float fn2, float fn3, int in1, int in2, double dn) {
		double d4 = (double) data.chunk_X + 8.0D, d5 = (double) data.chunk_Z + 8.0D;
		float f3 = 0.0F, f4 = 0.0F;
		Random r = new Random(rs);
		
		if (in2 <= 0) {
			int p = range * 16 - 16;
			in2 = p - r.nextInt(p / 4);
		}
		
		boolean flag1 = false;
		if (in1 == -1) {
			in1 = in2 / 2;
			flag1 = true;
		}
		
		float f5 = 1.0F;
		for (int k1 = 0; k1 < 256; ++k1) {
			if (k1 == 0 || r.nextInt(3) == 0)
				f5 = 1.0F + r.nextFloat() * r.nextFloat() * 1.0F;
			field_75046_d[k1] = f5 * f5;
		}
		
		for (; in1 < in2; ++in1) {
			double d12 = 1.5D + (double) MathHelper.sin((float) in1 * (float) Math.PI / (float) in2) * (double) fn1, d6 = d12 * dn;
			d12 *= (double) r.nextFloat() * 0.25D + 0.75D;
			d6 *= (double) r.nextFloat() * 0.25D + 0.75D;
			
			float f6 = MathHelper.cos(fn3), f7 = MathHelper.sin(fn3);
			gx += (double) MathHelper.cos(fn2) * (double) f6;
			gy += f7;
			gz += (double) MathHelper.sin(fn2) * (double) f6;
			
			fn3 *= 0.7F;
			fn3 += f4 * 0.05F;
			fn2 += f3 * 0.05F;
			
			f4 *= 0.8F;
			f3 *= 0.5F;
			f4 += (r.nextFloat() - r.nextFloat()) * r.nextFloat() * 2.0F;
			f3 += (r.nextFloat() - r.nextFloat()) * r.nextFloat() * 4.0F;
			
			if (flag1 || r.nextInt(4) != 0) {
				double d7 = gx - d4, d8 = gz - d5, d9 = (double) in2 - (double) in1, d10 = (double) fn1 + 18.0D;
				if (d7 * d7 + d8 * d8 - d9 * d9 > d10 * d10)
					return;
				
				if (gx >= d4 - 16.0D - d12 * 2.0D && gz >= d5 - 16.0D - d12 * 2.0D && gx <= d4 + 16.0D + d12 * 2.0D && gz <= d5 + 16.0D + d12 * 2.0D) {
					int i4 = MathHelper.floor_double(gx - d12) - (int) data.chunk_X - 1, l1 = MathHelper.floor_double(gx + d12) - (int) data.chunk_X + 1,
						j4 = MathHelper.floor_double(gy - d6) - 1, i2 = MathHelper.floor_double(gy + d6) + 1,
						k4 = MathHelper.floor_double(gz - d12) - (int) data.chunk_Z - 1, j2 = MathHelper.floor_double(gz + d12) - (int) data.chunk_Z + 1;
					
					if (i4 < 0)
						i4 = 0;
					if (l1 > 16)
						l1 = 16;
					if (j4 < 1)
						j4 = 1;
					if (i2 > 248)
						i2 = 248;
					if (k4 < 0)
						k4 = 0;
					if (j2 > 16)
						j2 = 16;
					
					boolean flag2 = false;
					int k2, j3;
					//-//
					for (k2 = i4; !flag2 && k2 < l1; ++k2)
						for (int l2 = k4; !flag2 && l2 < j2; ++l2)
							for (int i3 = i2 + 1; !flag2 && i3 >= j4 - 1; --i3) {
								j3 = (k2 * 16 + l2) * 256 + i3;
								if (i3 >= 0 && i3 < 256) {
									if (isOceanBlock(data, j3, k2, i3, l2))
										flag2 = true;
									if (i3 != j4 - 1 && k2 != i4 && k2 != l1 - 1 && l2 != k4 && l2 != j2 - 1)
										i3 = j4;
								}
							}
					//-//
					if (!flag2) {
						for (k2 = i4; k2 < l1; ++k2) {
							double d13 = ((double) k2 + (double) data.chunk_X + 0.5D - gx) / d12;
							for (j3 = k4; j3 < j2; ++j3) {
								double d14 = ((double) j3 + (double) data.chunk_Z + 0.5D - gz) / d12;
								int k3 = (k2 * 16 + j3) * 256 + i2;
								if (d13 * d13 + d14 * d14 < 1.0D)
									for (int l3 = i2 - 1; l3 >= j4; --l3) {
										double d11 = ((double) l3 + 0.5D - gy) / d6;
										if ((d13 * d13 + d14 * d14) * (double) field_75046_d[l3] + d11 * d11 / 6.0D < 1.0D)
											digBlock(data, k3, k2, l3, j3);
										--k3;
									}
							}
						}
						//-//
						if (flag1)
							break;
					}
				}
			}
		}
	}
	
	public boolean isOceanBlock(WE_GeneratorData data, int index, int x, int y, int z) {
		if (data.chunkBlocks[index] != null)
			return data.chunkBlocks[index].getMaterial().isLiquid();
		else
			return false;
	}
	
	public void digBlock(WE_GeneratorData data, int index, int x, int y, int z) {
		if (data.chunkBlocks[index] instanceof BlockFalling || data.chunkBlocks[index + 1] instanceof BlockFalling)
			return;
		
		for (int i = 0; i < replaceBlocksList.size(); i++)
			if (data.chunkBlocks[index] == replaceBlocksList.get(i) && data.chunkBlocksMeta[index] == replaceBlocksMetaList.get(i)) {
				if (y <= lavaMaxY) {
					data.chunkBlocks[index] = lavaBlock;
					data.chunkBlocksMeta[index] = lavaBlockMeta;
				} else {
					data.chunkBlocks[index] = caveBlock;
					data.chunkBlocksMeta[index] = caveBlockMeta;
				}
				break;
			}
	}
	
	public void addReplacingBlock(Block block, byte meta) {
		replaceBlocksList.add(block);
		replaceBlocksMetaList.add(meta);
	}
}