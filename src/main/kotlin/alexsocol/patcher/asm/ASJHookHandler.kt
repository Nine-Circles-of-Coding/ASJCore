package alexsocol.patcher.asm

import alexsocol.asjlib.*
import alexsocol.asjlib.mixinifaces.IHostedFoodStats
import alexsocol.asjlib.render.ICustomArmSwingEndEntity
import alexsocol.patcher.PatcherConfigHandler
import alexsocol.patcher.event.*
import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.relauncher.*
import gloomyfolken.hooklib.asm.*
import gloomyfolken.hooklib.asm.Hook.ReturnValue
import net.minecraft.block.*
import net.minecraft.block.material.Material
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.*
import net.minecraft.client.renderer.entity.Render
import net.minecraft.command.*
import net.minecraft.command.server.CommandSummon
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.*
import net.minecraft.entity.EntityList.EntityEggInfo
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.boss.*
import net.minecraft.entity.effect.*
import net.minecraft.entity.monster.*
import net.minecraft.entity.passive.EntityMooshroom
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.init.Blocks
import net.minecraft.inventory.*
import net.minecraft.item.*
import net.minecraft.nbt.*
import net.minecraft.potion.*
import net.minecraft.server.ServerEula
import net.minecraft.tileentity.TileEntityFurnace
import net.minecraft.util.*
import net.minecraft.world.*
import net.minecraft.world.biome.BiomeGenBase
import net.minecraftforge.client.event.EntityViewRenderEvent
import net.minecraftforge.common.*
import net.minecraftforge.common.ISpecialArmor.ArmorProperties
import net.minecraftforge.common.util.ForgeDirection
import org.lwjgl.opengl.*
import org.objectweb.asm.Opcodes
import java.io.File
import java.nio.FloatBuffer
import java.util.*
import kotlin.math.*

@Suppress("UNUSED_PARAMETER", "unused", "FunctionName", "UNCHECKED_CAST")
object ASJHookHandler {

	@SideOnly(Side.SERVER)
	@JvmStatic
	@Hook(injectOnExit = true, targetMethod = "<init>")
	fun ServerEula(thiz: ServerEula, p_i1227_1_: File) {
		ASJReflectionHelper.setFinalValue(thiz, true, "field_154351_c")
	}

