package alexsocol.patcher.asm.hook

import gloomyfolken.hooklib.asm.Hook
import gloomyfolken.hooklib.asm.Hook.ReturnValue
import gloomyfolken.hooklib.asm.ReturnCondition.*
import net.minecraft.world.biome.BiomeGenBase
import net.minecraftforge.common.BiomeDictionary
import net.minecraftforge.common.BiomeDictionary.Type
import ru.vamig.worldengine.WE_Biome

@Suppress("unused", "UNUSED_PARAMETER")
object BiomeDictionaryForWEHooks {

	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	fun registerBiomeType(static: BiomeDictionary?, biome: BiomeGenBase?, vararg types: Type): Boolean {
		if (biome !is WE_Biome) return false
		
		biome.typeList.addAll(listSubTags(*types))
		return true
	}
	
	@JvmStatic
	@Hook(returnCondition = ALWAYS, injectOnExit = true)
	fun getBiomesForType(static: BiomeDictionary?, type: Type?, @ReturnValue result: Array<BiomeGenBase?>?): Array<BiomeGenBase?> {
		val list = result?.toMutableList() ?: mutableListOf()
		
		WE_Biome.biomeList.values.forEach {
			if (type in it.typeList) list += it
		}
		
		return list.toTypedArray()
	}
	
	@JvmStatic
	@Hook(returnCondition = ALWAYS, injectOnExit = true)
	fun getTypesForBiome(static: BiomeDictionary?, biome: BiomeGenBase?, @ReturnValue result: Array<Type?>?): Array<Type?>? {
		if (biome !is WE_Biome) return result
		
		val list = result?.toMutableList() ?: mutableListOf()
		list.addAll(biome.typeList)
		
		return list.toTypedArray()
	}
	
	@JvmStatic
	@Hook(returnCondition = ALWAYS, injectOnExit = true)
	fun isBiomeOfType(static: BiomeDictionary?, biome: BiomeGenBase?, type: Type, @ReturnValue result: Boolean): Boolean {
		if (biome !is WE_Biome) return result
		
		return listSubTags(type).any { it in biome.typeList }
	}
	
	@JvmStatic
	@Hook(returnCondition = ALWAYS, injectOnExit = true)
	fun isBiomeRegistered(static: BiomeDictionary?, biome: BiomeGenBase?, @ReturnValue result: Boolean) = if (biome is WE_Biome) true else result
	
	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	fun makeBestGuess(static: BiomeDictionary?, biome: BiomeGenBase?) = biome is WE_Biome
	
	private fun listSubTags(vararg types: Type): Array<Type?> {
		val subTags = ArrayList<Type>()
		for (type in types) {
			if (type.subTags?.isNotEmpty() == true) subTags.addAll(type.subTags) else subTags.add(type)
		}
		return subTags.toTypedArray()
	}
}