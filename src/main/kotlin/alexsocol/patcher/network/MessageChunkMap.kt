package alexsocol.patcher.network

import alexsocol.asjlib.mc
import alexsocol.asjlib.network.ASJPacket
import alexsocol.patcher.client.GuiChunkMap
import cpw.mods.fml.common.network.simpleimpl.*
import io.netty.buffer.ByteBuf
import net.minecraft.util.ChunkCoordinates
import net.minecraft.world.ChunkCoordIntPair

class MessageChunkMap(var loaded: Set<ChunkCoordIntPair>, var forced: Map<ChunkCoordIntPair, String>, var spawnpoint: ChunkCoordinates?, var worldSpawn: ChunkCoordinates?, var forceWorldSpawn: Boolean): ASJPacket() {
	
	override fun toCustomBytes(buf: ByteBuf) {
		write(buf, loaded.size)
		loaded.forEach {
			write(buf, it.chunkXPos)
			write(buf, it.chunkZPos)
		}
		
		write(buf, forced.size)
		forced.forEach { (k, v) ->
			write(buf, k.chunkXPos)
			write(buf, k.chunkZPos)
			write(buf, v)
		}
		
		write(buf, spawnpoint != null)
		if (spawnpoint != null) {
			write(buf, spawnpoint!!.posX)
			write(buf, spawnpoint!!.posY)
			write(buf, spawnpoint!!.posZ)
		}
		
		write(buf, worldSpawn != null)
		if (worldSpawn != null) {
			write(buf, worldSpawn!!.posX)
			write(buf, worldSpawn!!.posY)
			write(buf, worldSpawn!!.posZ)
		}
	}
	
	override fun fromCustomBytes(buf: ByteBuf) {
		loaded = List(readI(buf)) { ChunkCoordIntPair(readI(buf), readI(buf)) }.toSet()
		forced = (0 until readI(buf)).associate { ChunkCoordIntPair(readI(buf), readI(buf)) to readLjavalangString(buf)!! }
		
		if (readZ(buf)) spawnpoint = ChunkCoordinates(readI(buf), readI(buf), readI(buf))
		if (readZ(buf)) worldSpawn = ChunkCoordinates(readI(buf), readI(buf), readI(buf))
	}
	
	companion object: IMessageHandler<MessageChunkMap, IMessage?> {
		override fun onMessage(msg: MessageChunkMap, ctx: MessageContext): IMessage? {
			if (ctx.side.isClient)
				mc.displayGuiScreen(GuiChunkMap(msg.loaded.associateWith { "" }, msg.forced, msg.spawnpoint, msg.worldSpawn, msg.forceWorldSpawn))
			
			return null
		}
	}
}