	// summon lightning bolt in /summon command
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS, targetMethod = "<init>", createMethod = true, superClass = "net/minecraft/entity/effect/EntityWeatherEffect.${Opcodes.ALOAD}.1.(Lnet/minecraft/world/World;)V")
	fun EntityLightningBolt(thiz: EntityLightningBolt, world: World) {
		thiz.lightningState = 2
		thiz.boltVertex = (Math.random() * Long.MAX_VALUE).toLong()
		thiz.boltLivingTime = ASJUtilities.randInBounds(1, 3)
	}

	// AIOOBE 257+ crash fix
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS, targetMethod = "<clinit>")
	fun EntityEnderman(thiz: EntityEnderman?) {
		val uuid = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0") // Entries in at.cfg causes gradle error
		ASJReflectionHelper.setStaticFinalValue(EntityEnderman::class.java, uuid, "attackingSpeedBoostModifierUUID", "field_110192_bp")
		ASJReflectionHelper.setStaticFinalValue(EntityEnderman::class.java, AttributeModifier(uuid, "Attacking speed boost", 6.199999809265137, 0).setSaved(false), "attackingSpeedBoostModifier", "field_110193_bq")

		arrayOf(Blocks.grass, Blocks.dirt, Blocks.sand, Blocks.gravel, Blocks.yellow_flower, Blocks.red_flower, Blocks.brown_mushroom, Blocks.red_mushroom, Blocks.tnt, Blocks.cactus, Blocks.clay, Blocks.pumpkin, Blocks.melon_block, Blocks.mycelium).forEach {
			EntityEnderman.setCarriable(it, true)
		}
	}

	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_TRUE)
	fun spawnEntityInWorld(world: World, target: Entity?): Boolean {
		if (target !is EntityWeatherEffect)
			return false

		world.addWeatherEffect(target)
		return false
	}

	// damageMobArmor config prop impl
	@JvmStatic
	@Hook
	fun damageArmor(entity: EntityLivingBase, damage: Float) {
		if (!PatcherConfigHandler.damageMobArmor) return

		val dmg = max(damage / 4f, 1f).I
		for (i in 1..4) entity.getEquipmentInSlot(i)?.damageItem(dmg, entity)
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
	fun addEntityEgg(entity: Class<*>, i: Int, j: Int) {
		val id = EntityList.classToIDMapping[entity] as Int
		if (EntityList.entityEggs[id] != null) return
		EntityList.entityEggs[id] = EntityEggInfo(id, i, j)
	}

	// gm alias for /gamemode
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS, createMethod = true)
	fun getCommandAliases(c: CommandGameMode): List<String> {
		return listOf("gm")
	}

	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS, createMethod = true)
	fun getCommandAliases(c: CommandDefaultGameMode): List<String>? {
		return null
	}

	// summon usage
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS, createMethod = true)
	fun getCommandUsage(c: CommandSummon, sender: ICommandSender?): String {
		return "commands.summon.usage.new"
	}

	// entity batches in /summon command
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_TRUE)
	fun processCommand(c: CommandSummon, sender: ICommandSender?, args: Array<String?>): Boolean {
		val count = args.getOrNull(1) ?: return false
		if (!count.startsWith('x')) return false

		val newArgs = args.toMutableList().apply { removeAt(1) }.toTypedArray()
		for (i in 0 until count.substring(1).toInt())
			c.processCommand(sender, newArgs)

		return true
	}

	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS)
	fun func_147182_d(c: CommandSummon): Array<String> {
		return (EntityList.stringToClassMapping.keys as Set<String>).toTypedArray()
	}

	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS)
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

	// biome dup id fix
	@JvmStatic
	@Hook(targetMethod = "<init>")
	fun BiomeGenBase(thiz: BiomeGenBase, id: Int, register: Boolean) {
		if (PatcherConfigHandler.biomeDuplication && BiomeGenBase.getBiomeGenArray()[id] != null)
			throw IllegalArgumentException("Biome with id $id is already registered!")
	}

	// potion dup id fix
	@JvmStatic
	@Hook(targetMethod = "<init>")
	fun Potion(thiz: Potion, id: Int, bad: Boolean, color: Int) {
		if (PatcherConfigHandler.potionDuplication && Potion.potionTypes[id] != null)
			throw IllegalArgumentException("Potion with id $id is already registered!")
	}

	// stack NBT fix
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL)
	fun writeToNBT(stack: ItemStack, nbt: NBTTagCompound): NBTTagCompound? {
		if (!PatcherConfigHandler.textIDs) return null

		nbt.setString("id", GameRegistry.findUniqueIdentifierFor(stack.field_151002_e).toString())
		nbt.setInteger("Count", stack.stackSize)
		nbt.setInteger("Damage", stack.itemDamage)

		stack.stackTagCompound?.let { nbt.setTag("tag", it) }

		return nbt
	}

	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_TRUE)
	fun readFromNBT(stack: ItemStack, nbt: NBTTagCompound): Boolean {
		if (!PatcherConfigHandler.textIDs) return false
		if (nbt.hasNoTags()) return true

		migrate(nbt)

		val id = nbt.getString("id")
		if (id.isBlank()) return true

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

		val stack = ItemStack(Item.getItemById(nbt.getShort("id").toInt()), nbt.getByte("Count").toInt(), max(0, nbt.getShort("Damage").toInt()))

		if (nbt.hasKey("tag", 10))
			stack.stackTagCompound = nbt.getCompoundTag("tag")

		nbt.removeTag("id")
		nbt.removeTag("Count")
		nbt.removeTag("Damage")
		nbt.removeTag("tag")

		stack.writeToNBT(nbt)
	}

	// armor can't block damage that is set to bypass armor
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_TRUE, returnType = "float", returnAnotherMethod = "armorNotApplied")
	fun ApplyArmor(props: ArmorProperties?, entity: EntityLivingBase?, inventory: Array<ItemStack?>?, source: DamageSource, damage: Double): Boolean {
		return source.isUnblockable
	}

	@JvmStatic
	fun armorNotApplied(props: ArmorProperties?, entity: EntityLivingBase?, inventory: Array<ItemStack?>?, source: DamageSource?, damage: Double) = damage.F

