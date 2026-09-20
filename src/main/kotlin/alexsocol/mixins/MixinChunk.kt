package alexsocol.mixins

import alexsocol.patcher.asm.hook.ASJHookHandler
import alexsocol.patcher.duck.ISubBiomeHolder
import net.minecraft.world.biome.BiomeGenBase
import net.minecraft.world.biome.WorldChunkManager
import net.minecraft.world.chunk.Chunk
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(Chunk::class)
abstract class MixinChunk: ISubBiomeHolder {

	/**
	 * Kept public and under its original name on purpose: this field used to be spliced in by
	 * `@HookField`, so downstream mods may still look it up by name. [JvmField] keeps it a plain
	 * field instead of a property with generated accessors, which is what Mixin needs to see.
	 */
	@Unique
	@JvmField
	var WorldEngine_SubBiomeList: Array<String?>? = null

	override fun getAsjSubBiomeList() = WorldEngine_SubBiomeList

	override fun setAsjSubBiomeList(subBiomeList: Array<String?>?) {
		WorldEngine_SubBiomeList = subBiomeList
	}

	/** WorldEngine sub-biome lookup. Was `@Hook(returnCondition = ON_NOT_NULL)`. */
	@Inject(method = ["getBiomeGenForWorldCoords"], at = [At("HEAD")], cancellable = true)
	fun asjGetBiomeGenForWorldCoords(localChunkX: Int, localChunkZ: Int, cm: WorldChunkManager, cir: CallbackInfoReturnable<BiomeGenBase>?) {
		ASJHookHandler.getBiomeGenForWorldCoords(this as Any as Chunk, localChunkX, localChunkZ, cm)
			?.let { cir?.returnValue = it }
	}
}
