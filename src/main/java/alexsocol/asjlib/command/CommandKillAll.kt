package alexsocol.asjlib.command

import alexsocol.asjlib.*
import alexsocol.asjlib.math.Vector3
import cpw.mods.fml.common.Loader
import cpw.mods.fml.common.registry.EntityRegistry
import net.minecraft.command.*
import net.minecraft.command.CommandBase.getListOfStringsFromIterableMatchingLastWord
import net.minecraft.entity.*
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.monster.IMob
import net.minecraft.entity.passive.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.util.StatCollector

object CommandKillAll: ASJCommandBase() {
	
	override fun getRequiredPermissionLevel() = 2
	
	override fun getCommandName() = "killall"
	
	override fun getCommandUsage(sender: ICommandSender?) = StatCollector.translateToLocalFormatted(super.getCommandUsage(sender), names.joinToString("|"))
	
	override fun processCommand(sender: ICommandSender, args: Array<out String>) {
		val name = args.getOrNull(0)?.uppercase() ?: "ALL"
		val matcher = EntitySelector.entries.firstOrNull { it.name == name }?.matcher
		if (matcher == null) throw WrongUsageException(getCommandUsage(sender))
		
		val radius = args.getOrNull(1)?.toInt() ?: -1
		val dimId = args.getOrNull(2)?.toInt() ?: sender.entityWorld.provider.dimensionId
		val modId = args.getOrNull(3)
		
		val (x, _, z) = sender.playerCoordinates
		val world = MinecraftServer.getServer().worldServerForDimension(dimId)
		
		var counter = 0
		
		world.loadedEntityList.toMutableList().forEach { it as Entity
			if (it is EntityPlayer) return@forEach
			
			if (!matcher(it)) return@forEach
			if (radius != -1 && Vector3.pointDistancePlane(x, z, it.posX, it.posZ) > radius) return@forEach
			if (modId != null && (EntityRegistry.instance().lookupModSpawn(it.javaClass, false)?.container?.modId ?: "minecraft") != modId) return@forEach
			
			it.setDead()
			counter++
		}
		
		ASJUtilities.say(sender, "asjcore.commands.killall.counter", counter)
	}
	
	override fun addTabCompletionOptions(sender: ICommandSender?, args: Array<out String>): MutableList<out Any?>? {
		return when (args.size) {
			1    -> getListOfStringsFromIterableMatchingLastWord(args, names)
			4    -> Loader.instance().indexedModList.keys.toMutableList().also { it.add(0, "minecraft") }
			else -> ArrayList()
		}
	}
	
	enum class EntitySelector(val matcher: (Entity) -> Boolean) {
		ALL({ true }), ITEMS({ it is EntityItem }), LIVING({ it is EntityLivingBase }), MOBS({ it is IMob }), AMBIENT({ it is EntityAnimal || it is EntityAmbientCreature || it is EntityWaterMob })
	}
	
	private val names = EntitySelector.entries.map { it.name.lowercase() }
}
