package alexsocol.patcher.event

import cpw.mods.fml.common.eventhandler.*
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.*
import net.minecraftforge.event.entity.player.PlayerEvent

/**
 * Fired when all players are woken up by server in [net.minecraft.world.WorldServer.wakeAllPlayers]
 *
 * This event is fired on the [net.minecraftforge.common.MinecraftForge.EVENT_BUS]
 *
 * This event is not [Cancelable]
 */
class ServerWakeUpEvent(val world: WorldServer): Event()

/**
 * Fired when block of fire is placed and portal is tried to be created in [net.minecraft.block.BlockPortal.func_150000_e]
 *
 * This event is fired on the [net.minecraftforge.common.MinecraftForge.EVENT_BUS]
 *
 * This event is [Cancelable]
 * If the event is canceled the portal is not created
 */
@Cancelable class NetherPortalActivationEvent(val worldObj: World, val xCoord: Int, val yCoord: Int, val zCoord: Int): Event()

/**
 * Fired when all players are woken up by server in [net.minecraft.world.WorldServer.wakeAllPlayers]
 *
 * This event is fired on the [net.minecraftforge.common.MinecraftForge.EVENT_BUS]
 *
 * This event is [Cancelable]
 */
@Cancelable class RenderEntityPostEvent(val entity: Entity, val x: Double, val y: Double, val z: Double, val yaw: Float): Event()

/**
 * Fired when food stats are added to players FoodStats in [net.minecraft.util.FoodStats.addStats]
 *
 * After event is processed and not canceled stats from [newFoodLevel] and [newSaturationLevel] are added
 *
 * [entityPlayer] may be null
 *
 * This event is fired on the [net.minecraftforge.common.MinecraftForge.EVENT_BUS]
 *
 * This event is [Cancelable]
 * If the event is canceled food stats are not added.
 */
@Cancelable class PlayerEatingEvent(player: EntityPlayer?, val foodLevel: Int, val saturationLevel: Float, var newFoodLevel: Int = foodLevel, var newSaturationLevel: Float = saturationLevel): PlayerEvent(player)