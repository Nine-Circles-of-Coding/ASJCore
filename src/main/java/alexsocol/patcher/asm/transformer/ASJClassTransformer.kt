@file:Suppress("ClassName", "unused")

package alexsocol.patcher.asm.transformer

import alexsocol.patcher.PatcherConfigHandler
import alexsocol.patcher.asm.ASJHookLoader.Companion.OBF
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.*

class ASJClassTransformer: ASJAbstractClassTransformer() {
	
	override fun transform(transformedName: String, basicClass: ByteArray): ByteArray {
		if (transformedName != "alexsocol.patcher.asm.ASJClassTransformer\$ClassVisitorPotionMethodPublicizer") try {
			val cr = ClassReader(this.basicClass)
			val cw = ClassWriter(ClassWriter.COMPUTE_MAXS)
			val cv = ClassVisitorPotionMethodPublicizer(cw, transformedName)
			cr.accept(cv, ClassReader.EXPAND_FRAMES)
			this.basicClass = cw.toByteArray()
		} catch (e: Throwable) {
			logger.error("Something went wrong while transforming class $transformedName. Ignore if everything is OK (this is NOT ASJCore error):", e)
		}
		
		return when (transformedName) {
			"codechicken.nei.api.ItemInfo"                                    -> core { `ItemInfo$ClassVisitor`(it) }
			"cpw.mods.fml.common.network.handshake.FMLHandshakeClientState$4" -> expandReport()
			"io.netty.channel.DefaultChannelPipeline"                         -> core { `DefaultChannelPipeline$ClassVisitor`(it) }
			"net.minecraft.client.network.NetHandlerPlayClient"               -> core { `NetHandlerPlayClient$ClassVisitor`(it) }
			"net.minecraft.client.particle.EffectRenderer"                    -> core { `EffectRenderer$ClassVisitor`(it) }
			"net.minecraft.command.server.CommandSummon"                      -> core { `CommandSummon$ClassVisitor`(it) }
			"net.minecraft.entity.Entity"                                     -> {
				if (PatcherConfigHandler.fixItemCollision)
					this.basicClass = fixEntityCollision()
				
				core { `Entity$ClassVisitor`(it) }
			}
			"net.minecraft.item.ItemGlassBottle"                              -> core { `ItemGlassBottle$ClassVisitor`(it) }
			"net.minecraft.nbt.JsonToNBT"                                     -> core { `JsonToNBT$ClassVisitor`(it) }
			"net.minecraft.client.network.OldServerPinger$2",
			"net.minecraft.network.NetworkManager$2",
			"net.minecraft.network.NetworkSystem$1"                           -> core { `Network_$_$ClassVisitor`(it) }
			"net.minecraft.network.play.client.C17PacketCustomPayload"        -> core { `C17PacketCustomPayload$ClassVisitor`(it) }
			"net.minecraft.server.management.ItemInWorldManager"              -> core { `ItemInWorldManager$ClassVisitor`(it) }
			"net.minecraft.tileentity.TileEntityFurnace"                      -> core { `TileEntityFurnace$ClassVisitor`(it) }
			"net.minecraft.world.World"                                       -> core { `World$ClassVisitor`(it) }
			"thaumcraft.common.blocks.BlockCustomOre"                         -> core { `BlockCustomOre$ClassVisitor`(it) }
			"thaumcraft.common.tiles.TileInfusionMatrix"                      -> fixInfusionMatrix()
			else                                                              -> this.basicClass
		}
	}
	
	private inner class ClassVisitorPotionMethodPublicizer(cv: ClassVisitor, val className: String): ClassVisitor(ASM5, cv) {
		
		override fun visitMethod(acc: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?): MethodVisitor {
			var access = acc
			if (name == (if (OBF) "b" else "onFinishedPotionEffect") && desc == (if (OBF) "(Lrw;)V" else "(Lnet/minecraft/potion/PotionEffect;)V")) {
				logger.debug("Publicizing onFinishedPotionEffect: $name$desc for $className")
				access = ACC_PUBLIC
			}
			if (name == (if (OBF) "a" else "onChangedPotionEffect") && desc == (if (OBF) "(Lrw;Z)V" else "(Lnet/minecraft/potion/PotionEffect;Z)V")) {
				logger.debug("Publicizing onChangedPotionEffect: $name$desc for $className")
				access = ACC_PUBLIC
			}
			return super.visitMethod(access, name, desc, signature, exceptions)
		}
	}
	
