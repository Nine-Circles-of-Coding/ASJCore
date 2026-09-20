package alexsocol.mixins;

import alexsocol.patcher.duck.ISubBiomeHolder;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Chunk.class)
public class MixinChunk implements ISubBiomeHolder {

	/**
	 * Kept public and under its original name on purpose: this field used to be spliced in by
	 * {@code @HookField}, so downstream mods may still look it up by name.
	 */
	@Unique
	public String[] WorldEngine_SubBiomeList;

	@Override
	public String[] getAsjSubBiomeList() {
		return WorldEngine_SubBiomeList;
	}

	@Override
	public void setAsjSubBiomeList(String[] subBiomeList) {
		WorldEngine_SubBiomeList = subBiomeList;
	}
}
