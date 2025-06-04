package alexsocol.patcher.asm.hook

import alexsocol.asjlib.*
import alexsocol.asjlib.extendables.block.*
import alexsocol.asjlib.render.*
import alexsocol.patcher.PatcherConfigHandler
import alexsocol.patcher.event.*
import alexsocol.patcher.handler.*
import alexsocol.patcher.handler.GameRulesHandler.GR_DO_WEATHER_CYCLE
import alexsocol.patcher.helper.*
import alexsocol.patcher.network.*
import biomesoplenty.common.blocks.BlockBOPLog
import biomesoplenty.common.itemblocks.ItemBlockLog
import cofh.asmhooks.HooksCore
import com.emoniph.witchery.dimension.WorldProviderDreamWorld
import cpw.mods.fml.client.*
import cpw.mods.fml.common.*
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.common.registry.LanguageRegistry
import cpw.mods.fml.relauncher.*
import gloomyfolken.hooklib.asm.Hook
import gloomyfolken.hooklib.asm.Hook.ReturnValue
import gloomyfolken.hooklib.asm.ReturnCondition.*
import net.minecraft.block.*
import net.minecraft.block.material.Material
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.*
import net.minecraft.client.gui.achievement.GuiStats
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.multiplayer.PlayerControllerMP
import net.minecraft.client.renderer.*
import net.minecraft.client.renderer.entity.*
import net.minecraft.client.resources.I18n
import net.minecraft.client.settings.GameSettings
import net.minecraft.client.settings.KeyBinding
import net.minecraft.command.*
import net.minecraft.command.server.CommandSummon
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.*
import net.minecraft.entity.DataWatcher.WatchableObject
import net.minecraft.entity.EntityList.EntityEggInfo
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.boss.*
import net.minecraft.entity.effect.*
import net.minecraft.entity.item.EntityEnderPearl
import net.minecraft.entity.monster.*
import net.minecraft.entity.passive.EntityMooshroom
import net.minecraft.entity.player.*
import net.minecraft.entity.projectile.*
import net.minecraft.init.*
import net.minecraft.inventory.*
import net.minecraft.item.*
import net.minecraft.nbt.*
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.potion.*
import net.minecraft.profiler.PlayerUsageSnooper
import net.minecraft.server.MinecraftServer
import net.minecraft.server.ServerEula
import net.minecraft.server.dedicated.DedicatedServer
import net.minecraft.server.integrated.IntegratedServer
import net.minecraft.server.management.ServerConfigurationManager
import net.minecraft.stats.*
import net.minecraft.stats.StatList.*
import net.minecraft.tileentity.TileEntityFurnace
import net.minecraft.util.*
import net.minecraft.world.*
import net.minecraft.world.biome.*
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.storage.AnvilChunkLoader
import net.minecraftforge.common.*
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.IFluidBlock
import org.lwjgl.opengl.GL11.*
import org.objectweb.asm.Opcodes
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.io.File
import java.util.*
import kotlin.math.*

@Suppress("UNUSED_PARAMETER", "unused", "FunctionName", "UNCHECKED_CAST", "DEPRECATION")
object ASJHookHandler {
	
	@SideOnly(Side.SERVER)
	@JvmStatic
	@Hook(injectOnExit = true, targetMethod = "<init>")
	fun ServerEula(thiz: ServerEula, file: File) {
		thiz.field_154351_c = true
	}
	
	// summon lightning bolt in /summon command
	@JvmStatic
	@Hook(returnCondition = ALWAYS, targetMethod = "<init>", createMethod = true, superClass = "net/minecraft/entity/effect/EntityWeatherEffect.${Opcodes.ALOAD}.1.(Lnet/minecraft/world/World;)V")
	fun EntityLightningBolt(thiz: EntityLightningBolt, world: World) {
		thiz.lightningState = 2
		thiz.boltVertex = (Math.random() * Long.MAX_VALUE).toLong()
		thiz.boltLivingTime = ASJUtilities.randInBounds(1, 3, world.rand)
	}
	
	
	// move fire spawn from init to update
	@JvmStatic
	@Hook(returnCondition = ALWAYS, targetMethod = "<init>", createMethod = true, superClass = "net/minecraft/entity/effect/EntityWeatherEffect.${Opcodes.ALOAD}.1.(Lnet/minecraft/world/World;)V")
	fun EntityLightningBolt(thiz: EntityLightningBolt, world: World, x: Double, y: Double, z: Double) {
		thiz.setLocationAndAngles(x, y, z, 0f, 0f)
		EntityLightningBolt(thiz, world)
	}
	
	@JvmStatic
	@Hook
	fun onUpdate(entity: EntityLightningBolt) {
		if (entity.lightningState != 2) return
		
		var i = entity.posX.mfloor()
		var j = entity.posY.mfloor()
		var k = entity.posZ.mfloor()
		
		if (entity.worldObj.isRemote || !entity.worldObj.gameRules.getGameRuleBooleanValue("doFireTick") || !(entity.worldObj.difficultySetting == EnumDifficulty.NORMAL || entity.worldObj.difficultySetting == EnumDifficulty.HARD) || !entity.worldObj.doChunksNearChunkExist(i, j, k, 10)) return
		
		if (entity.worldObj.getBlock(i, j, k).material === Material.air && Blocks.fire.canPlaceBlockAt(entity.worldObj, i, j, k))
			entity.worldObj.setBlock(i, j, k, Blocks.fire)
		
		repeat(4) {
			i = entity.posX.mfloor() + entity.worldObj.rand.nextInt(3) - 1
			j = entity.posY.mfloor() + entity.worldObj.rand.nextInt(3) - 1
			k = entity.posZ.mfloor() + entity.worldObj.rand.nextInt(3) - 1
			
			if (entity.worldObj.getBlock(i, j, k).material === Material.air && Blocks.fire.canPlaceBlockAt(entity.worldObj, i, j, k)) {
				entity.worldObj.setBlock(i, j, k, Blocks.fire)
			}
		}
	}
	
	
	// AIOOBE 257+ crash fix
	@JvmStatic
	@Hook(returnCondition = ALWAYS, targetMethod = "<clinit>")
	fun EntityEnderman(static: EntityEnderman?) {
		val uuid = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0")
		EntityEnderman.attackingSpeedBoostModifierUUID = uuid
		EntityEnderman.attackingSpeedBoostModifier = AttributeModifier(uuid, "Attacking speed boost", 6.199999809265137, 0).setSaved(false)
		EntityEnderman.carriableBlocks = BooleanArray(256)
		
		arrayOf(Blocks.grass, Blocks.dirt, Blocks.sand, Blocks.gravel, Blocks.yellow_flower, Blocks.red_flower, Blocks.brown_mushroom, Blocks.red_mushroom, Blocks.tnt, Blocks.cactus, Blocks.clay, Blocks.pumpkin, Blocks.melon_block, Blocks.mycelium).forEach {
			EntityEnderman.setCarriable(it, true)
		}
	}
	
	// invisible lightnings fix
	@JvmStatic
	@Hook
	fun spawnEntityInWorld(world: World, target: Entity?): Boolean {
		if (target !is EntityWeatherEffect)
			return false
		
		return world.addWeatherEffect(target)
	}
	
	// damageMobArmor config prop impl
	@JvmStatic
	@Hook
	fun damageArmor(entity: EntityLivingBase, damage: Float) {
		if (!PatcherConfigHandler.damageMobArmor) return
		
		val dmg = max(damage / 4f, 1f).I
		for (i in 1..4) {
			val stack = entity.getEquipmentInSlot(i) ?: continue
			stack.damageItem(dmg, entity)
			
			if (stack.stackSize <= 0)
				entity.setCurrentItemOrArmor(i, null)
		}
	}
	
	
	// Adding eggs
	@JvmStatic
	@Hook(targetMethod = "<clinit>", injectOnExit = true)
	fun `EntityList$clinit`(e: EntityList?) {
		addEntityEgg(EntityGiantZombie::class.java, 0x00AFAF, 0x4D6341) // Giant
		addEntityEgg(EntityDragon::class.java, 0x0E0E0E, 0xCC00FA) // Ender Dragon
		addEntityEgg(EntityWither::class.java, 0x141414, 0x5C5C5C) // Wither Boss
		addEntityEgg(EntitySnowman::class.java, 0xEEFFFF, 0xFFA221) // Snowman
		addEntityEgg(EntityIronGolem::class.java, 0xC5C2C1, 0xFFE1CC) // Iron Golem
		
		if (PatcherConfigHandler.lightningID != -1) EntityList.addMapping(EntityLightningBolt::class.java, "LightningBolt", PatcherConfigHandler.lightningID)
	}
	
