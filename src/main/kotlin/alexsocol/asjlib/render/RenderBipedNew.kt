package alexsocol.asjlib.render


import alexsocol.asjlib.*
import com.google.common.collect.Maps
import com.mojang.authlib.GameProfile
import net.minecraft.block.Block
import net.minecraft.client.model.ModelBiped
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.entity.RenderLiving
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer
import net.minecraft.entity.*
import net.minecraft.init.Items
import net.minecraft.item.*
import net.minecraft.nbt.NBTUtil
import net.minecraft.util.*
import net.minecraftforge.client.*
import org.lwjgl.opengl.GL11.*

abstract class RenderBipedNew(var mainModelNew: ModelBipedNew, shadowSize: Float): RenderLiving(mainModelNew, shadowSize) {
	
	protected var armorModel = ModelBiped(1f)
	protected var armorModelPants = ModelBiped(0.5f)
	
	override fun shouldRenderPass(entity: EntityLivingBase, path: Int, ticks: Float): Int {
		val stack = entity.getEquipmentInSlot(3 - path + 1) ?: return -1
		val item = stack.item as? ItemArmor ?: return -1
		
		bindTexture(getArmorResource(entity, stack, path, null))
		var armorModel = if (path == 2) armorModelPants else armorModel
		
		armorModel.bipedHead.showModel = path == 0
		armorModel.bipedHeadwear.showModel = path == 0
		armorModel.bipedBody.showModel = path == 1 || path == 2
		armorModel.bipedRightArm.showModel = path == 1
		armorModel.bipedLeftArm.showModel = path == 1
		armorModel.bipedRightLeg.showModel = path == 2 || path == 3
		armorModel.bipedLeftLeg.showModel = path == 2 || path == 3
		armorModel = ForgeHooksClient.getArmorModel(entity, stack, path, armorModel)
		setRenderPassModel(armorModel)
		armorModel.onGround = mainModel.onGround
		armorModel.isRiding = mainModel.isRiding
		armorModel.isChild = mainModel.isChild
		
		val color = item.getColor(stack)
		
		if (color != -1) {
			val r = (color shr 16 and 255).F / 255f
			val g = (color shr 8 and 255).F / 255f
			val b = (color and 255).F / 255f
			glColor3f(r, g, b)
			return if (stack.isItemEnchanted) 31 else 16
		}
		
		glColor3f(1f, 1f, 1f)
		return if (stack.isItemEnchanted) 15 else 1
	}
	
	// bindArmorTexture
	override fun func_82408_c(entity: EntityLivingBase, path: Int, ticks: Float) {
		val stack = entity.getEquipmentInSlot(3 - path + 1) ?: return
		bindTexture(getArmorResource(entity, stack, path, "overlay"))
		glColor3f(1f, 1f, 1f)
	}
	
	override fun doRender(entity: Entity, x: Double, y: Double, z: Double, yaw: Float, ticks: Float) =
		this.doRender(entity as EntityLivingBase, x, y, z, yaw, ticks)
	
	override fun doRender(entity: EntityLivingBase, x: Double, y: Double, z: Double, yaw: Float, ticks: Float) =
		this.doRender(entity as EntityLiving, x, y, z, yaw, ticks)
	
	override fun doRender(entity: EntityLiving, x: Double, y: Double, z: Double, yaw: Float, ticks: Float) {
		glColor3f(1f, 1f, 1f)
		
		mainModelNew.heldItemRight = if (entity.heldItem != null) 1 else 0
		armorModelPants.heldItemRight = mainModelNew.heldItemRight
		armorModel.heldItemRight = mainModelNew.heldItemRight
		
		armorModelPants.isSneak = entity.isSneaking
		armorModel.isSneak = entity.isSneaking
		
		var yOffset = y - entity.yOffset.D
		if (entity.isSneaking) yOffset -= 0.125
		
		super.doRender(entity, x, yOffset, z, yaw, ticks)
		
		mainModelNew.aimedBow = false
		armorModelPants.aimedBow = false
		armorModel.aimedBow = false
		
		armorModelPants.isSneak = false
		armorModel.isSneak = false
		
		mainModelNew.heldItemRight = 0
		armorModelPants.heldItemRight = 0
		armorModel.heldItemRight = 0
	}
	
	override fun getEntityTexture(entity: Entity) = this.getEntityTexture(entity as EntityLiving)
	
	abstract fun getEntityTexture(entity: EntityLiving): ResourceLocation?
	
