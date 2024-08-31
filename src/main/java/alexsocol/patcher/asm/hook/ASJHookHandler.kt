package alexsocol.patcher.asm.hook

import alexsocol.asjlib.*
import alexsocol.asjlib.extendables.block.*
import alexsocol.asjlib.render.ICustomArmSwingEndEntity
import alexsocol.patcher.PatcherConfigHandler
import alexsocol.patcher.event.*
import alexsocol.patcher.helper.*
import alexsocol.patcher.helper.OFHelper.shadersmodSupport
import alexsocol.patcher.network.*
import biomesoplenty.common.blocks.BlockBOPLog
import biomesoplenty.common.itemblocks.ItemBlockLog
import cofh.asmhooks.HooksCore
import cpw.mods.fml.client.*
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.relauncher.*
import gloomyfolken.hooklib.asm.Hook
import gloomyfolken.hooklib.asm.Hook.ReturnValue
import gloomyfolken.hooklib.asm.ReturnCondition.*
import net.minecraft.block.*
import net.minecraft.block.material.Material
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.*
import net.minecraft.client.multiplayer.PlayerControllerMP
import net.minecraft.client.renderer.*
import net.minecraft.client.renderer.entity.Render
import net.minecraft.command.*
import net.minecraft.command.server.CommandSummon
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.enchantment.EnchantmentHelper
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
import net.minecraft.init.Blocks
import net.minecraft.inventory.*
import net.minecraft.item.*
import net.minecraft.nbt.*
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.potion.*
import net.minecraft.server.*
import net.minecraft.tileentity.TileEntityFurnace
import net.minecraft.util.*
import net.minecraft.world.*
import net.minecraft.world.biome.*
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.storage.AnvilChunkLoader
import net.minecraftforge.client.event.EntityViewRenderEvent
import net.minecraftforge.common.*
import net.minecraftforge.common.ISpecialArmor.ArmorProperties
import net.minecraftforge.common.util.*
import net.minecraftforge.fluids.IFluidBlock
import org.lwjgl.opengl.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.NVFogDistance.*
import org.objectweb.asm.Opcodes
import java.awt.Desktop
import java.awt.Toolkit
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
			e.printStackTrace()
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
		
		nbt.setString("id", GameRegistry.findUniqueIdentifierFor(stack.field_151002_e)?.toString() ?: return null)
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
		
		val id = nbt.getString("id")
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
	
	
	// armor can't block damage that is set to bypass armor
	// shitcode because LotR author don't want to fix their mistake -_-
	
	var originalDamage = 0f
	
	@JvmStatic
	@Hook(targetMethod = "ApplyArmor", returnCondition = NEVER)
	fun ApplyArmorPre(props: ArmorProperties?, entity: EntityLivingBase?, inventory: Array<ItemStack?>, source: DamageSource, damage: Double): Float {
		originalDamage = damage.F
		return originalDamage
	}
	
	@JvmStatic
	@Hook(targetMethod = "ApplyArmor", returnCondition = ALWAYS, injectOnExit = true)
	fun ApplyArmorPost(props: ArmorProperties?, entity: EntityLivingBase?, inventory: Array<ItemStack?>, source: DamageSource, damage: Double, @ReturnValue result: Float): Float {
		val newDamage = if (source.isUnblockable && !AlchemicalWizardryIntegration.hasVoidSigil(inventory, source)) originalDamage else result
		originalDamage = 0f
		return newDamage
	}
	
	// same fix but for Cauldron -_-
	var originalDamageC = 0f
	
	@JvmStatic
	@Hook(targetMethod = "ApplyArmor", returnCondition = NEVER, isMandatory = false)
	fun ApplyArmorPre(props: ArmorProperties?, entity: EntityLivingBase?, inventory: Array<ItemStack?>, source: DamageSource, damage: Double, damageArmor: Boolean): Float {
		originalDamageC = damage.F
		return originalDamageC
	}
	
	@JvmStatic
	@Hook(targetMethod = "ApplyArmor", returnCondition = ALWAYS, isMandatory = false, injectOnExit = true)
	fun ApplyArmorPost(props: ArmorProperties?, entity: EntityLivingBase?, inventory: Array<ItemStack?>, source: DamageSource, damage: Double, damageArmor: Boolean, @ReturnValue result: Float): Float {
		val newDamage = if (source.isUnblockable && !AlchemicalWizardryIntegration.hasVoidSigil(inventory, source)) originalDamageC else result
		originalDamageC = 0f
		return newDamage
	}
	
	
	// events
	@JvmStatic
	@Hook(injectOnExit = true)
	fun wakeAllPlayers(world: WorldServer) {
		MinecraftForge.EVENT_BUS.post(ServerWakeUpEvent(world))
	}
	
	@JvmStatic
	@Hook(injectOnExit = true)
	fun onNewPotionEffect(e: EntityLivingBase, pe: PotionEffect) {
		MinecraftForge.EVENT_BUS.post(LivingPotionEvent.Add.Post(e, pe))
	}
	
	@JvmStatic
	@Hook(injectOnExit = true)
	fun onChangedPotionEffect(e: EntityLivingBase, pe: PotionEffect, was: Boolean) {
		MinecraftForge.EVENT_BUS.post(LivingPotionEvent.Change.Post(e, pe, was))
	}
	
	@JvmStatic
	@Hook(injectOnExit = true)
	fun onFinishedPotionEffect(e: EntityLivingBase, pe: PotionEffect) {
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
		
		// FUCKING SIDEONLY SHIT
		val nbt = NBTTagCompound()
		stats.writeNBT(nbt)
		
		nbt.setInteger("foodLevel", min(e.newFoodLevel + stats.foodLevel, 20))
		nbt.setFloat("foodSaturationLevel", min(stats.saturationLevel + e.newFoodLevel * e.newSaturationLevel * 2f, stats.foodLevel.F))
		
		stats.readNBT(nbt)
	}
	
	
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
			ASJUtilities.log("Well, that was expected. Ignore.")
			ex.printStackTrace()
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
	
	// Fix nbt clearing in Enchanting Table
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun transferStackInSlot(container: ContainerEnchantment, player: EntityPlayer?, slotID: Int): ItemStack? {
		var itemstack: ItemStack? = null
		val slot = container.inventorySlots[slotID] as Slot?
		if (slot != null && slot.hasStack) {
			val itemstack1 = slot.stack
			itemstack = itemstack1.copy()
			if (slotID == 0) {
				if (!ASJSuperWrapperHandler.mergeItemStack(container, itemstack1, 1, 37, true)) return null
			} else {
				if ((container.inventorySlots[0] as Slot).hasStack || !(container.inventorySlots[0] as Slot).isItemValid(itemstack1)) return null
				
				if (itemstack1.hasTagCompound() && itemstack1.stackSize == 1) {
					(container.inventorySlots[0] as Slot).putStack(itemstack1.copy())
					itemstack1.stackSize = 0
				} else if (itemstack1.stackSize >= 1) {
					val copy = itemstack1.copy()
					copy.stackSize = 1
					(container.inventorySlots[0] as Slot).putStack(copy)
					--itemstack1.stackSize
				}
			}
			if (itemstack1.stackSize == 0) slot.putStack(null as ItemStack?)
			else slot.onSlotChanged()
			
			if (itemstack1.stackSize == itemstack.stackSize) return null
			
			slot.onPickupFromSlot(player, itemstack1)
		}
		return itemstack
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
	@Hook
	fun getSubBlocks(block: BlockTallGrass, item: Item?, tab: CreativeTabs?, list: MutableList<ItemStack?>) {
		list.add(ItemStack(item))
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
	
	// fog fixes
	@SideOnly(Side.CLIENT)
	@JvmStatic
	@Hook(returnCondition = ALWAYS)
	fun setupFog(renderer: EntityRenderer, fogMode: Int, renderPartialTicks: Float) {
		val entity = renderer.mc.renderViewEntity
		val creative = if (entity is EntityPlayer) entity.capabilities.isCreativeMode else false
		
		OFHelper.setStandardFog(renderer, false)
		
		if (fogMode == 999) {
			glFog(GL_FOG_COLOR, renderer.setFogColorBuffer(0f, 0f, 0f, 1f))
			shadersmodSupport(GL_FOG_MODE, GL_LINEAR)
			glFogf(GL_FOG_START, 0f)
			glFogf(GL_FOG_END, 8f)
			
			if (GLContext.getCapabilities().GL_NV_fog_distance)
				shadersmodSupport(GL_FOG_DISTANCE_MODE_NV, GL_EYE_RADIAL_NV)
			
			glFogf(GL_FOG_START, 0f)
			return
		}
		
		glFog(GL_FOG_COLOR, renderer.setFogColorBuffer(renderer.fogColorRed, renderer.fogColorGreen, renderer.fogColorBlue, 1f))
		
		glNormal3f(0f, -1f, 0f)
		glColor4f(1f, 1f, 1f, 1f)
		
		val block = ActiveRenderInfo.getBlockAtEntityViewpoint(renderer.mc.theWorld, entity, renderPartialTicks)
		val event = EntityViewRenderEvent.FogDensity(renderer, entity, block, renderPartialTicks.D, 0.1f)
		
		if (MinecraftForge.EVENT_BUS.post(event)) {
			glFogf(GL_FOG_DENSITY, event.density)
		} else if (entity.isPotionActive(Potion.blindness) && !creative) {
			val pe = entity.getActivePotionEffect(Potion.blindness)
			var distance = 5f / (pe.amplifier + 1)
			
			if (pe.duration < 20)
				distance += (renderer.farPlaneDistance - distance) * (1f - pe.duration / 20f)
			
			shadersmodSupport(GL_FOG_MODE, GL_LINEAR)
			
			if (fogMode < 0) {
				glFogf(GL_FOG_START, 0f)
				glFogf(GL_FOG_END, distance * 0.8f)
			} else {
				glFogf(GL_FOG_START, distance * 0.25f)
				glFogf(GL_FOG_END, distance)
			}
			
			OFHelper.fancyFogCheck()
		} else if (renderer.cloudFog) {
			shadersmodSupport(GL_FOG_MODE, GL_EXP)
			glFogf(GL_FOG_DENSITY, 0.1f)
		} else if (block.material === Material.water) {
			shadersmodSupport(GL_FOG_MODE, GL_EXP)
			
			if (OFHelper.isClearWater())
				glFogf(GL_FOG_DENSITY, 0.01f)
			else if (entity.isPotionActive(Potion.waterBreathing)) {
				glFogf(GL_FOG_DENSITY, 0.05f)
			} else {
				glFogf(GL_FOG_DENSITY, 0.1f - min(3, EnchantmentHelper.getRespiration(entity)) * 0.03f)
			}
		} else if (block.material === Material.lava) {
			shadersmodSupport(GL_FOG_MODE, GL_EXP)
			glFogf(GL_FOG_DENSITY, if (creative) 0.05f else 2f)
		} else {
			var farPlane = renderer.farPlaneDistance
			
			OFHelper.setStandardFog(renderer, true)
			
			if (OFHelper.isVoidFog() && renderer.mc.theWorld.provider.worldHasVoidParticles && !creative) {
				var brightness = (((entity.getBrightnessForRender(renderPartialTicks) and 0xF00000) shr 20) / 16.0 + (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * renderPartialTicks + 4.0) / 32.0).F
				
				if (brightness < 1f) {
					if (brightness < 0f) brightness = 0f
					
					brightness *= brightness
					
					var newPlane = 100f * brightness
					if (newPlane < 5f) newPlane = 5f
					
					if (farPlane > newPlane) farPlane = newPlane
				}
			}
			
			shadersmodSupport(GL_FOG_MODE, GL_LINEAR)
			
			if (fogMode < 0) {
				glFogf(GL_FOG_START, 0f)
				glFogf(GL_FOG_END, farPlane)
			} else {
				glFogf(GL_FOG_START, farPlane * OFHelper.getFogStart())
				glFogf(GL_FOG_END, farPlane)
			}
			
			if (GLContext.getCapabilities().GL_NV_fog_distance) {
				if (OFHelper.isFogFancy())
					shadersmodSupport(GL_FOG_DISTANCE_MODE_NV, GL_EYE_RADIAL_NV)
				
				if (OFHelper.isFogFast())
					shadersmodSupport(GL_FOG_DISTANCE_MODE_NV, GL_EYE_PLANE_ABSOLUTE_NV)
			}
			
			if (renderer.mc.theWorld.provider.doesXZShowFog(entity.posX.mfloor(), entity.posZ.mfloor())) {
				OFHelper.XZFog(renderer.farPlaneDistance)
			}
			
			MinecraftForge.EVENT_BUS.post(EntityViewRenderEvent.RenderFogEvent(renderer, entity, block, renderPartialTicks.D, fogMode, farPlane))
		}
		
		glEnable(GL_COLOR_MATERIAL)
		glColorMaterial(GL_FRONT, GL_AMBIENT)
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
	
	
	// chunk reforcing after world reload
	@JvmStatic
	@Hook(injectOnExit = true)
	fun loadWorld(static: ForgeChunkManager?, world: World) {
		val persistentChunks = world.persistentChunks.keySet()
		
		ForgeChunkManager.tickets[world]?.values()?.forEach {
			val ticketChunks = HashSet<ChunkCoordIntPair>()
			ticketChunks.addAll(it.requestedChunks)
			
			ticketChunks.forEach inner@ { c ->
				if (c in persistentChunks) return@inner
				
				try {
					ForgeChunkManager.forceChunk(it, c)
				} catch (e: Exception) {
					ASJUtilities.error("Failed to force chunk $c requested by ${it.modId}. It won't persist until requested again.", e)
				}
			}
		}
	}
	
	@JvmStatic
	fun addChunksToTicket(nbt: NBTTagCompound, ticket: ForgeChunkManager.Ticket) {
		val list = nbt.getTagList("ChunkList", Constants.NBT.TAG_INT_ARRAY)
		
		for (i in 0 until list.tagCount()) {
			val (x, z) = list.func_150306_c(i)
			ticket.requestedChunks.add(ChunkCoordIntPair(x, z))
		}
	}
	
	@JvmStatic
	fun storeChunksFromTicket(nbt: NBTTagCompound, ticket: ForgeChunkManager.Ticket) {
		val list = NBTTagList()
		nbt.setTag("ChunkList", list)
		
		ticket.chunkList.forEach {
			list.appendTag(NBTTagIntArray(intArrayOf(it.chunkXPos, it.chunkZPos)))
		}
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
	fun calcPotionLiquidColor(potions: Collection<PotionEffect>?): Int {
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
}