	// NEI function copy, added check
	private fun addEntityEgg(entity: Class<*>, i: Int, j: Int) {
		val id = EntityList.classToIDMapping[entity] as Int
		if (EntityList.entityEggs[id] != null) return
		EntityList.entityEggs[id] = EntityEggInfo(id, i, j)
	}
	
	
	// gm alias for /gamemode
	@JvmStatic
	@Hook(returnCondition = ALWAYS, createMethod = true)
	fun getCommandAliases(c: CommandGameMode): List<String> {
		return listOf("gm")
	}
	
	@JvmStatic
	@Hook(returnCondition = ALWAYS, createMethod = true)
	fun getCommandAliases(c: CommandDefaultGameMode): List<String>? {
		return null
	}
	
	
	// summon usage
	@JvmStatic
	@Hook(returnCondition = ALWAYS, createMethod = true)
	fun getCommandUsage(c: CommandSummon, sender: ICommandSender?): String {
		return "commands.summon.usage.new"
	}
	
	// entity batches in /summon command
	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	fun processCommand(c: CommandSummon, sender: ICommandSender?, args: Array<String?>): Boolean {
		val count = args.getOrNull(1) ?: return false
		if (!count.startsWith('x')) return false
		
		val newArgs = args.toMutableList().apply { removeAt(1) }.toTypedArray()
		for (i in 0 until count.substring(1).toInt())
			c.processCommand(sender, newArgs)
		
		return true
	}
	
	// all entity names for tab
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun func_147182_d(c: CommandSummon): Array<String> {
		return (EntityList.stringToClassMapping.keys as Set<String>).toTypedArray()
	}
	
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun addTabCompletionOptions(c: CommandSummon, sender: ICommandSender?, args: Array<String?>): MutableList<*>? {
		if (args.size != 1) return null
		
		val last = args[0]!!
		val sb = StringBuilder()
		
		try {
			var fullNames: Iterable<String> = func_147182_d(c).toList()
			val ends = last.matches(Regex(".*\\W$"))
			if (last.isNotEmpty()) fullNames = fullNames.filter { it.startsWith(last, true) }
			
			fullNames = fullNames.mapTo(HashSet()) { mob ->
				sb.setLength(0)
				
				var doBreak = false
				for ((id, it) in mob.withIndex()) {
					if (id < last.length) {
						sb.append(it)
						continue
					} else if ("$it".matches(Regex("\\W"))) {
						if (doBreak) break
						
						if (ends) {
							doBreak = true
							sb.append(it)
						} else break
					} else {
						sb.append(it)
					}
				}
				
				sb.toString()
			}
			
			return CommandBase.getListOfStringsMatchingLastWord(args, *fullNames.toTypedArray())
		} catch (e: Throwable) {
			ASJUtilities.error("Error tabbing /summon", e)
			return null
		}
	}
	
	
	// clear skeleton (and other) arrows in creative
	@JvmStatic
	@Hook
	fun onCollideWithPlayer(arrow: EntityArrow, player: EntityPlayer) {
		if (arrow.canBePickedUp == 0 && player.capabilities.isCreativeMode) arrow.canBePickedUp = 2
	}
	
	// Adventuring Time achievement extension
	@JvmStatic
	@Hook(targetMethod = "<init>")
	fun BiomeGenBase(thiz: BiomeGenBase, id: Int, register: Boolean) {
		if (thiz !is BiomeGenMutated)
			BiomeGenBase.explorationBiomesList += thiz
	}
	
	
	// stack NBT fix
	@JvmStatic
	@Hook(returnCondition = ON_NOT_NULL)
	fun writeToNBT(stack: ItemStack, nbt: NBTTagCompound): NBTTagCompound? {
		if (!PatcherConfigHandler.textIDs) return null
		
		nbt.setString("uId", GameRegistry.findUniqueIdentifierFor(stack.field_151002_e)?.toString() ?: return null)
		nbt.setInteger("id", Item.getIdFromItem(stack.field_151002_e))
		nbt.setInteger("Count", stack.stackSize)
		nbt.setInteger("Damage", stack.itemDamage)
		
		stack.stackTagCompound?.let { nbt.setTag("tag", it) }
		
		return nbt
	}
	
	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	fun readFromNBT(stack: ItemStack, nbt: NBTTagCompound): Boolean {
		if (!PatcherConfigHandler.textIDs) return false
		if (nbt.hasNoTags()) return true
		
		migrate(nbt)
		
		val id = nbt.getString("uId")
		if (id.isBlank() || id.indexOf(':') == -1) return true
		
		val (modid, name) = id.split(':')
		stack.func_150996_a(GameRegistry.findItem(modid, name))
		stack.stackSize = nbt.getInteger("Count")
		stack.itemDamage = max(0, nbt.getInteger("Damage"))
		
		if (nbt.hasKey("tag", 10))
			stack.stackTagCompound = nbt.getCompoundTag("tag")
		
		return true
	}
	
	private fun migrate(nbt: NBTTagCompound) {
		// already migrated
		if (nbt.hasKey("uId", 8)) return
		
		// from prev version with same key for string id
		if (nbt.hasKey("id", 8)) {
			nbt.setString("uId", nbt.getString("id"))
			return
		}
		
		if (!nbt.hasKey("id", 2)) return
		
		val item = Item.getItemById(nbt.getShort("id").toInt()) ?: Blocks.stone.toItem()
		val stack = ItemStack(item, nbt.getByte("Count").toInt(), max(0, nbt.getShort("Damage").toInt()))
		
		if (nbt.hasKey("tag", 10))
			stack.stackTagCompound = nbt.getCompoundTag("tag")
		
		nbt.removeTag("id")
		nbt.removeTag("Count")
		nbt.removeTag("Damage")
		nbt.removeTag("tag")
		
		stack.writeToNBT(nbt)
	}
	
	
	// events
	@JvmStatic
	@Hook(injectOnExit = true)
	fun wakeAllPlayers(world: WorldServer) {
		MinecraftForge.EVENT_BUS.post(ServerWakeUpEvent(world))
	}
	
	@JvmStatic
	@Hook(targetMethod = "onNewPotionEffect", returnCondition = ON_TRUE)
	fun onNewPotionEffectPre(e: EntityLivingBase, pe: PotionEffect) =
		MinecraftForge.EVENT_BUS.post(LivingPotionEvent.Add.Pre(e, pe))
	
	@JvmStatic
	@Hook(targetMethod = "onChangedPotionEffect", returnCondition = ON_TRUE)
	fun onChangedPotionEffectPre(e: EntityLivingBase, pe: PotionEffect, was: Boolean) =
		MinecraftForge.EVENT_BUS.post(LivingPotionEvent.Change.Pre(e, pe, was))
	
	@JvmStatic
	@Hook(targetMethod = "onFinishedPotionEffect", returnCondition = ON_TRUE)
	fun onFinishedPotionEffectPre(e: EntityLivingBase, pe: PotionEffect) =
		MinecraftForge.EVENT_BUS.post(LivingPotionEvent.Remove.Pre(e, pe))
	
	@JvmStatic
	@Hook(targetMethod = "onNewPotionEffect", injectOnExit = true)
	fun onNewPotionEffectPost(e: EntityLivingBase, pe: PotionEffect) {
		MinecraftForge.EVENT_BUS.post(LivingPotionEvent.Add.Post(e, pe))
	}
	
