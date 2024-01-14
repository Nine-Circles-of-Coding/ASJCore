package com.KAIIIAK.superwrapper

import com.google.common.collect.*
import gloomyfolken.hooklib.minecraft.HookLibPlugin
import org.objectweb.asm.tree.MethodNode
import java.io.IOException
import java.io.InputStreamReader

// because AlexSocol hates java :3
object McpToSrg {
	
	val mcpToSrgMethodNames = loadMethodNames()
	
	private fun loadMethodNames(): Multimap<String, String> {
		val inputStream = this::class.java.getResourceAsStream("/McpToSrg.csv") ?: throw IOException("Method names map not found")
		val map = HashMultimap.create<String, String>()
		
		InputStreamReader(inputStream).use { iS ->
			iS.readLines().forEach {
				val (mcp, srg) = it.split(",")
				map.put(mcp, srg)
			}
		}
		
		return map
	}
	
	@JvmStatic
	fun getTargetMethodMatchingNameAndDesc(methods: List<MethodNode>, name: String, desc: String) =
		getActualMethodNames(name).firstOrNull { methods.any { mn -> mn.name == it && mn.desc == desc } } ?: name
	
	private fun getActualMethodNames(targetMethod: String): Collection<String> {
		if (!HookLibPlugin.getObfuscated()) return listOf(targetMethod)
		val map = mcpToSrgMethodNames
		if (!map.containsKey(targetMethod)) map.put(targetMethod, targetMethod)
		return map[targetMethod]
	}
}