	override fun renderEquippedItems(entity: EntityLivingBase, ticks: Float) {
		glColor3f(1f, 1f, 1f)
		super.renderEquippedItems(entity, ticks)
		
		val stack = entity.heldItem
		val helmet = entity.getEquipmentInSlot(4)
		
		if (helmet?.item != null) {
			glPushMatrix()
			mainModelNew.head.postRender(0.0625f)
			val item = helmet.item
			
			val customRenderer = MinecraftForgeClient.getItemRenderer(helmet, IItemRenderer.ItemRenderType.EQUIPPED)
			val is3D = customRenderer != null && customRenderer.shouldUseRenderHelper(IItemRenderer.ItemRenderType.EQUIPPED, helmet, IItemRenderer.ItemRendererHelper.BLOCK_3D)
			
			if (item is ItemBlock) {
				if (is3D || RenderBlocks.renderItemIn3d(Block.getBlockFromItem(item).renderType)) {
					glTranslatef(0f, -0.25f, 0f)
					glRotatef(90f, 0f, 1f, 0f)
					val scale = 0.625f
					glScalef(scale, -scale, -scale)
				}
				
				renderManager.itemRenderer.renderItem(entity, helmet, 0)
			} else if (item === Items.skull) {
				val scale = 1.0625f
				glScalef(scale, -scale, -scale)
				
				var gameprofile: GameProfile? = null
				if (helmet.hasTagCompound()) {
					val nbt = helmet.tagCompound
					if (nbt.hasKey("SkullOwner", 10)) {
						gameprofile = NBTUtil.func_152459_a(nbt.getCompoundTag("SkullOwner"))
					} else if (nbt.hasKey("SkullOwner", 8) && !StringUtils.isNullOrEmpty(nbt.getString("SkullOwner"))) {
						gameprofile = GameProfile(null, nbt.getString("SkullOwner"))
					}
				}
				
				TileEntitySkullRenderer.field_147536_b.func_152674_a(-0.5f, 0f, -0.5f, 1, 180f, helmet.getItemDamage(), gameprofile)
			}
			
			glPopMatrix()
		}
		
		if (stack?.item != null) {
			val item = stack.item
			glPushMatrix()
			if (mainModel.isChild) {
				glTranslatef(0f, 0.625f, 0f)
				glRotatef(-20f, -1f, 0f, 0f)
				glScalef(0.5f)
			}
			
			mainModelNew.rightarm.postRender(0.0625f)
			glTranslatef(-0.0625f, 0.4375f, 0.0625f)
			
			val customRenderer = MinecraftForgeClient.getItemRenderer(stack, IItemRenderer.ItemRenderType.EQUIPPED)
			val is3D = customRenderer != null && customRenderer.shouldUseRenderHelper(IItemRenderer.ItemRenderType.EQUIPPED, stack, IItemRenderer.ItemRendererHelper.BLOCK_3D)
			
			if (item is ItemBlock && (is3D || RenderBlocks.renderItemIn3d(Block.getBlockFromItem(item).renderType))) {
				glTranslatef(0f, 0.1875f, -0.3125f)
				glRotatef(20f, 1f, 0f, 0f)
				glRotatef(45f, 0f, 1f, 0f)
				val scale = 0.375f
				glScalef(-scale, -scale, scale)
			} else if (item === Items.bow) {
				glTranslatef(0f, 0.125f, 0.3125f)
				glRotatef(-20f, 0f, 1f, 0f)
				val scale = 0.625f
				glScalef(scale, -scale, scale)
				glRotatef(-100f, 1f, 0f, 0f)
				glRotatef(45f, 0f, 1f, 0f)
			} else if (item.isFull3D) {
				if (item.shouldRotateAroundWhenRendering()) {
					glRotatef(180f, 0f, 0f, 1f)
					glTranslatef(0f, -0.125f, 0f)
				}
				glTranslatef(0f, 0.1875f, 0f)
				val scale = 0.625f
				glScalef(scale, -scale, scale)
				glRotatef(-100f, 1f, 0f, 0f)
				glRotatef(45f, 0f, 1f, 0f)
			} else {
				glTranslatef(0.25f, 0.1875f, -0.1875f)
				val scale = 0.375f
				glScalef(scale, scale, scale)
				glRotatef(60f, 0f, 0f, 1f)
				glRotatef(-90f, 1f, 0f, 0f)
				glRotatef(20f, 0f, 0f, 1f)
			}
			
			if (stack.item.requiresMultipleRenderPasses()) {
				var path = 0
				while (path < stack.item.getRenderPasses(stack.getItemDamage())) {
					val color = stack.item.getColorFromItemStack(stack, path)
					val r = (color shr 16 and 255).F / 255f
					val g = (color shr 8 and 255).F / 255f
					val b = (color and 255).F / 255f
					glColor4f(r, g, b, 1f)
					renderManager.itemRenderer.renderItem(entity, stack, path)
					++path
				}
			} else {
				val color = stack.item.getColorFromItemStack(stack, 0)
				val r = (color shr 16 and 255).F / 255f
				val g = (color shr 8 and 255).F / 255f
				val b = (color and 255).F / 255f
				glColor4f(r, g, b, 1f)
				renderManager.itemRenderer.renderItem(entity, stack, 0)
			}
			
			glPopMatrix()
		}
	}
	
	companion object {
		
		private val textureCache = Maps.newHashMap<String, ResourceLocation>()
		
		var bipedArmorFilenamePrefix = arrayOf("leather", "chainmail", "iron", "diamond", "gold")
		
		/**
		 * More generic ForgeHook version of the above function, it allows for Items to have more control over what texture they provide.
		 *
		 * @param entity Entity wearing the armor
		 * @param stack ItemStack for the armor
		 * @param slot Slot ID that the item is in
		 * @param type Subtype, can be null or "overlay"
		 * @return ResourceLocation pointing at the armor's texture
		 */
		fun getArmorResource(entity: Entity?, stack: ItemStack, slot: Int, type: String?): ResourceLocation {
			val item = stack.item as ItemArmor
			var texture = "textures/models/armor/${bipedArmorFilenamePrefix[item.renderIndex]}_layer_${if (slot == 2) 2 else 1}${if (type == null) "" else "_$type"}.png"
			texture = ForgeHooksClient.getArmorTexture(entity, stack, texture, slot, type)
			return textureCache.computeIfAbsent(texture) { ResourceLocation(texture) }
		}
	}
}