	@JvmStatic
	@Hook(targetMethod = "onChangedPotionEffect", injectOnExit = true)
	fun onChangedPotionEffectPost(e: EntityLivingBase, pe: PotionEffect, was: Boolean) {
		MinecraftForge.EVENT_BUS.post(LivingPotionEvent.Change.Post(e, pe, was))
	}
	
	@JvmStatic
	@Hook(targetMethod = "onFinishedPotionEffect", injectOnExit = true)
	fun onFinishedPotionEffectPost(e: EntityLivingBase, pe: PotionEffect) {
		MinecraftForge.EVENT_BUS.post(LivingPotionEvent.Remove.Post(e, pe))
	}
	
	@SideOnly(Side.CLIENT)
	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	fun doRenderShadowAndFire(render: Render, entity: Entity, x: Double, y: Double, z: Double, yaw: Float, ticks: Float): Boolean =
		MinecraftForge.EVENT_BUS.post(RenderEntityPostEvent(entity, x, y, z, yaw))
	
	@SideOnly(Side.CLIENT)
	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	fun renderSky(rg: RenderGlobal, partialTickTime: Float): Boolean =
		MinecraftForge.EVENT_BUS.post(RenderSkyEvent(mc.entityRenderer, mc.renderViewEntity, ActiveRenderInfo.getBlockAtEntityViewpoint(mc.theWorld, mc.renderViewEntity, partialTickTime), partialTickTime))
	
	@JvmStatic
	@Hook(returnCondition = ON_TRUE, targetMethod = "func_150000_e")
	fun tryToCreatePortal(portal: BlockPortal, world: World, x: Int, y: Int, z: Int) =
		MinecraftForge.EVENT_BUS.post(NetherPortalActivationEvent(world, x, y, z))
	
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun addStats(stats: FoodStats, foodLevel: Int, foodSaturationLevel: Float) {
		val e = PlayerEatingEvent(stats.ASJCore_host, foodLevel, foodSaturationLevel)
		MinecraftForge.EVENT_BUS.post(e)
		
		if (e.isCanceled) return
		
		stats.foodLevel = min(e.newFoodLevel + stats.foodLevel, 20)
		stats.foodSaturationLevel = min(stats.saturationLevel + e.newFoodLevel * e.newSaturationLevel * 2f, stats.foodLevel.F)
	}
	
	@SideOnly(Side.CLIENT)
	@JvmStatic
	@Hook(injectOnExit = true)
	fun mouseXYChange(mh: MouseHelper) = MinecraftForge.EVENT_BUS.post(MouseMovedEvent())
	
	
	// Portal closes GUI fix
	private var portalHook = false
	
	@JvmStatic
	@Hook
	fun onLivingUpdate(player: EntityPlayerSP) {
		portalHook = PatcherConfigHandler.portalHook
	}
	
	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	@SideOnly(Side.CLIENT)
	fun displayGuiScreen(mc: Minecraft, gui: GuiScreen?): Boolean {
		return if (portalHook && mc.thePlayer?.inPortal == true) {
			portalHook = false
			gui == null
		} else false
	}
	
	
	// BlockPane fix
	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	fun canPaneConnectTo(pane: BlockPane, world: IBlockAccess, x: Int, y: Int, z: Int, dir: ForgeDirection) = world.getBlock(x, y, z).let {
		it is IPaneConnectable && it.canPaneConnectTo(world, x, y, z)
	}
	
	// a hook into your hook >:D can you hook it?
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun paneConnectsTo(static: HooksCore?, world: IBlockAccess, x: Int, y: Int, z: Int, dir: ForgeDirection): Boolean {
		if (canPaneConnectTo(Blocks.glass_pane as BlockPane, world, x, y, z, dir)) return true
		
		val block = world.getBlock(x, y, z)
		return block.func_149730_j() || block.material === Material.glass || block is BlockPane || world.isSideSolid(x, y, z, dir.opposite, false)
	}
	
	
	// BlockFence fix
	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	fun canConnectFenceTo(fence: BlockFence, world: IBlockAccess, x: Int, y: Int, z: Int) = world.getBlock(x, y, z).let {
		it is BlockFence || it is IFenceConnectable && it.canConnectFenceTo(world, x, y, z) || it is IFenceGate && it.isGate(world, x, y, z)
	}
	
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun func_149825_a(static: BlockFence?, block: Block) = block is BlockFence
	
	
	// BlockWall fix
	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	fun canConnectWallTo(wall: BlockWall, world: IBlockAccess, x: Int, y: Int, z: Int) = world.getBlock(x, y, z).let {
		it is BlockWall || it is IWallConnectable && it.canConnectWallTo(world, x, y, z) || it is IFenceGate && it.isGate(world, x, y, z)
	}
	
	@JvmStatic
	@Hook(returnCondition = ALWAYS, createMethod = true)
	fun isSideSolid(wall: BlockWall, world: IBlockAccess, x: Int, y: Int, z: Int, side: ForgeDirection) = when (side) {
		ForgeDirection.DOWN -> true
		ForgeDirection.UP   -> wall.blockBoundsMaxY == 1.0
		else                -> false
	}
	
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun renderBlockWall(render: RenderBlocks, block: BlockWall, x: Int, y: Int, z: Int): Boolean {
		val flag = block.canConnectWallTo(render.blockAccess, x - 1, y, z)
		val flag1 = block.canConnectWallTo(render.blockAccess, x + 1, y, z)
		val flag2 = block.canConnectWallTo(render.blockAccess, x, y, z - 1)
		val flag3 = block.canConnectWallTo(render.blockAccess, x, y, z + 1)
		val flag4 = flag2 && flag3 && !flag && !flag1
		val flag5 = !flag2 && !flag3 && flag && flag1
		val doNotRenderPost = render.blockAccess.getBlock(x, y + 1, z) !is BlockWall && render.blockAccess.getBlock(x, y + 1, z) !is BlockSkull && render.blockAccess.getBlock(x, y - 1, z) !is BlockWall
		
		if ((flag4 || flag5) && doNotRenderPost) {
			if (flag4) {
				render.setRenderBounds(0.3125, 0.0, 0.0, 0.6875, 0.8125, 1.0)
				render.renderStandardBlock(block, x, y, z)
			} else {
				render.setRenderBounds(0.0, 0.0, 0.3125, 1.0, 0.8125, 0.6875)
				render.renderStandardBlock(block, x, y, z)
			}
		} else {
			render.setRenderBounds(0.25, 0.0, 0.25, 0.75, 1.0, 0.75)
			render.renderStandardBlock(block, x, y, z)
			if (flag) {
				render.setRenderBounds(0.0, 0.0, 0.3125, 0.25, 0.8125, 0.6875)
				render.renderStandardBlock(block, x, y, z)
			}
			if (flag1) {
				render.setRenderBounds(0.75, 0.0, 0.3125, 1.0, 0.8125, 0.6875)
				render.renderStandardBlock(block, x, y, z)
			}
			if (flag2) {
				render.setRenderBounds(0.3125, 0.0, 0.0, 0.6875, 0.8125, 0.25)
				render.renderStandardBlock(block, x, y, z)
			}
			if (flag3) {
				render.setRenderBounds(0.3125, 0.0, 0.75, 0.6875, 0.8125, 1.0)
				render.renderStandardBlock(block, x, y, z)
			}
		}
		block.setBlockBoundsBasedOnState(render.blockAccess, x, y, z)
		return true
	}
	
	
	// potion fixes
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun updatePotionEffects(e: EntityLivingBase) {
		try {
			val iterator = e.activePotionsMap.keys.iterator()
			
			while (iterator.hasNext()) {
				val integer = iterator.next() as Int
				val potioneffect = e.activePotionsMap[integer] as PotionEffect
				
				if (potioneffect.potionID < 0)
					throw IllegalArgumentException("Potion ID is negative (${potioneffect.potionID}). Did you set some ID to 128+ without potion fixing mod?")
				
				if (potioneffect.potionID !in Potion.potionTypes.indices || Potion.potionTypes[potioneffect.potionID] == null)
					throw IllegalArgumentException("Potential potion ID conflict #${potioneffect.potionID}")
				
				if (!potioneffect.onUpdate(e)) {
					//if (!e.worldObj.isRemote) {
					iterator.remove()
					e.onFinishedPotionEffect(potioneffect)
					//}
				} else if (potioneffect.duration % 600 == 0) {
					e.onChangedPotionEffect(potioneffect, false)
				}
			}
			
			var i: Int
			
			if (e.potionsNeedUpdate) {
				if (!e.worldObj.isRemote) {
					if (e.activePotionsMap.isEmpty()) {
						e.dataWatcher.updateObject(8, 0.toByte())
						e.dataWatcher.updateObject(7, 0)
						e.isInvisible = false
					} else {
						i = PotionHelper.calcPotionLiquidColor(e.activePotionsMap.values)
						e.dataWatcher.updateObject(8, (if (PotionHelper.func_82817_b(e.activePotionsMap.values)) 1 else 0).toByte())
						e.dataWatcher.updateObject(7, i)
						e.isInvisible = e.isPotionActive(Potion.invisibility.id)
					}
				}
				
				e.potionsNeedUpdate = false
			}
			
			i = e.dataWatcher.getWatchableObjectInt(7)
			val flag1 = e.dataWatcher.getWatchableObjectByte(8) > 0
			
			if (i > 0) {
				var flag: Boolean
				
				flag = if (!e.isInvisible) {
					e.worldObj.rand.nextBoolean()
				} else {
					e.worldObj.rand.nextInt(15) == 0
				}
				
				if (flag1) {
					flag = flag and (e.worldObj.rand.nextInt(5) == 0)
				}
				
				if (flag) {
					val d0 = (i shr 16 and 255).D / 255.0
					val d1 = (i shr 8 and 255).D / 255.0
					val d2 = (i and 255).D / 255.0
					e.worldObj.spawnParticle(if (flag1) "mobSpellAmbient" else "mobSpell", e.posX + (e.worldObj.rand.nextDouble() - 0.5) * e.width.D, e.posY + e.worldObj.rand.nextDouble() * e.height.D - e.yOffset.D, e.posZ + (e.worldObj.rand.nextDouble() - 0.5) * e.width.D, d0, d1, d2)
				}
			}
		} catch (ex: ConcurrentModificationException) {
			ASJUtilities.error("Well, that was expected. Ignore.", ex)
		} catch (e: Exception) {
			ASJReflectionHelper.setValue(message_f, e, ASJReflectionHelper.getValue<String>(message_f, e) + "\nIt is possible that you got potion ID conflict. Try installing 'Extended Potions' or make sure you have all IDs BELOW 128!", true)
			val stackTrace = e.stackTrace.filter { "alexsocol" !in it.className }.toTypedArray()
			ASJReflectionHelper.setValue(stackTrace_f, e, stackTrace)
			throw e
		}
	}
	