//	@JvmStatic
//	@Hook(returnCondition = ReturnCondition.ON_TRUE)
//	fun writeItemStackToBuffer(buf: PacketBuffer, stack: ItemStack?): Boolean {
//		if (!PatcherConfigHandler.textIDs) return false
//
//		if (stack == null) {
//			buf.writeStringToBuffer("")
//			return true
//		}
//
//		val name = GameRegistry.findUniqueIdentifierFor(stack.item).toString()
//		buf.writeInt(name.length)
//		buf.writeStringToBuffer(name)
//		buf.writeInt(stack.stackSize)
//		buf.writeInt(stack.itemDamage)
//		var nbt: NBTTagCompound? = null
//		if (stack.item.isDamageable || stack.item.shareTag)
//			nbt = stack.stackTagCompound
//
//		buf.writeNBTTagCompoundToBuffer(nbt)
//
//		return true
//	}
//
//	@JvmStatic
//	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL)
//	fun readItemStackFromBuffer(buf: PacketBuffer): ItemStack? {
//		if (!PatcherConfigHandler.textIDs) return null
//
//		val id = buf.readStringFromBuffer(buf.readInt())
//		if (id.isEmpty())
//			return null
//
//		val (modid, name) = id.split(':')
//		val item = GameRegistry.findItem(modid, name) ?: return null
//		val count = buf.readInt()
//		val meta = buf.readInt()
//		val stack = ItemStack(item, count, meta)
//		stack.stackTagCompound = buf.readNBTTagCompoundFromBuffer()
//
//		return stack
//	}

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
	@Hook(returnCondition = ReturnCondition.ON_TRUE)
	fun doRenderShadowAndFire(render: Render, entity: Entity, x: Double, y: Double, z: Double, yaw: Float, ticks: Float): Boolean {
		return MinecraftForge.EVENT_BUS.post(RenderEntityPostEvent(entity, x, y, z, yaw))
	}

	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_TRUE, targetMethod = "func_150000_e")
	fun tryToCreatePortal(portal: BlockPortal, world: World, x: Int, y: Int, z: Int) = MinecraftForge.EVENT_BUS.post(NetherPortalActivationEvent(world, x, y, z))

	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS)
	fun addStats(stats: FoodStats, foodLevel: Int, foodSaturationLevel: Float) {
		val e = PlayerEatingEvent((stats as IHostedFoodStats).host, foodLevel, foodSaturationLevel)
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

	var portalHook = false

	@JvmStatic
	@Hook
	fun onLivingUpdate(player: EntityPlayerSP) {
		portalHook = PatcherConfigHandler.portalHook
	}

	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_TRUE)
	@SideOnly(Side.CLIENT)
	fun displayGuiScreen(mc: Minecraft, gui: GuiScreen?): Boolean {
		return if (portalHook && mc.thePlayer?.inPortal == true) {
			portalHook = false
			gui == null
		} else false
	}

	// BlockFence connection fix
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_TRUE)
	fun canConnectFenceTo(fence: BlockFence, world: IBlockAccess, x: Int, y: Int, z: Int) = world.getBlock(x, y, z) is BlockFence

	// BlockWall fix

	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS, createMethod = true)
	fun isSideSolid(wall: BlockWall, world: IBlockAccess, x: Int, y: Int, z: Int, side: ForgeDirection) = when (side) {
		ForgeDirection.DOWN -> true
		ForgeDirection.UP   -> wall.blockBoundsMaxY == 1.0
		else                -> false
	}

	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS)
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
	@Hook(returnCondition = ReturnCondition.ALWAYS)
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
	@Hook(returnCondition = ReturnCondition.ALWAYS)
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
	@Hook(returnCondition = ReturnCondition.ALWAYS)
	fun getNightVisionBrightness(render: EntityRenderer, player: EntityPlayer, partialTicks: Float) = if ((player.getActivePotionEffect(Potion.nightVision.id)?.duration ?: 0) > 0) 1f else 0f

	// Fix nbt clearing in Enchanting Table

	val mergeItemStack by lazy {
		ASJReflectionHelper.getMethod(Container::class.java, arrayOf("mergeItemStack", "func_75135_a"), arrayOf(ItemStack::class.java, Int::class.java, Int::class.java, Boolean::class.java))?.also {
			it.isAccessible = true
		}
	}

	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS)
	fun transferStackInSlot(container: ContainerEnchantment, player: EntityPlayer?, slotID: Int): ItemStack? {
		var itemstack: ItemStack? = null
		val slot = container.inventorySlots[slotID] as Slot?
		if (slot != null && slot.hasStack) {
			val itemstack1 = slot.stack
			itemstack = itemstack1.copy()
			if (slotID == 0) {
				// I'm sooo sorry but all inheritance will just fuck everything up
				if (mergeItemStack?.invoke(container, itemstack1, 1, 37, true) == false) return null
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
	@Hook(returnCondition = ReturnCondition.ON_TRUE)
	fun itemInteractionForEntity(item: ItemNameTag, stack: ItemStack, player: EntityPlayer?, target: EntityLivingBase?): Boolean {
		if (!stack.hasDisplayName() && target is EntityLiving) {
			target.customNameTag = ""
			return true
		}

		return false
	}

	// can't shear dead animals (dupe fix)
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ON_NOT_NULL)
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
	@Hook(returnCondition = ReturnCondition.ALWAYS)
	fun getBurnTimeRemainingScaled(furnace: TileEntityFurnace, mod: Int): Int {
		if (furnace.currentItemBurnTime == 0) {
			furnace.currentItemBurnTime = 200
		}

		return (furnace.furnaceBurnTime.D / furnace.currentItemBurnTime * mod).I
	}

	// fog fixes
	@SideOnly(Side.CLIENT)
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS)
	fun setupFog(renderer: EntityRenderer, fogMode: Int, renderPartialTicks: Float) {
		val entitylivingbase = renderer.mc.renderViewEntity
		val creative = if (entitylivingbase is EntityPlayer) entitylivingbase.capabilities.isCreativeMode else false

		fun setFogColorBuffer(p_78469_1_: Float, p_78469_2_: Float, p_78469_3_: Float, p_78469_4_: Float): FloatBuffer {
			renderer.fogColorBuffer.clear()
			renderer.fogColorBuffer.put(p_78469_1_).put(p_78469_2_).put(p_78469_3_).put(p_78469_4_)
			renderer.fogColorBuffer.flip()
			return renderer.fogColorBuffer
		}

		if (fogMode == 999) {
			GL11.glFog(GL11.GL_FOG_COLOR, setFogColorBuffer(0f, 0f, 0f, 1f))
			GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR)
			GL11.glFogf(GL11.GL_FOG_START, 0f)
			GL11.glFogf(GL11.GL_FOG_END, 8f)

			if (GLContext.getCapabilities().GL_NV_fog_distance) {
				GL11.glFogi(NVFogDistance.GL_FOG_DISTANCE_MODE_NV, NVFogDistance.GL_EYE_RADIAL_NV)
			}

			GL11.glFogf(GL11.GL_FOG_START, 0f)
		} else {
			GL11.glFog(GL11.GL_FOG_COLOR, setFogColorBuffer(renderer.fogColorRed, renderer.fogColorGreen, renderer.fogColorBlue, 1f))
			GL11.glNormal3f(0f, -1f, 0f)
			GL11.glColor4f(1f, 1f, 1f, 1f)
			val block = ActiveRenderInfo.getBlockAtEntityViewpoint(renderer.mc.theWorld, entitylivingbase, renderPartialTicks)
			var f1: Float

			val event = EntityViewRenderEvent.FogDensity(renderer, entitylivingbase, block, renderPartialTicks.D, 0.1f)

			if (MinecraftForge.EVENT_BUS.post(event)) {
				GL11.glFogf(GL11.GL_FOG_DENSITY, event.density)
			} else if (entitylivingbase.isPotionActive(Potion.blindness) && !creative) {
				val pe = entitylivingbase.getActivePotionEffect(Potion.blindness.id)!!
				val j = pe.duration
				f1 = 5f / (pe.amplifier + 1)

				if (j < 20) {
					f1 += (renderer.farPlaneDistance - f1) * (1f - j.F / 20f)
				}

				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR)

				if (fogMode < 0) {
					GL11.glFogf(GL11.GL_FOG_START, 0f)
					GL11.glFogf(GL11.GL_FOG_END, f1 * 0.8f)
				} else {
					GL11.glFogf(GL11.GL_FOG_START, f1 * 0.25f)
					GL11.glFogf(GL11.GL_FOG_END, f1)
				}

				if (GLContext.getCapabilities().GL_NV_fog_distance) {
					GL11.glFogi(NVFogDistance.GL_FOG_DISTANCE_MODE_NV, NVFogDistance.GL_EYE_RADIAL_NV)
				}
			} else if (renderer.cloudFog) {
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP)
				GL11.glFogf(GL11.GL_FOG_DENSITY, 0.1f)
			} else if (block.material === Material.water) {
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP)

				if (entitylivingbase.isPotionActive(Potion.waterBreathing)) {
					GL11.glFogf(GL11.GL_FOG_DENSITY, if (PatcherConfigHandler.clearWater) 0.01f else 0.05f)
				} else {
					GL11.glFogf(GL11.GL_FOG_DENSITY, if (PatcherConfigHandler.clearWater) 0.01f else 0.1f - EnchantmentHelper.getRespiration(entitylivingbase).F * 0.03f)
				}
			} else if (block.material === Material.lava) {
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP)
				GL11.glFogf(GL11.GL_FOG_DENSITY, 2f)
			} else {
				f1 = renderer.farPlaneDistance

				if (renderer.mc.theWorld.provider.worldHasVoidParticles && PatcherConfigHandler.voidFog && !creative) {
					var d0 = (entitylivingbase.getBrightnessForRender(renderPartialTicks) and 15728640 shr 20).D / 16.0 + (entitylivingbase.lastTickPosY + (entitylivingbase.posY - entitylivingbase.lastTickPosY) * renderPartialTicks.D + 4.0) / 32.0

					if (d0 < 1.0) {
						if (d0 < 0.0) {
							d0 = 0.0
						}

						d0 *= d0
						var f2 = 100f * d0.F

						if (f2 < 5f) {
							f2 = 5f
						}

						if (f1 > f2) {
							f1 = f2
						}
					}
				}

				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR)

				if (fogMode < 0) {
					GL11.glFogf(GL11.GL_FOG_START, 0f)
					GL11.glFogf(GL11.GL_FOG_END, f1)
				} else {
					GL11.glFogf(GL11.GL_FOG_START, f1 * 0.75f)
					GL11.glFogf(GL11.GL_FOG_END, f1)
				}

				if (GLContext.getCapabilities().GL_NV_fog_distance) {
					GL11.glFogi(NVFogDistance.GL_FOG_DISTANCE_MODE_NV, NVFogDistance.GL_EYE_RADIAL_NV)
				}

				if (renderer.mc.theWorld.provider.doesXZShowFog(entitylivingbase.posX.I, entitylivingbase.posZ.I)) {
					GL11.glFogf(GL11.GL_FOG_START, f1 * 0.05f)
					GL11.glFogf(GL11.GL_FOG_END, min(f1, 192f) * 0.5f)
				}

				MinecraftForge.EVENT_BUS.post(EntityViewRenderEvent.RenderFogEvent(renderer, entitylivingbase, block, renderPartialTicks.D, fogMode, f1))
			}

			GL11.glEnable(GL11.GL_COLOR_MATERIAL)
			GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT)
		}
	}

	// fixing some occasional OptiFine crashes
	@SideOnly(Side.CLIENT)
	@Synchronized
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS)
	fun deleteDisplayLists(gla: GLAllocation?, id: Int) {
		if (GLAllocation.mapDisplayLists.contains(id)) GL11.glDeleteLists(id, GLAllocation.mapDisplayLists.remove(id) as Int)
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
	@Hook(returnCondition = ReturnCondition.ON_TRUE)
	fun trackBrokenTexture(handler: FMLClientHandler, resourceLocation: ResourceLocation, error: String?): Boolean {
		if (error == null) {
			handler.trackBrokenTexture(resourceLocation, "Unknown Error")
			return true
		}

		return false
	}

	// custom arm swinging
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS, injectOnExit = true)
	fun getArmSwingAnimationEnd(e: EntityLivingBase, @ReturnValue result: Int) = if (e is ICustomArmSwingEndEntity) e.getCustomArmSwingAnimationEnd() else result

	// NBT ByteArray to string fix -- STUPID FUCKING MOTHERFUCKERS
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS)
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