	// fixes crash when adding eggs
	// also calls ASJPatches#patchNeiNoWither
	private inner class `ItemInfo$ClassVisitor`(cv: ClassVisitor): ClassVisitor(ASM5, cv) {
		
		override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?): MethodVisitor {
			if (name == "load") {
				logger.debug("Visiting ItemInfo#load: $name$desc")
				return `ItemInfo$load$MethodVisitor`(super.visitMethod(access, name, desc, signature, exceptions))
			}
			return super.visitMethod(access, name, desc, signature, exceptions)
		}
		
		private inner class `ItemInfo$load$MethodVisitor`(mv: MethodVisitor): MethodVisitor(ASM5, mv) {
			
			override fun visitMethodInsn(opcode: Int, owner: String, name: String, desc: String?, itf: Boolean) {
				if (opcode == INVOKESTATIC && owner == "codechicken/nei/api/ItemInfo" && name == "addSpawnEggs")
					return super.visitMethodInsn(opcode, "alexsocol/patcher/asm/ASJPatches", "patchNeiNoWither", desc, itf)
				
				super.visitMethodInsn(opcode, owner, name, desc, itf)
			}
		}
	}
	
	private inner class `DefaultChannelPipeline$ClassVisitor`(cv: ClassVisitor): ClassVisitor(ASM5, cv) {
		
		override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?): MethodVisitor {
			val mv = super.visitMethod(access, name, desc, signature, exceptions)
			
			if (name == "checkDuplicateName") {
				mv.visitCode()
				val l0 = Label()
				mv.visitLabel(l0)
				mv.visitInsn(RETURN)
				val l1 = Label()
				mv.visitLabel(l1)
				mv.visitLocalVariable("this", "Lio/netty/channel/DefaultChannelPipeline;", null, l0, l1, 0)
				mv.visitLocalVariable("name", "Ljava/lang/String;", null, l0, l1, 1)
				mv.visitMaxs(0, 2)
				mv.visitEnd()
			}
			
			return mv
		}
	}
	
	// wrong RangedAttribute#minimumValue fix
	private inner class `NetHandlerPlayClient$ClassVisitor`(cv: ClassVisitor): ClassVisitor(ASM5, cv) {
		
		override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?): MethodVisitor {
			if (name == "handleEntityProperties" || (name == "a" && desc == "(Lil;)V")) {
				logger.debug("Visiting NetHandlerPlayClient#handleEntityProperties: $name$desc")
				return `NetHandlerPlayClient$handleEntityProperties$MethodVisitor`(super.visitMethod(access, name, desc, signature, exceptions))
			}
			
			return super.visitMethod(access, name, desc, signature, exceptions)
		}
		
		private inner class `NetHandlerPlayClient$handleEntityProperties$MethodVisitor`(mv: MethodVisitor): MethodVisitor(ASM5, mv) {
			
			override fun visitLdcInsn(cst: Any?) {
				if (cst == java.lang.Double.MIN_NORMAL) super.visitLdcInsn(-java.lang.Double.MAX_VALUE)
				else super.visitLdcInsn(cst)
			}
		}
	}
	
	// More Particles
	private inner class `EffectRenderer$ClassVisitor`(cv: ClassVisitor): ClassVisitor(ASM5, cv) {
		
		override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?): MethodVisitor {
			if (name == "addEffect" || name == "a" && desc == "(Lbkm;)V") {
				logger.debug("Visiting EffectRenderer#addEffect: $name$desc")
				return `EffectRenderer$addEffect$MethodVisitor`(super.visitMethod(access, name, desc, signature, exceptions))
			}
			
			return super.visitMethod(access, name, desc, signature, exceptions)
		}
		
		private inner class `EffectRenderer$addEffect$MethodVisitor`(mv: MethodVisitor): MethodVisitor(ASM5, mv) {
			
			override fun visitIntInsn(opcode: Int, operand: Int) {
				if (opcode == SIPUSH && operand == 4000) {
					when (PatcherConfigHandler.maxParticles) {
						in Byte.MIN_VALUE..Byte.MAX_VALUE   -> super.visitIntInsn(BIPUSH, PatcherConfigHandler.maxParticles)
						in Short.MIN_VALUE..Short.MAX_VALUE -> super.visitIntInsn(SIPUSH, PatcherConfigHandler.maxParticles)
						else                                -> super.visitLdcInsn(Integer(PatcherConfigHandler.maxParticles))
					}
				} else super.visitIntInsn(opcode, operand)
			}
		}
	}
	
	// Summom Usage
	private inner class `CommandSummon$ClassVisitor`(cv: ClassVisitor): ClassVisitor(ASM5, cv) {
		
		override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?): MethodVisitor {
			logger.debug("Visiting CommandSummon#: $name$desc")
			return `CommandSummon$processCommand$MethodVisitor`(super.visitMethod(access, name, desc, signature, exceptions))
		}
		
		private inner class `CommandSummon$processCommand$MethodVisitor`(mv: MethodVisitor): MethodVisitor(ASM5, mv) {
			
			override fun visitLdcInsn(cst: Any?) {
				val ncst = if ("commands.summon.usage" == cst) "commands.summon.usage.new" else cst
				super.visitLdcInsn(ncst)
			}
		}
	}
	
	// flag count expansion to 32
	private inner class `Entity$ClassVisitor`(cv: ClassVisitor): ClassVisitor(ASM5, cv) {
		
		override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?): MethodVisitor {
			val visitMethod = super.visitMethod(access, name, desc, signature, exceptions)
			
			if (name == "<init>") {
				logger.debug("Visiting Entity#init: $name$desc")
				return `Entity$init$MethodVisitor`(visitMethod)
			} else if (name == "getFlag" || name == "setFlag" || (name == "g" && desc == "(I)Z") || (name == "a" && desc == "(IZ)V")) {
				logger.debug("Visiting Entity#flag property: $name$desc")
				return `Entity$init$MethodVisitor`(visitMethod)
			}
			
			return visitMethod
		}
		
		private inner class `Entity$init$MethodVisitor`(mv: MethodVisitor): MethodVisitor(ASM5, mv) {
			
			override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, itf: Boolean) {
				if ((owner == "net/minecraft/entity/DataWatcher" || owner == "te") && desc == "(I)B")
					super.visitMethodInsn(opcode, owner, if (OBF) "c" else "getWatchableObjectInt", "(I)I", itf)
				else if (owner == "java/lang/Byte" && desc == "(B)Ljava/lang/Byte;")
					super.visitMethodInsn(opcode, "java/lang/Integer", name, "(I)Ljava/lang/Integer;", itf)
				else
					super.visitMethodInsn(opcode, owner, name, desc, itf)
			}
			
			override fun visitInsn(opcode: Int) {
				if (opcode != I2B)
					super.visitInsn(opcode)
			}
		}
	}
	
	private inner class `ItemGlassBottle$ClassVisitor`(cv: ClassVisitor): ClassVisitor(ASM5, cv) {
		
		override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?): MethodVisitor {
			if (name == "onItemRightClick" || name == "a" && desc == "(Ladd;Lahb;Lyz;)Ladd;") {
				logger.debug("Visiting ItemGlassBottle#onItemRightClick: $name$desc")
				return `ItemGlassBottle$onItemRightClick$MethodVisitor`(super.visitMethod(access, name, desc, signature, exceptions))
			}
			
			return super.visitMethod(access, name, desc, signature, exceptions)
		}
		
		private inner class `ItemGlassBottle$onItemRightClick$MethodVisitor`(mv: MethodVisitor): MethodVisitor(ASM5, mv) {
			
			override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, itf: Boolean) {
				if (name == "getMaterial" || name == "o" && desc == "()Lawt;") return
				
				super.visitMethodInsn(opcode, owner, name, desc, itf)
			}
			
			override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, desc: String?) {
				if (name == "water" || name == "h")
					super.visitFieldInsn(GETSTATIC, if (OBF) "ajn" else "net/minecraft/init/Blocks", if (OBF) "j" else "water", if (OBF) "Laji;" else "Lnet/minecraft/block/Block;")
				else
					super.visitFieldInsn(opcode, owner, name, desc)
			}
		}
	}
	
	private inner class `JsonToNBT$ClassVisitor`(cv: ClassVisitor): ClassVisitor(ASM5, cv) {
		
		override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?): MethodVisitor {
			if (name == "func_150316_a" || (name == "a" && desc == "(Ljava/lang/String;Ljava/lang/String;)Lec;")) {
				logger.debug("Visiting JsonToNBT#func_150316_a: $name$desc")
				return `JsonToNBT$func_150316_a$MethodVisitor`(super.visitMethod(access, name, desc, signature, exceptions))
			}
			
			return super.visitMethod(access, name, desc, signature, exceptions)
		}
		
		private inner class `JsonToNBT$func_150316_a$MethodVisitor`(mv: MethodVisitor): MethodVisitor(ASM5, mv) {
			
			override fun visitLdcInsn(cst: Any?) {
				super.visitLdcInsn(if (cst == "\\[[-\\d|,\\s]+\\]") "\\[[-\\db|,\\s]+]" else cst)
			}
		}
	}
	
	// TCP no delay (other part in PatcherEventHandler)
	private inner class `Network_$_$ClassVisitor`(cv: ClassVisitor): ClassVisitor(ASM5, cv) {
		
		override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?): MethodVisitor {
			if (PatcherConfigHandler.tcpNoDelay && name == "initChannel") {
				logger.debug("Visiting $transformedName#initChannel: $name$desc")
				return `Network_$_$initChannel$MethodVisitor`(super.visitMethod(access, name, desc, signature, exceptions))
			}
			
			return super.visitMethod(access, name, desc, signature, exceptions)
		}
		
		private inner class `Network_$_$initChannel$MethodVisitor`(mv: MethodVisitor): MethodVisitor(ASM5, mv) {
			
			var changed = false
			
			override fun visitInsn(opcode: Int) {
				super.visitInsn(
					if (changed)
						opcode
					else if (opcode == ICONST_0) run {
						changed = true
						ICONST_1
					} else
						opcode
				)
			}
		}
	}
	
	// Payload fix
	private inner class `C17PacketCustomPayload$ClassVisitor`(cv: ClassVisitor): ClassVisitor(ASM5, cv) {
		
		override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?): MethodVisitor {
			if (PatcherConfigHandler.c17PacketCustomPayloadUnlimit)
				if ((name == "<init>" && desc == "(Ljava/lang/String;[B)V") || (name == "readPacketData" || name == "a" && desc == "(Let;)V") || (name == "writePacketData" || name == "b" && desc == "(Let;)V")) {
					logger.debug("Visiting C17PacketCustomPayload methods: $name$desc")
					return `C17PacketCustomPayload$MethodVisitor`(super.visitMethod(access, name, desc, signature, exceptions))
				}
			
			return super.visitMethod(access, name, desc, signature, exceptions)
		}
		
		private inner class `C17PacketCustomPayload$MethodVisitor`(mv: MethodVisitor): MethodVisitor(ASM5, mv) {
			
			override fun visitIntInsn(opcode: Int, operand: Int) {
				if (opcode == SIPUSH && operand == 32767) {
					super.visitLdcInsn(Int.MAX_VALUE)
				} else if (opcode == LDC) {
					super.visitLdcInsn("Sorry hook not worked :(")
				} else super.visitIntInsn(opcode, operand)
			}
			
			override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, itf: Boolean) {
				var newName = name
				var newDesc = desc
				
				if (name == "readShort") {
					newName = "readInt"
					newDesc = "()I"
				} else if (name == "writeShort") {
					newName = "writeInt"
				}
				
				super.visitMethodInsn(opcode, owner, newName, newDesc, itf)
			}
			
			override fun visitInsn(opcode: Int) {
				if (opcode != I2S) super.visitInsn(opcode)
			}
		}
	}
	
	// Non-null fire extinguishing
	private inner class `ItemInWorldManager$ClassVisitor`(cv: ClassVisitor): ClassVisitor(ASM5, cv) {
		
		override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?): MethodVisitor {
			if (name == "onBlockClicked" || name == "a" && desc == "(IIII)V") {
				logger.debug("Visiting ItemInWorldManager#onBlockClicked: $name$desc")
				return `ItemRelic$onBlockClicked$MethodVisitor`(super.visitMethod(access, name, desc, signature, exceptions))
			}
			
			return super.visitMethod(access, name, desc, signature, exceptions)
		}
		
		private inner class `ItemRelic$onBlockClicked$MethodVisitor`(mv: MethodVisitor): MethodVisitor(ASM5, mv) {
			
			override fun visitInsn(opcode: Int) {
				if (opcode == ACONST_NULL) {
					visitVarInsn(ALOAD, 0)
					visitFieldInsn(GETFIELD, if (OBF) "mx" else "net/minecraft/server/management/ItemInWorldManager", if (OBF) "b" else "thisPlayerMP", if (OBF) "Lmw;" else "Lnet/minecraft/entity/player/EntityPlayerMP;")
				} else {
					super.visitInsn(opcode)
				}
			}
			
			override fun visitTypeInsn(opcode: Int, type: String) {
				if (opcode != CHECKCAST || type != (if (OBF) "yz" else "net/minecraft/entity/player/EntityPlayer")) {
					super.visitTypeInsn(opcode, type)
				}
			}
		}
	}
	
	// saving more than Short.MAX_VALUE amount of fuel
	private inner class `TileEntityFurnace$ClassVisitor`(cv: ClassVisitor): ClassVisitor(ASM5, cv) {
		
		override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?): MethodVisitor {
			if (name == "readFromNBT" || (name == "a" && desc == "(Ldh;)V")) {
				logger.debug("Visiting TileEntityFurnace#readFromNBT: $name$desc")
				return `TileEntityFurnace$readFromNBT$MethodVisitor`(super.visitMethod(access, name, desc, signature, exceptions))
			} else if (name == "writeToNBT" || (name == "b" && desc == "(Ldh;)V")) {
				logger.debug("Visiting TileEntityFurnace#writeToNBT: $name$desc")
				return `TileEntityFurnace$writeToNBT$MethodVisitor`(super.visitMethod(access, name, desc, signature, exceptions))
			}
			
			return super.visitMethod(access, name, desc, signature, exceptions)
		}
		
		private inner class `TileEntityFurnace$readFromNBT$MethodVisitor`(mv: MethodVisitor): MethodVisitor(ASM5, mv) {
			
			override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, itf: Boolean) {
				if (name == "getShort" || (name == "e" && desc == "(Ljava/lang/String;)S")) {
					super.visitMethodInsn(opcode, owner, if (OBF) "f" else "getInteger", "(Ljava/lang/String;)I", itf)
				} else super.visitMethodInsn(opcode, owner, name, desc, itf)
			}
		}
		
		private inner class `TileEntityFurnace$writeToNBT$MethodVisitor`(mv: MethodVisitor): MethodVisitor(ASM5, mv) {
			
			override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, itf: Boolean) {
				if (name == "setShort" || (name == "a" && desc == "(Ljava/lang/String;S)V")) {
					super.visitMethodInsn(opcode, owner, if (OBF) "a" else "setInteger", "(Ljava/lang/String;I)V", itf)
				} else super.visitMethodInsn(opcode, owner, name, desc, itf)
			}
			
			override fun visitInsn(opcode: Int) {
				if (opcode != I2S) super.visitInsn(opcode)
			}
		}
	}
	
	// Firing any entity update event
	private inner class `World$ClassVisitor`(cv: ClassVisitor): ClassVisitor(ASM5, cv) {
		
		override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?): MethodVisitor {
			if (name == "updateEntityWithOptionalForce" || name == "a" && desc == "(Lsa;Z)V") {
				logger.debug("Visiting World#updateEntityWithOptionalForce: $name$desc")
				return `World$updateEntityWithOptionalForce$MethodVisitor`(super.visitMethod(access, name, desc, signature, exceptions))
			}
			
			return super.visitMethod(access, name, desc, signature, exceptions)
		}
		
		private inner class `World$updateEntityWithOptionalForce$MethodVisitor`(mv: MethodVisitor): MethodVisitor(ASM5, mv) {
			
			override fun visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) {
				if (opcode == INVOKEVIRTUAL && owner == (if (OBF) "sa" else "net/minecraft/entity/Entity") && (name == (if (OBF) "ab" else "updateRidden") || name == if (OBF) "h" else "onUpdate") && desc == "()V" && !itf) {
					mv.visitMethodInsn(INVOKESTATIC, "alexsocol/patcher/event/EntityUpdateEvent", "instantiate", if (OBF) "(Lsa;)Lalexsocol/patcher/event/EntityUpdateEvent;" else "(Lnet/minecraft/entity/Entity;)Lalexsocol/patcher/event/EntityUpdateEvent;", false)
					mv.visitVarInsn(ASTORE, 8)
					mv.visitFieldInsn(GETSTATIC, "net/minecraftforge/common/MinecraftForge", "EVENT_BUS", "Lcpw/mods/fml/common/eventhandler/EventBus;")
					mv.visitVarInsn(ALOAD, 8)
					mv.visitMethodInsn(INVOKEVIRTUAL, "cpw/mods/fml/common/eventhandler/EventBus", "post", "(Lcpw/mods/fml/common/eventhandler/Event;)Z", false)
					val label = Label()
					mv.visitJumpInsn(IFNE, label)
					mv.visitVarInsn(ALOAD, 1)
					mv.visitMethodInsn(INVOKEVIRTUAL, if (OBF) "sa" else "net/minecraft/entity/Entity", name, "()V", false)
					mv.visitLabel(label)
					mv.visitFrame(F_APPEND, 1, arrayOf<Any>("alexsocol/patcher/event/EntityUpdateEvent"), 0, null)
					mv.visitMethodInsn(INVOKESTATIC, "alexsocol/patcher/event/EntityUpdateEvent", "stub", "()V", false)
				} else super.visitMethodInsn(opcode, owner, name, desc, itf)
			}
		}
	}
	
	// Fix for entropy ore being without black hit particles
	private inner class `BlockCustomOre$ClassVisitor`(cv: ClassVisitor): ClassVisitor(ASM5, cv) {
		
		override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<String>?): MethodVisitor {
			if (name == "addHitEffects") {
				logger.debug("Visiting BlockCustomOre#addHitEffects: $name$desc")
				return `BlockCustomOre$addHitEffects$MethodVisitor`(super.visitMethod(access, name, desc, signature, exceptions))
			}
			
			return super.visitMethod(access, name, desc, signature, exceptions)
		}
		
		private inner class `BlockCustomOre$addHitEffects$MethodVisitor`(mv: MethodVisitor): MethodVisitor(ASM5, mv) {
			
			override fun visitIntInsn(opcode: Int, operand: Int) {
				if (opcode == BIPUSH && operand == 6) super.visitIntInsn(opcode, 7)
				else super.visitIntInsn(opcode, operand)
			}
		}
	}
	
	private fun fixInfusionMatrix() = tree { cn ->
		val mn = cn.methods.find { it.name == "getSurroundings" } ?: return@tree
		
		val i = object: Iterable<AbstractInsnNode> {
			override fun iterator() = mn.instructions.iterator()
		}
		
		mn.instructions.set(i.find { it is LdcInsnNode && it.cst == 0.1f }, LdcInsnNode(1f))
		mn.instructions.set(i.find { it is LdcInsnNode && it.cst == 0.2f }, LdcInsnNode(2f))
		
		val loc = i.find { it is InsnNode && it.opcode == I2F }?.next
		mn.instructions.insert(loc, LdcInsnNode(10f))
		mn.instructions.insert(loc?.next, InsnNode(FDIV))
	}
	
	private fun expandReport() = tree { cn ->
		val mn = cn.methods.find { it.name == "accept" } ?: return@tree
		
		val i = object: Iterable<AbstractInsnNode> {
			override fun iterator() = mn.instructions.iterator()
		}
		
		val ldc = i.find { it is LdcInsnNode && it.cst == "Fatally missing blocks and items" } ?: return@tree
		val invoke = MethodInsnNode(INVOKESTATIC, "alexsocol/patcher/asm/hook/ASJHookReplacerHandlerKt", "printMissingData", "(Ljava/util/List;)Ljava/lang/String;", false)
		mn.instructions.insert(ldc, invoke)
		
		val aload = VarInsnNode(ALOAD, 4)
		mn.instructions.set(ldc, aload)
		
		val log = i.find { it is MethodInsnNode && it.name == "fine" } as? MethodInsnNode ?: return@tree
		log.name = "severe"
	}
	
	// FUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUCK YOU
	private fun fixEntityCollision() = tree(0, 0) { cn ->
		val mn = cn.methods.find { it.name == "func_145771_j" || (it.name == "j" && it.desc == "(DDD)Z") } ?: return@tree
		
		val i = object: Iterable<AbstractInsnNode> {
			override fun iterator() = mn.instructions.iterator()
		}
		
		run {
			val aload = i.find { it is VarInsnNode && it.opcode == ALOAD && it.`var` == 16 && it.next?.let { next -> next is MethodInsnNode && next.name == "isEmpty" } == true } ?: return@run
			mn.instructions.insertBefore(aload, VarInsnNode(ALOAD, 0))
			mn.instructions.set(aload.next, MethodInsnNode(
				INVOKESTATIC,
				"alexsocol/patcher/asm/hook/ItemCollisionFix",
				"checkCollisions",
				"(L${if (OBF) "sa" else "net/minecraft/entity/Entity"};Ljava/util/List;)Z",
				false
			))
		}
		
		run {
			val aload = i.find {
				it is VarInsnNode && it.opcode == ALOAD && it.`var` == 0 && it.next?.let { next ->
					next is FieldInsnNode && next.name == (if (OBF) "o" else "worldObj") && next.next?.let { nnext ->
						nnext is VarInsnNode && nnext.opcode == ILOAD && nnext.`var` == 7 && nnext.next?.let { nnnext ->
							nnnext is VarInsnNode && nnnext.opcode == ILOAD && nnnext.`var` == 8 && nnnext.next?.let { nnnnext ->
								nnnnext is VarInsnNode && nnnnext.opcode == ILOAD && nnnnext.`var` == 9 && nnnnext.next?.let { nnnnnext ->
									nnnnnext is MethodInsnNode && nnnnnext.name == (if (OBF) "q" else "func_147469_q")
								} == true
							} == true
						} == true
					} == true
				} == true
			} ?: return@run
			
			mn.instructions.remove(aload.next.next.next.next.next)
			mn.instructions.remove(aload.next.next.next.next)
			mn.instructions.remove(aload.next.next.next)
			mn.instructions.remove(aload.next.next)
			mn.instructions.remove(aload.next)
			mn.instructions.set(aload, InsnNode(ICONST_0))
		}
	}
	
}