	private val message_f = ASJReflectionHelper.getField(java.lang.Throwable::class.java, "detailMessage")
	private val stackTrace_f = ASJReflectionHelper.getField(java.lang.Throwable::class.java, "stackTrace")
	
	
	// modded fire breaking in creative fix
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun extinguishFire(world: World, player: EntityPlayer?, x: Int, y: Int, z: Int, side: Int): Boolean {
		var i = x
		var j = y
		var k = z
		
		if (side == 0) --j
		if (side == 1) ++j
		if (side == 2) --k
		if (side == 3) ++k
		if (side == 4) --i
		if (side == 5) ++i
		
		val block = world.getBlock(i, j, k)
		
		val breakable = if (player != null) block.getPlayerRelativeBlockHardness(player, world, i, j, k) > 0f || player.capabilities.isCreativeMode else true
		
		if (block.material === Material.fire && breakable) {
			world.playAuxSFXAtEntity(player, 1004, i, j, k, 0)
			world.setBlockToAir(i, j, k)
			return true
		}
		return false
	}
	
	// nightvision twinkling fix
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun getNightVisionBrightness(render: EntityRenderer, player: EntityPlayer, partialTicks: Float): Float {
		val duration = player.getActivePotionEffect(Potion.nightVision.id)?.duration ?: 0
		return if (duration >= 20) 1f else (duration + (1 - partialTicks)) * 0.05f
	}
	
	// Fix nbt clearing and item deletion in Enchanting Table
	@JvmStatic
	@Hook(targetMethod = "<init>", injectOnExit = true)
	fun ContainerEnchantment(thiz: ContainerEnchantment, inv: InventoryPlayer?, world: World?, x: Int, y: Int, z: Int) {
		val slot = object: Slot(thiz.tableInventory, 0, 25, 47) {
			override fun isItemValid(stack: ItemStack?) =  stack?.stackSize == 1
		}
		
		slot.slotNumber = 0
		thiz.inventorySlots[0] = slot
	}
	
	// clear entity name
	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	fun itemInteractionForEntity(item: ItemNameTag, stack: ItemStack, player: EntityPlayer?, target: EntityLivingBase?): Boolean {
		if (!stack.hasDisplayName() && target is EntityLiving) {
			target.customNameTag = ""
			return true
		}
		
		return false
	}
	
	// can't shear dead animals (dupe fix)
	@JvmStatic
	@Hook(returnCondition = ON_NOT_NULL)
	fun onSheared(entity: EntityMooshroom, item: ItemStack?, world: IBlockAccess?, x: Int, y: Int, z: Int, fortune: Int) = if (entity.isDead) ArrayList<Any?>() else null
	
	
	// invisible blocks to tabs
	@SideOnly(Side.CLIENT)
	@JvmStatic
	@Hook(targetMethod = "getSubBlocks")
	fun getSubBlocksPre(block: BlockTallGrass, item: Item?, tab: CreativeTabs?, list: MutableList<ItemStack?>) {
		list.add(ItemStack(item))
	}
	
	@SideOnly(Side.CLIENT)
	@JvmStatic
	@Hook(targetMethod = "getSubBlocks", injectOnExit = true)
	fun getSubBlocksPost(block: BlockTallGrass, item: Item?, tab: CreativeTabs?, list: MutableList<ItemStack?>) {
		list.add(ItemStack(item, 1, 3))
	}
	
	@SideOnly(Side.CLIENT)
	@JvmStatic
	@Hook(injectOnExit = true)
	fun getSubBlocks(block: BlockDirt, item: Item?, tab: CreativeTabs?, list: MutableList<ItemStack?>) {
		list.add(list.size - 1, ItemStack(item, 1, 1))
	}
	
	
	// int overflow fix
	@SideOnly(Side.CLIENT)
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun getBurnTimeRemainingScaled(furnace: TileEntityFurnace, mod: Int): Int {
		if (furnace.currentItemBurnTime == 0) {
			furnace.currentItemBurnTime = 200
		}
		
		return (furnace.furnaceBurnTime.D / furnace.currentItemBurnTime * mod).I
	}
	
	// fixing some occasional OptiFine crashes
	@SideOnly(Side.CLIENT)
	@Synchronized
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun deleteDisplayLists(gla: GLAllocation?, id: Int) {
		if (GLAllocation.mapDisplayLists.contains(id)) glDeleteLists(id, GLAllocation.mapDisplayLists.remove(id) as Int)
	}
	
	// file:// scheme for chat
	@SideOnly(Side.CLIENT)
	@JvmStatic
	@Hook(targetMethod = "<clinit>", injectOnExit = true)
	fun GuiChat_clinit(gui: GuiChat?) {
		GuiChat.field_152175_f.add("file")
	}
	
	// ???
	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	fun trackBrokenTexture(handler: FMLClientHandler, resourceLocation: ResourceLocation, error: String?): Boolean {
		if (error == null) {
			handler.trackBrokenTexture(resourceLocation, "Unknown Error")
			return true
		}
		
		return false
	}
	
