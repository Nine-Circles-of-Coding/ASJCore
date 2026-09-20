package alexsocol.asjlib

import alexsocol.patcher.PatcherConfigHandler
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.minecraft.block.*
import net.minecraft.init.Blocks
import net.minecraft.nbt.*
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import java.io.File

object SchemaUtils {
	
	private val cache = HashMap<String, List<BlockElement>>()
	
	val type = object: TypeToken<List<StringBlockElement>>() {}.type
	
	fun generate(world: World, x: Int, y: Int, z: Int, path: String, doCache: Boolean, hasRotations: Boolean = false, fourRotations: Boolean = true, rotation: Int = 0) {
		val schema = if (hasRotations) loadStructureWithRotations(path, doCache, fourRotations)[rotation] else loadStructure(path, doCache)
		generate(world, x, y, z, schema)
	}
	
	internal fun generate(world: World, x: Int, y: Int, z: Int, arr: List<BlockElement>) {
		world.setBlock(x, y, z, Blocks.air, 0, 4)
		
		for (ele in arr) {
			val block = ele.block ?: continue
			
			for (loc in ele.location) {
				world.setBlock(x + loc.x, y + loc.y, z + loc.z, block, loc.meta, 3)
				
				if (loc.nbt != null) {
					val tile = TileEntity.createAndLoadEntity(JsonToNBT.func_150315_a(loc.nbt) as NBTTagCompound) ?: continue
					tile.xCoord = x + loc.x
					tile.yCoord = y + loc.y
					tile.zCoord = z + loc.z
					world.setTileEntity(x + loc.x, y + loc.y, z + loc.z, tile)
				}
			}
		}
	}
	
	fun checkStructure(world: World, x: Int, y: Int, z: Int, path: String, onFail: ((Int, Int, Int, Int) -> Unit)? = null) = checkStructure(world, x, y, z, loadStructure(path, true), onFail)
	
	internal fun checkStructure(world: World, x: Int, y: Int, z: Int, arr: List<BlockElement>, onFail: ((Int, Int, Int, Int) -> Unit)? = null): Boolean {
		for (ele in arr) {
			for (loc in ele.location) {
				val i = x + loc.x
				val j = y + loc.y
				val k = z + loc.z
				
				fun check(): Boolean {
					if (world.getBlock(i, j, k) != ele.block || world.getBlockMetadata(i, j, k) != loc.meta) return false
					
					loc.nbt ?: return true
					val locNBT = JsonToNBT.func_150315_a(loc.nbt) as NBTTagCompound
					
					val tile = world.getTileEntity(i, j, k) ?: return false
					val landNBT = NBTTagCompound()
					tile.writeToNBT(landNBT)
					
					for (entry in locNBT.tagMap) if (entry.value != landNBT.tagMap[entry.key]) return false
					
					return true
				}
				
				if (!check()) {
					onFail?.invoke(world.provider.dimensionId, i, j, k)
					return false
				}
			}
		}
		
		return true
	}
	
	fun uncache(path: String) {
		cache.remove(path)
	}
	
	internal fun clearCache() {
		cache.clear()
	}
	
	internal fun parseText(schemaText: String) = Gson().fromJson<List<StringBlockElement>>(schemaText, type)!!.map { BlockElement(Block.getBlockFromName(it.block), it.location) }
	
	internal fun loadStructure(path: String, doCache: Boolean): List<BlockElement> {
		if (path in cache) return cache[path]!!
		
		val schema = parseText(javaClass.getResourceAsStream("/assets/$path")!!.use { it.readBytes().decodeToString() })
		if (PatcherConfigHandler.cacheSchemas && doCache) cache[path] = schema
		return schema
	}
	
	private fun loadStructureWithRotations(path: String, doCache: Boolean, allFour: Boolean): MutableList<List<BlockElement>> {
		if (path in cache) {
			val list = mutableListOf(cache[path]!!, cache["$path-"]!!)
			
			if (allFour) {
				list.add(cache.computeIfAbsent("$path--") { rotate(list.last ()) })
				list.add(cache.computeIfAbsent("$path---") { rotate(list.last ()) })
			}
			
			return list
		}
		
		val schema = loadStructure(path, doCache)
		
		val list = mutableListOf(schema)
		list.add(rotate(list.last()))
		
		if (allFour) {
			list.add(rotate(list.last()))
			list.add(rotate(list.last()))
		}
		
		if (PatcherConfigHandler.cacheSchemas && doCache) {
			cache[path] = list[0]
			cache["$path-"] = list[1]
			
			if (allFour) {
				cache["$path--"] = list[2]
				cache["$path---"] = list[3]
			}
		}
		
		return list
	}
	
	private fun rotate(schema: List<BlockElement>) = schema.map { BlockElement(it.block, it.location.map { loc ->
		val meta = if (it.block is BlockStairs) when (loc.meta) {
			 0   -> 2
			 1   -> 3
			 2   -> 1
			 3   -> 0
			 4   -> 6
			 5   -> 7
			 6   -> 5
			 7   -> 4
			 8   -> 10
			 9   -> 11
			10   -> 9
			11   -> 8
			12   -> 14
			13   -> 15
			14   -> 13
			15   -> 12
			else -> loc.meta
		} else loc.meta
		
		LocationElement(-loc.z, loc.y, loc.x, meta, loc.nbt)
	}) }
}

/**
 * Unsafe schema manipulations ignoring caching. Proceed with caution!
 */
object UnsafeSchemaUtils {
	
	fun generate(world: World, x: Int, y: Int, z: Int, file: File) = SchemaUtils.generate(world, x, y, z, parseText(file.readText()))
	
	fun checkStructure(world: World, x: Int, y: Int, z: Int, arr: List<BlockElement>, onFail: ((Int, Int, Int, Int) -> Unit)? = null) = SchemaUtils.checkStructure(world, x, y, z, arr, onFail)
	
	fun parseText(schemaText: String): List<BlockElement> = SchemaUtils.parseText(schemaText)
	
	fun loadStructure(path: String): List<BlockElement> = SchemaUtils.loadStructure(path, false)
}

class StringBlockElement(val block: String, val location: List<LocationElement>)

class BlockElement(val block: Block?, val location: List<LocationElement>)

class LocationElement(val x: Int, val y: Int, val z: Int, val meta: Int, val nbt: String?)