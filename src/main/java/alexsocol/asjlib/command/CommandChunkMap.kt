package alexsocol.asjlib.command

import alexsocol.patcher.network.*
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.world.ChunkCoordIntPair
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.common.DimensionManager

object CommandChunkMap: ASJCommandBase() {
	
	override fun getCommandName() = "chunkmap"
	
	override fun processCommand(sender: ICommandSender?, args: Array<out String?>?) {
		if (sender !is EntityPlayerMP) return
		
		val world = MinecraftServer.getServer().worldServerForDimension(sender.worldObj.provider.dimensionId)
		
		val forced = HashMap<ChunkCoordIntPair, String>()
		world.persistentChunks.apply { keySet().forEach { forced[it] = get(it).mapTo(HashSet()) { t -> t.modId }.joinToString(", ") } }
		val loaded = world.theChunkProviderServer.func_152380_a().mapTo(HashSet()) {
			ChunkCoordIntPair((it as Chunk).xPosition, it.zPosition)
		}
		loaded.removeAll(forced.keys)
		
		val worldSpawn = if (world.provider.canRespawnHere()) world.spawnPoint else null
		val forceWorldSpawn = world.provider.canRespawnHere() && DimensionManager.shouldLoadSpawn(world.provider.dimensionId)
		val spawnpoint = sender.getBedLocation(world.provider.dimensionId)
		
		NetworkHandler.network.sendTo(MessageChunkMap(loaded, forced, spawnpoint, worldSpawn, forceWorldSpawn), sender)
	}
}