	// custom arm swinging
	@JvmStatic
	@Hook(returnCondition = ALWAYS, injectOnExit = true)
	fun getArmSwingAnimationEnd(e: EntityLivingBase, @ReturnValue result: Int) = if (e is ICustomArmSwingEndEntity) e.getCustomArmSwingAnimationEnd() else result
	
	
	// NBT ByteArray to string fix -- STUPID FUCKING MOTHERFUCKERS
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun toString(tag: NBTTagByteArray): String {
		var s = "["
		val abyte: ByteArray = tag.func_150292_c()
		val i = abyte.size
		
		for (j in 0 until i) {
			val k = abyte[j]
			s = "$s${k}b,"
		}
		
		return "$s]"
	}
	
	@Suppress("LocalVariableName")
	fun func_150489_a(primitive: JsonToNBT.Primitive): NBTBase {
		val field_150493_b = primitive.field_150493_b
		return try {
			if (field_150493_b.matches("[-+]?\\d*\\.?\\d+[dD]".toRegex())) {
				NBTTagDouble(field_150493_b.substring(0, field_150493_b.length - 1).toDouble())
			} else if (field_150493_b.matches("[-+]?\\d*\\.?\\d+[fF]".toRegex())) {
				NBTTagFloat(field_150493_b.substring(0, field_150493_b.length - 1).toFloat())
			} else if (field_150493_b.matches("[-+]?\\d+[bB]".toRegex())) {
				NBTTagByte(field_150493_b.substring(0, field_150493_b.length - 1).toByte())
			} else if (field_150493_b.matches("[-+]?\\d+[lL]".toRegex())) {
				NBTTagLong(field_150493_b.substring(0, field_150493_b.length - 1).toLong())
			} else if (field_150493_b.matches("[-+]?\\d+[sS]".toRegex())) {
				NBTTagShort(field_150493_b.substring(0, field_150493_b.length - 1).toShort())
			} else if (field_150493_b.matches("[-+]?\\d+".toRegex())) {
				NBTTagInt(field_150493_b.substring(0, field_150493_b.length).toInt())
			} else if (field_150493_b.matches("[-+]?\\d*\\.?\\d+".toRegex())) {
				NBTTagDouble(field_150493_b.substring(0, field_150493_b.length).toDouble())
			} else if (!field_150493_b.equals("true", true) && !field_150493_b.equals("false", true)) {
				if (field_150493_b.startsWith("[") && field_150493_b.endsWith("]")) {
					if (field_150493_b.length > 2) {
						val s = field_150493_b.substring(1, field_150493_b.length - 1)
						val astring = s.split(",")
						
						try {
							if (astring.size <= 1) {
								val st = s.trim()
								if (st.endsWith('b') || st.endsWith('B'))
									NBTTagByteArray(byteArrayOf(st.substringEnding(1).toByte()))
								else
									NBTTagIntArray(intArrayOf(st.toInt()))
							} else {
								val st = astring[0].trim() // supposing that all other also endsWith b
								if (st.endsWith('b') || st.endsWith('B'))
									NBTTagByteArray(ByteArray(astring.size) { astring[it].trim().substringEnding(1).toByte() })
								else
									NBTTagIntArray(IntArray(astring.size) { astring[it].trim().toInt() })
							}
						} catch (e: NumberFormatException) {
							NBTTagString(field_150493_b)
						}
					} else {
						NBTTagIntArray(IntArray(0))
					}
				} else {
					var field_150493_b_ = field_150493_b
					if (field_150493_b_.startsWith("\"") && field_150493_b_.endsWith("\"") && field_150493_b_.length > 2) {
						field_150493_b_ = field_150493_b_.substring(1, field_150493_b_.length - 1)
					}
					
					field_150493_b_ = field_150493_b_.replace("\\\\\"", "\"")
					NBTTagString(field_150493_b_)
				}
			} else {
				NBTTagByte(if (field_150493_b.toBoolean()) 1 else 0)
			}
		} catch (e: NumberFormatException) {
			NBTTagString(field_150493_b.replace("\\\\\"", "\""))
		}
	}
	
	
	// NPE fix
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun func_151519_b(src: EntityDamageSource, victim: EntityLivingBase): IChatComponent {
		val damageSourceEntity: Entity? = src.entity
		val itemstack = if (damageSourceEntity is EntityLivingBase) damageSourceEntity.heldItem else null
		val s = "death.attack." + src.damageType
		val s1 = "$s.item"
		val component = damageSourceEntity?.func_145748_c_() ?: ChatComponentText("null")
		return if (itemstack != null && itemstack.hasDisplayName() && StatCollector.canTranslate(s1)) ChatComponentTranslation(s1, victim.func_145748_c_(), component, itemstack.func_151000_E()) else ChatComponentTranslation(s, victim.func_145748_c_(), component)
	}
	
