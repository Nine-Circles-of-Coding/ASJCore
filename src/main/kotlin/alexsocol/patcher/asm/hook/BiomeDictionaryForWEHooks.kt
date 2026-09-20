package alexsocol.patcher.asm.hook

import net.minecraft.world.biome.BiomeGenBase
import net.minecraftforge.common.BiomeDictionary
import net.minecraftforge.common.BiomeDictionary.Type
import ru.vamig.worldengine.WE_Biome

// Called from alexsocol.mixins.common.MixinBiomeDictionary.
// The leading `static` parameter is the HookLib calling convention for a static target; the mixin
// passes null for it, same as HookLib did.
@Suppress("unused", "UNUSED_PARAMETER")
object BiomeDictionaryForWEHooks {

	@JvmStatic
	fun registerBiomeType(static: BiomeDictionary?, biome: BiomeGenBase?, vararg types: Type): Boolean {
		if (biome !is WE_Biome) return false
		
		biome.typeList.addAll(listSubTags(*types))
		return true
	}
	
	@JvmStatic
	fun getBiomesForType(static: BiomeDictionary?, type: Type?, result: Array<BiomeGenBase?>?): Array<BiomeGenBase?> {
		val list = result?.toMutableList() ?: mutableListOf()
		
		WE_Biome.biomeList.values.forEach {
			if (type in it.typeList) list += it
		}
		
		return list.toTypedArray()
	}
	
	@JvmStatic
	fun getTypesForBiome(static: BiomeDictionary?, biome: BiomeGenBase?, result: Array<Type?>?): Array<Type?>? {
		if (biome !is WE_Biome) return result
		
		val list = result?.toMutableList() ?: mutableListOf()
		list.addAll(biome.typeList)
		
		return list.toTypedArray()
	}
	
	@JvmStatic
	fun isBiomeOfType(static: BiomeDictionary?, biome: BiomeGenBase?, type: Type, result: Boolean): Boolean {
		if (biome !is WE_Biome) return result
		
		return listSubTags(type).any { it in biome.typeList }
	}
	
	@JvmStatic
	fun isBiomeRegistered(static: BiomeDictionary?, biome: BiomeGenBase?, result: Boolean) = if (biome is WE_Biome) true else result
	
	@JvmStatic
	fun makeBestGuess(static: BiomeDictionary?, biome: BiomeGenBase?) = biome is WE_Biome
	
	private fun listSubTags(vararg types: Type): Array<Type?> {
		val subTags = ArrayList<Type>()
		for (type in types) {
			if (type.subTags?.isNotEmpty() == true) subTags.addAll(type.subTags) else subTags.add(type)
		}
		return subTags.toTypedArray()
	}
}