//	// flag count expansion to 32
//	// Byte -> Int in ASJClassTransformer
//	@JvmStatic
//	@Hook(returnCondition = ReturnCondition.ALWAYS)
//	fun getFlag(entity: Entity, id: Int) = entity.dataWatcher.getWatchableObjectInt(0) and (1 shl id) != 0
//
//	@JvmStatic
//	@Hook(returnCondition = ReturnCondition.ALWAYS)
//	fun setFlag(entity: Entity, id: Int, value: Boolean) {
//		val allFlags = entity.dataWatcher.getWatchableObjectInt(0)
//
//		if (value) {
//			entity.dataWatcher.updateObject(0, allFlags or (1 shl id))
//		} else {
//			entity.dataWatcher.updateObject(0, allFlags and (1 shl id).inv())
//		}
//	}

	// NPE fix
	@JvmStatic
	@Hook(returnCondition = ReturnCondition.ALWAYS)
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
	@Hook(returnCondition = ReturnCondition.ON_TRUE)
	fun renderVignette(gui: GuiIngame, vignetteBrightness: Float, width: Int, height: Int): Boolean {
		val disable = !PatcherConfigHandler.vignette
		if (disable) OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
		return disable
	}

	// NPE fix
	@JvmStatic
	@Hook(injectOnExit = true)
	fun getCollidingBoundingBoxes(world: World, entity: Entity?, aabb: AxisAlignedBB?, @ReturnValue result: MutableList<AxisAlignedBB?>): List<AxisAlignedBB?> {
		result.removeAll { it == null }
		return result
	}
}