	// disable vignette
	@SideOnly(Side.CLIENT)
	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	fun renderVignette(gui: GuiIngame, vignetteBrightness: Float, width: Int, height: Int): Boolean {
		val disable = !PatcherConfigHandler.vignette
		if (disable) OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, 1, 0)
		return disable
	}
	
	// NPE fix
	@JvmStatic
	@Hook(injectOnExit = true, returnCondition = ALWAYS)
	fun getCollidingBoundingBoxes(world: World, entity: Entity?, aabb: AxisAlignedBB?, @ReturnValue result: List<AxisAlignedBB?>) = ArrayList(result).filterNotNull()
	
	// Entity gravity fix
	// by KAIIIAK
	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	fun moveEntityWithHeading(thiz: EntityLivingBase, moveStrafe: Float, moveForward: Float): Boolean {
		if (!PatcherConfigHandler.entityGravityFix) return false
		
		if (ASJUtilities.isServer || thiz is EntityPlayer) return false
		
		var y = -0.0784000015258789
		val d7 = y
		val list = thiz.worldObj.getCollidingBoundingBoxes(thiz, thiz.boundingBox.addCoord(0.0, y, 0.0))
		
		for (i in list.indices) {
			y = (list[i] as AxisAlignedBB).calculateYOffset(thiz.boundingBox, y)
		}
		
		thiz.isCollidedVertically = d7 != y
		thiz.onGround = d7 != y
		
		thiz.prevLimbSwingAmount = thiz.limbSwingAmount
		val x = thiz.posX - thiz.prevPosX
		val z = thiz.posZ - thiz.prevPosZ
		val f = min(sqrt(x * x + z * z).F * 4f, 1f)
		thiz.limbSwingAmount += (f - thiz.limbSwingAmount) * 0.4f
		thiz.limbSwing += thiz.limbSwingAmount
		
		return true
	}
	
	
	// WE SubBiome storage
	@JvmStatic
	@Hook(injectOnExit = true)
	fun writeChunkToNBT(acl: AnvilChunkLoader, chunk: Chunk, world: World, nbt: NBTTagCompound) {
		val subBiomes = chunk.WorldEngine_SubBiomeList ?: return
		
		val subBiomesList = NBTTagList()
		for (subBiome in subBiomes) subBiomesList.appendTag(NBTTagString(subBiome ?: "<null>"))
		
		nbt.setTag("WorldEngine_SubBiomeList", subBiomesList)
	}
	
	@JvmStatic
	@Hook(injectOnExit = true)
	fun readChunkFromNBT(acl: AnvilChunkLoader, world: World, nbt: NBTTagCompound, @ReturnValue chunk: Chunk): Chunk {
		if (!nbt.hasKey("WorldEngine_SubBiomeList", 9)) return chunk
		
		chunk.WorldEngine_SubBiomeList = arrayOfNulls(256)
		val subBiomesList = nbt.getTag("WorldEngine_SubBiomeList") as NBTTagList
		for (i in 0 until subBiomesList.tagCount()) {
			val subBiome = subBiomesList.getStringTagAt(i)
			chunk.WorldEngine_SubBiomeList[i] = if (subBiome == "<null>") null else subBiome
		}
		
		return chunk
	}
	
	
	// Fix for invalid modders not using [Entity.getFlag]
	// returnAnotherMethod is used so other conflicts won't show ASJCore in the stacktrace
	@JvmStatic
	@Hook(returnCondition = ON_TRUE, returnAnotherMethod = "getWatchableObjectByteBody")
	fun getWatchableObjectByte(dw: DataWatcher, index: Int) = index == 0
	
	@JvmStatic
	fun getWatchableObjectByteBody(dw: DataWatcher, index: Int): Byte {
		val byte = dw.getWatchedObject(index).getObject()
		if (byte is Byte) return byte
		
		return when (byte) {
			is Number -> byte.toByte()
			is String -> byte.toByte()
			else      -> 0
		}
	}
	
	@JvmStatic
	@Hook(returnCondition = ON_TRUE, returnAnotherMethod = "getWatchableObjectIntBody")
	fun getWatchableObjectInt(dw: DataWatcher, index: Int) = index == 0
	
	@JvmStatic
	fun getWatchableObjectIntBody(dw: DataWatcher, index: Int): Int {
		val int = dw.getWatchedObject(index).getObject()
		if (int is Int) return int
		
		return when (int) {
			is Number -> int.toInt()
			is String -> int.toInt()
			else      -> 0
		}
	}
	
	@JvmStatic
	@Hook(injectOnExit = true)
	fun setObject(wo: WatchableObject, watchedObject: Any?) {
		if (watchedObject == null) return
		wo.objectType = FuckingSpigotFix.dataWatcher_dataTypes[watchedObject.javaClass] ?: return
	}
	
	@JvmStatic
	@Hook(injectOnExit = true, targetMethod = "<init>")
	fun `WatchableObject$init`(wo: WatchableObject, objectType: Int, dataValueId: Int, watchedObject: Any?) {
		if (watchedObject == null) return
		wo.objectType = FuckingSpigotFix.dataWatcher_dataTypes[watchedObject.javaClass] ?: return
	}
	
	
	// dark theme for start screen
	@JvmStatic
	@Hook(injectOnExit = true)
	@SideOnly(Side.CLIENT)
	fun start(static: SplashProgress?) {
		if (!PatcherConfigHandler.darkMode) return
		
		SplashProgress.backgroundColor = 0x333333
		SplashProgress.barBackgroundColor = 0x333333
		SplashProgress.barBorderColor = 0x111111
		SplashProgress.barColor = 0x2D0709
		SplashProgress.fontColor = 0xCCCCCC
	}
	
	// mooshrum respawn fix
	@JvmStatic
	@Hook(createMethod = true, returnCondition = ALWAYS)
	fun getCanSpawnHere(entity: EntityMooshroom): Boolean {
		val i = MathHelper.floor_double(entity.posX)
		val j = MathHelper.floor_double(entity.boundingBox.minY)
		val k = MathHelper.floor_double(entity.posZ)
		
		return entity.worldObj.getBlock(i, j - 1, k) === Blocks.mycelium && entity.worldObj.getFullBlockLightValue(i, j, k) > 8 && ASJSuperWrapperHandler.getCanSpawnHere(entity)
	}
	
	// overflow fix
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun calcPotionLiquidColor(static: PotionHelper?, potions: Collection<PotionEffect>?): Int {
		val i = 0x3883DE
		
		if (potions.isNullOrEmpty()) return i
		
		var f = 0f
		var f1 = 0f
		var f2 = 0f
		var f3 = 0f
		
		for (pe in potions) {
			val j = Potion.potionTypes[pe.getPotionID()].getLiquidColor()
			val amp = min(pe.getAmplifier(), 255)
			
			for (k in 0..amp) {
				f += (j shr 16 and 255).F / 255f
				f1 += (j shr 8 and 255).F / 255f
				f2 += (j shr 0 and 255).F / 255f
				++f3
			}
		}
		
		f = f / f3 * 255f
		f1 = f1 / f3 * 255f
		f2 = f2 / f3 * 255f
		return f.I shl 16 or (f1.I shl 8) or f2.I
	}
	
	@JvmStatic
	@Hook(targetMethod = "<init>", injectOnExit = true)
	fun PotionEffect(effect: PotionEffect, potionID: Int, duration: Int, amplifier: Int, isAmbient: Boolean) {
		if (PatcherConfigHandler.clampPotionLevel) effect.amplifier = MathHelper.clamp_int(effect.amplifier, 0, 255)
	}
	
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun getAmplifier(effect: PotionEffect): Int {
		if (PatcherConfigHandler.clampPotionLevel) effect.amplifier = MathHelper.clamp_int(effect.amplifier, 0, 255)
		return effect.amplifier
	}
	
	
	// memes
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	@SideOnly(Side.CLIENT)
	fun enableEverythingIsScrewedUpMode(pcmp: PlayerControllerMP) = PatcherConfigHandler.everythingIsScrewedUpMode
	
	@JvmStatic
	@Hook(returnCondition = ALWAYS, targetMethod = "func_149466_j", injectOnExit = true)
	fun ignoreIllegalStances(c03: C03PacketPlayer, @ReturnValue original: Boolean) = if (PatcherConfigHandler.ignoreIllegalStates) false else original
	
	
	// dragon damaging fix
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun attackEntityFrom(dragon: EntityDragon, source: DamageSource?, amount: Float) = dragon.attackEntityFromPart(dragon.dragonPartHead, source, amount)
	
	
	// egg particles fix
	var eggHook = false
	
	@JvmStatic
	@Hook(targetMethod = "onImpact")
	fun onImpactPre(egg: EntityEgg, mop: MovingObjectPosition?) {
		eggHook = true
	}
	
	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	fun spawnParticle(world: World, name: String?, x: Double, y: Double, z: Double, mx: Double, my: Double, mz: Double): Boolean {
		if (!eggHook || name != "snowballpoof") return false
		var motionX = (Math.random() * 2 - 1) * 0.4
		var motionY = (Math.random() * 2 - 1) * 0.4
		var motionZ = (Math.random() * 2 - 1) * 0.4
		val f = (Math.random() + Math.random() + 1) * 0.15
		val f1 = MathHelper.sqrt_double(motionX * motionX + (motionY * motionY) + (motionZ * motionZ))
		motionX = motionX / f1 * f * 0.4000000059604645
		motionY = motionY / f1 * f * 0.4000000059604645 + 0.10000000149011612
		motionZ = motionZ / f1 * f * 0.4000000059604645
		
		world.spawnParticle("iconcrack_344", x, y, z, motionX, motionY, motionZ)
		
		return true
	}
	
	@JvmStatic
	@Hook(targetMethod = "onImpact", injectOnExit = true)
	fun onImpactPost(egg: EntityEgg, mop: MovingObjectPosition?) {
		eggHook = false
	}
	
	
	// fix for material check for BlockLiquid
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun isInsideOfMaterial(entity: Entity, material: Material): Boolean {
		val d0 = entity.posY + entity.eyeHeight.D + if (entity is EntityPlayerMP) 0.12 else 0.0 // WHY THE FUCK server foot-eye diff is 1.62, and client one - 1.74 ??? 
		val i = MathHelper.floor_double(entity.posX)
		val j = MathHelper.floor_float(MathHelper.floor_double(d0).F) // wtf two floor why?
		val k = MathHelper.floor_double(entity.posZ)
		val block = entity.worldObj.getBlock(i, j, k)
		
		if (block.material !== material) return false
		var filled = 1f //If it's not a liquid assume it's a solid block
		
		if (block is IFluidBlock) {
			filled = block.getFilledPercentage(entity.worldObj, i, j, k)
		} else if (block is BlockLiquid) {
			filled = 1 - BlockLiquid.getLiquidHeightPercent(entity.worldObj.getBlockMetadata(i, j, k) - 1)
		}
		
		if (filled >= 0) return d0 < (j + filled)
		
		filled *= -1
		//filled -= 0.11111111F; //Why this is needed.. not sure...
		return d0 > (j + (1 - filled))
	}
	
	// throw pearls in creative
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun onItemRightClick(item: ItemEnderPearl, stack: ItemStack, world: World, player: EntityPlayer): ItemStack {
		if (!player.capabilities.isCreativeMode) --stack.stackSize
		world.playSoundAtEntity(player, "random.bow", 0.5f, 0.4f / (Item.itemRand.nextFloat() * 0.4f + 0.8f))
		if (!world.isRemote) world.spawnEntityInWorld(EntityEnderPearl(world, player))
		return stack
	}
	
	// adventure game mode
	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	fun actionPerformed(gui: GuiCreateWorld, button: GuiButton): Boolean {
		if (!button.enabled || button.id != 2) return false
		
		if (gui.field_146342_r != "creative") return false
		
		if (!gui.field_146339_u) gui.field_146340_t = false
		
		gui.field_146337_w = false
		gui.field_146342_r = "adventure"
		gui.field_146321_E.enabled = true
		gui.field_146326_C.enabled = true
		gui.func_146319_h()
		
		return true
	}
	
	// fix for potion ui transparency
	@JvmStatic
	@Hook(targetMethod = "func_147044_g")
	fun drawActivePotionEffectsPre(gui: InventoryEffectRenderer) {
		glEnable(GL_BLEND)
		OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
	}
	
	
	// iron golem attack cooldown
	private var EntityIronGolem.attackCd
		get() = dataWatcher.getWatchableObjectInt(2)
		set(value) = dataWatcher.updateObject(2, value)
	
	@JvmStatic
	@Hook(injectOnExit = true)
	fun entityInit(golem: EntityIronGolem) {
		golem.dataWatcher.addObject(2, 0)
	}
	
	@JvmStatic
	@Hook(injectOnExit = true)
	fun onLivingUpdate(golem: EntityIronGolem) {
		if (golem.attackCd > 0) --golem.attackCd
	}
	
	@JvmStatic
	@Hook(targetMethod = "attackEntityAsMob", returnCondition = ON_TRUE, booleanReturnConstant = false)
	fun attackEntityAsMobPre(golem: EntityIronGolem, target: Entity?) = golem.attackCd > 0
	
	@JvmStatic
	@Hook(targetMethod = "attackEntityAsMob", injectOnExit = true)
	fun attackEntityAsMobPost(golem: EntityIronGolem, target: Entity?) {
		golem.attackCd = 20
	}
	
	
	// copy seed to clipboard
	@JvmStatic
	@Hook(injectOnExit = true)
	fun processCommand(command: CommandShowSeed, sender: ICommandSender, args: Array<String?>?) {
		val seed = sender.entityWorld.seed.toString()
		
		if (sender is EntityPlayerMP)
			NetworkHandler.network.sendTo(MessageClipboard(seed), sender)
		else if (Desktop.isDesktopSupported()) // for console execution
			Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(seed), null)
	}
	
	
	// allow allsided rotateable blocks placement
	@JvmStatic
	@Hook(returnCondition = ON_TRUE, returnAnotherMethod = "placeAllsided")
	fun onBlockPlaced(block: BlockRotatedPillar, world: World?, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float, hitZ: Float, meta: Int) = meta and 0b1100 == 0b1100
	
	@JvmStatic
	fun placeAllsided(block: BlockRotatedPillar, world: World?, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float, hitZ: Float, meta: Int) = meta
	
	@JvmStatic
	@Hook(returnCondition = ON_TRUE, returnAnotherMethod = "placeAllsided")
	fun onBlockPlaced(block: BlockBOPLog, world: World?, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float, hitZ: Float, meta: Int) = meta and 0b1100 == 0b1100
	
	@JvmStatic
	fun placeAllsided(block: BlockBOPLog, world: World?, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float, hitZ: Float, meta: Int) = meta
	
	@JvmStatic
	@Hook(returnCondition = ON_TRUE, returnAnotherMethod = "allsidedMeta")
	fun getMetadata(ib: ItemBlockLog, meta: Int) = meta and 0b1100 == 0b1100
	
	@JvmStatic
	fun allsidedMeta(ib: ItemBlockLog, meta: Int) = meta
	
	
	// bucket sounds
	@JvmStatic
	@Hook
	fun func_150910_a(target: ItemBucket, stack: ItemStack?, player: EntityPlayer, item: Item): ItemStack? {
		if (!PatcherConfigHandler.bucketSounds) return stack
		
		val s = if (item === Items.water_bucket)
			"game.neutral.swim" // "game.neutral.swim.splash"
		else if (item === Items.lava_bucket)
			"liquid.lavapop"
		else
			return stack
		
		player.playSound(s, 1f, 1f)
		return stack
	}
	
	@JvmStatic
	@Hook(injectOnExit = true)
	fun tryPlaceContainedLiquid(target: ItemBucket, world: World, x: Int, y: Int, z: Int, @ReturnValue result: Boolean): Boolean {
		if (!PatcherConfigHandler.bucketSounds) return result
		
		if (world.provider.isHellWorld && target.isFull === Blocks.flowing_water) return result
		
		val s = if (target.isFull.material === Material.water)
			"game.neutral.swim" // "game.neutral.swim.splash"
		else if (target.isFull.material === Material.lava)
			"liquid.lavapop"
		else
			return result
		
		world.playSound(x + 0.5, y + 0.5, z + 0.5, s, 1f, 1f, false)
		return result
	}
	
	
	// optional other worlds respawn
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun canRespawnHere(target: WorldProviderHell) = PatcherConfigHandler.respawnInNether
	
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun canRespawnHere(target: WorldProviderEnd) = PatcherConfigHandler.respawnInEnd
	
	
	// adding more game rules to default set 
	@JvmStatic
	@Hook(injectOnExit = true, targetMethod = "<init>")
	fun `GameRules$init`(thiz: GameRules) {
		GameRulesHandler.registerAll(thiz)
	}
	
	// doWeatherCycle game rule implementation
	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	fun updateWeatherBody(world: World): Boolean {
		val rules = world.gameRules
		if (!rules.hasRule(GR_DO_WEATHER_CYCLE))
			rules.addGameRule(GR_DO_WEATHER_CYCLE, true.toString())
		
		return !rules.getGameRuleBooleanValue(GR_DO_WEATHER_CYCLE)
	}
	
	// move player from non-existent dimension
	@JvmStatic
	@Hook(injectOnExit = true)
	fun readPlayerDataFromFile(scm: ServerConfigurationManager, player: EntityPlayerMP?, @ReturnValue result: NBTTagCompound?): NBTTagCompound? {
		player ?: return result
		val dim = player.dimension
		if (DimensionManager.isDimensionRegistered(dim)) return result
		
		player.dimension = 0
		
		val (x, y, z) = DimensionManager.getWorld(0).spawnPoint
		ASJUtilities.sendToDimensionWithoutPortal(player, 0, x + 0.5, y.D, z + 0.5)
		
		if (result == null) return null
		
		result.setInteger("Dimension", 0)
		val pos = NBTTagList()
		pos.appendTag(NBTTagDouble(x + 0.5))
		pos.appendTag(NBTTagDouble(y.D))
		pos.appendTag(NBTTagDouble(z + 0.5))
		result.setTag("Pos", pos)
		
		return result
	}
	
	
	// fix player shadow render
	@JvmStatic
	@Hook(targetMethod = "renderShadow")
	fun renderShadowPre(render: Render, entity: Entity, x: Double, y: Double, z: Double, shadowAlpha: Float, partialTickTime: Float) {
		if (entity !== mc.thePlayer) return
		
		entity.lastTickPosY -= 1.6200000047683716
		entity.posY -= 1.6200000047683716
		Tessellator.instance.addTranslation(0f, -1.62f, 0f)
	}
	
	@JvmStatic
	@Hook(targetMethod = "renderShadow", injectOnExit = true)
	fun renderShadowPost(render: Render, entity: Entity, x: Double, y: Double, z: Double, shadowAlpha: Float, partialTickTime: Float) {
		if (entity !== mc.thePlayer) return
		
		entity.lastTickPosY += 1.6200000047683716
		entity.posY += 1.6200000047683716
		Tessellator.instance.addTranslation(0f, 1.62f, 0f)
	}
	
	
	// changeable reach distance
	@SideOnly(Side.CLIENT)
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun getBlockReachDistance(pcmp: PlayerControllerMP) = (mc.thePlayer?.getEntityAttribute(PlayerReachDistanceHandler.reachDistance)?.attributeValue ?: PlayerReachDistanceHandler.reachDistance.defaultValue).F
	
	// fix for https://bugs.mojang.com/browse/MC-1519
	@SideOnly(Side.CLIENT)
	@JvmStatic
	@Hook(injectOnExit = true)
	fun toggleFullscreen(mc: Minecraft) {
		KeyBinding.unPressAllKeys()
	}
	
	// arm fix
	@SideOnly(Side.CLIENT)
	@JvmStatic
	@Hook
	fun renderFirstPersonArm(rp: RenderPlayer, player: EntityPlayer) {
		rp.modelBipedMain.isRiding = player.isRiding
	}
	
	// fucking stupid shitcoder fix
	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	fun dropBetterBackpacks(static: WorldProviderDreamWorld?, player: EntityPlayer) = !Loader.isModLoaded("betterstorage")
	
	// Mod options gui tweaks
	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	fun showInGameModOptions(fmlch: FMLClientHandler, guiIngameMenu: GuiIngameMenu?): Boolean {
		if (!PatcherConfigHandler.fixInGameModOptions) return false
		fmlch.showGuiScreen(GuiModList(guiIngameMenu))
		return true
	}
	
	@JvmStatic
	@Hook(injectOnExit = true)
	fun initGui(gui: GuiModList) {
		if (!PatcherConfigHandler.fixInGameModOptions) return
		
		gui.buttonList.remove(gui.disableModButton)
	}
	
	// delete streaming
	@JvmStatic
	@Hook(injectOnExit = true)
	fun initGui(gui: GuiOptions) {
		gui.buttonList.removeAll { (it as GuiButton).id == 107 } // Broadcast Settings
		gui.buttonList.find { (it as GuiButton).id == 8675309 }?.let { // SSS
			it as GuiButton
			it.yPosition = gui.height / 6 + 72 - 6
		}
		gui.buttonList.forEach {
			it as GuiButton
			if (it.displayString == I18n.format("options.snooper.view"))
				it.displayString = I18n.format("options.snooper.view.new")
		}
	}
	
	@JvmStatic
	@Hook(injectOnExit = true, targetMethod = "<init>")
	fun GameSettings(thiz: GameSettings) {
		deleteStreamKeyBindings(thiz)
	}
	
	@JvmStatic
	@Hook(injectOnExit = true, targetMethod = "<init>")
	fun GameSettings(thiz: GameSettings, mc: Minecraft?, file: File?) {
		deleteStreamKeyBindings(thiz)
	}
	
	fun deleteStreamKeyBindings(thiz: GameSettings) {
		unregisterKeyBinding(thiz, thiz.field_152396_an)
		unregisterKeyBinding(thiz, thiz.field_152397_ao)
		unregisterKeyBinding(thiz, thiz.field_152398_ap)
		unregisterKeyBinding(thiz, thiz.field_152399_aq)
		
		KeyBinding.getKeybinds().remove("key.categories.stream")
	}
	
	private fun unregisterKeyBinding(thiz: GameSettings, key: KeyBinding) {
		key.keyCode = 0
		KeyBinding.keybindArray.remove(key)
		thiz.keyBindings = thiz.keyBindings.filter { it !== key }.toTypedArray()
	}
	
	
	// remove snooper sending
	@JvmStatic
	@Hook(injectOnExit = true)
	fun startSnooper(pus: PlayerUsageSnooper) {
		pus.stopSnooper()
	}
	
	@JvmStatic
	@Hook(targetMethod = "loadOptions", injectOnExit = true)
	fun loadOptionsPost(thiz: GameSettings) {
		thiz.snooperEnabled = false
	}
	
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun isSnooperEnabled(server: Minecraft) = false
	
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun isSnooperEnabled(server: MinecraftServer) = false
	
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun isSnooperEnabled(server: IntegratedServer) = false
	
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun isSnooperEnabled(server: DedicatedServer) = false
	
	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	fun setOptionValue(thiz: GameSettings, option: GameSettings.Options?, value: Int) = option == GameSettings.Options.SNOOPER_ENABLED
	
	@JvmStatic
	@Hook(injectOnExit = true)
	fun initGui(gui: GuiSnooper) {
		gui.buttonList.removeAll { (it as GuiButton).id == 1 } // snooper toggle button
		gui.buttonList.find { (it as GuiButton).id == 2 }?.let { // Done
			it as GuiButton
			it.xPosition = gui.width / 2 - 75
		}
		
		gui.field_146607_r = emptyArray<String>() // no text
	}
	
	
	// slot indices debug 
	@SideOnly(Side.CLIENT)
	@JvmStatic
	@Hook
	fun func_146977_a(gui: GuiContainer, slot: Slot) {
		if (!PatcherConfigHandler.slotIndices) return
		
		glScaled(0.5)
		mc.fontRenderer.drawString("${slot.slotIndex}:${slot.slotNumber}", slot.xDisplayPosition * 2, slot.yDisplayPosition * 2, 0xFFFFFF)
		glScalef(2f)
	}
	
	
	// remove stats
	@JvmStatic
	@Hook(injectOnExit = true, targetMethod = "<clinit>")
	fun StatList_static(static: StatList?) {
		if (!PatcherConfigHandler.disableStats) return
		
		allStats.clear()
		generalStats.clear()
		oneShotStats.values.clear()
		
		val dumbStat = StatBasic("gui.stats.new", ChatComponentTranslation("gui.stats.new")) { "" }.registerStat()
		
		mineBlockStatArray.fill(dumbStat)
		objectBreakStats.fill(dumbStat)
		objectCraftStats.fill(dumbStat)
		objectUseStats.fill(dumbStat)
		
		leaveGameStat = dumbStat
		minutesPlayedStat = dumbStat
		distanceWalkedStat = dumbStat
		distanceSwumStat = dumbStat
		distanceFallenStat = dumbStat
		distanceClimbedStat = dumbStat
		distanceFlownStat = dumbStat
		distanceDoveStat = dumbStat
		distanceByMinecartStat = dumbStat
		distanceByBoatStat = dumbStat
		distanceByPigStat = dumbStat
		field_151185_q = dumbStat
		jumpStat = dumbStat
		dropStat = dumbStat
		damageDealtStat = dumbStat
		damageTakenStat = dumbStat
		deathsStat = dumbStat
		mobKillsStat = dumbStat
		field_151186_x = dumbStat
		playerKillsStat = dumbStat
		fishCaughtStat = dumbStat
		field_151183_A = dumbStat
		field_151184_B = dumbStat
	}
	
	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	fun func_151178_a(static: StatList?): Boolean {
		if (!PatcherConfigHandler.disableStats) return false
		
		AchievementList.init()
		EntityList.func_151514_a()
		
		return true
	}
	
	@JvmStatic
	@Hook(returnCondition = ON_TRUE)
	fun addStat(player: EntityPlayerMP, stat: StatBase?, amount: Int) = PatcherConfigHandler.disableStats && stat?.isAchievement != true
	
	@SideOnly(Side.CLIENT)
	@JvmStatic
	@Hook(injectOnExit = true)
	fun func_146541_h(gui: GuiStats) {
		if (!PatcherConfigHandler.disableStats) return
		
		gui.buttonList.removeAll { (it as GuiButton).id != 0 }
		(gui.buttonList[0] as GuiButton).xPosition = gui.width / 2 - 75
	}
	
	@SideOnly(Side.CLIENT)
	@JvmStatic
	@Hook(injectOnExit = true)
	fun initGui(gui: GuiStats) {
		if (PatcherConfigHandler.disableStats)
			gui.field_146542_f = ""
	}
	
	
	// Remove ladder interference with flight 
	@JvmStatic
	@Hook(returnCondition = ON_TRUE, booleanReturnConstant = false)
	fun isLivingOnLadder(static: ForgeHooks?, block: Block?, world: World?, x: Int, y: Int, z: Int, entity: EntityLivingBase?) =
		entity is EntityPlayer && entity.capabilities.isFlying
	
	// remove extra langs
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun loadLanguagesFor(reg: LanguageRegistry, container: ModContainer?, side: Side?) = Unit
}