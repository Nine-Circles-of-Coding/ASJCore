package alexsocol.patcher.asm.hook;

import alexsocol.patcher.compat.AngelicaCompat;
import alexsocol.patcher.handler.PlayerReachDistanceHandler;
import com.KAIIIAK.classManipulators.HookReplacer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelCreeper;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.resources.FileResourcePack;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.ComponentScatteredFeaturePieces;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.glu.Project;

import java.io.File;
import java.util.Random;
import java.util.Set;

import static com.KAIIIAK.classManipulators.HookReplacer.Replacer.*;

@SuppressWarnings({"unused", "ConstantValue", "UnusedAssignment", "JavaExistingMethodCanBeUsed", "DataFlowIssue"})
//@formatter:off
public class ASJHookReplacerHandler {
	
	// reach distance
	@SideOnly(Side.CLIENT)
	@HookReplacer
	public static void getMouseOver(EntityRenderer er, float partialTick) {
		if (er.mc.renderViewEntity != null) {
			if (er.mc.theWorld != null) {
				double d0 = 0;
				double d1 = 0;
				Vec3 vec3 = null;
				
				startFROM();
				POPLine();if (er.mc.playerController.extendedReach()) {
					d0 = 6.0D;
					d1 = 6.0D;
				} else {
					if (d0 > 3.0D) {
						d1 = 3.0D;
					}
					
					d0 = d1;
				}
				startTO();stop();
			}
		}
	}
	
	@HookReplacer
	public static void processUseEntity(NetHandlerPlayServer nhps, C02PacketUseEntity packet) {
		WorldServer worldserver = null;
		Entity entity = null;
		
		if (entity != null) {
			boolean flag = false;
			double d0 = 0;
			
			startFROM();
			POPLine();if (!flag) {
				d0 = 9.0D;
			}
			startTO();
			d0 = Math.pow(PlayerReachDistanceHandler.getReachDistance(nhps.playerEntity), 2);
			stop();
		}
	}
	
	@HookReplacer // fucking Crucible bitches overwriting my changes so post-transforming -_-
	public static double getBlockReachDistance(ItemInWorldManager iiwm) {
		startFROM();
		POP(iiwm.blockReachDistance);
		startTO();
		POP(PlayerReachDistanceHandler.getReachDistance(iiwm.thisPlayerMP));
		stop();
		
		return 0;
	}
	
	@HookReplacer // fucking Crucible bitches overwriting my changes so post-transforming -_-
	public static void setBlockReachDistance(ItemInWorldManager iiwm, double distance) {
		startFROM();
		iiwm.blockReachDistance = distance;
		startTO();
		PlayerReachDistanceHandler.setReachDistance(iiwm.thisPlayerMP, distance);
		stop();
	}
	
	
	// Perspective vs Ortho proj config. Keeping old line for other mods like RiftFlux to mixin/hook into
	@HookReplacer(isMandatory = false)
	public static void setupCameraTransform(EntityRenderer er, float f, int i) {
		startFROM();
		Project.gluPerspective(er.getFOVModifier(f, true), (float)er.mc.displayWidth / (float)er.mc.displayHeight, 0.05F, er.farPlaneDistance * 2F);
		startTO();
		Project.gluPerspective(er.getFOVModifier(f, true), (float)er.mc.displayWidth / (float)er.mc.displayHeight, 0.05F, er.farPlaneDistance * 2F);
		AngelicaCompat.switchToOrtho(er, f, er.farPlaneDistance * 2F);
		stop();
	}
	
	@HookReplacer(targetMethod = "setupCameraTransform", isMandatory = false)
	public static void setupCameraTransformOF(EntityRenderer er, float f, int i) {
		startFROM();
		Project.gluPerspective(er.getFOVModifier(f, true), (float)er.mc.displayWidth / (float)er.mc.displayHeight, 0.05F, FLOAD("4"));
		startTO();
		Project.gluPerspective(er.getFOVModifier(f, true), (float)er.mc.displayWidth / (float)er.mc.displayHeight, 0.05F, FLOAD("4"));
		AngelicaCompat.switchToOrtho(er, f, FLOAD("4"));
		stop();
	}
	
	// bind smooth camera key
	@HookReplacer(targetMethod = "<init>")
	public static void GameSettings(GameSettings thiz) {
		startFROM();
		POP("key.smoothCamera");POP(0);
		startTO();
		POP("key.smoothCamera");POP(Keyboard.KEY_F8);
		stop();
	}
	
	@HookReplacer(targetMethod = "<init>")
	public static void GameSettings(GameSettings thiz, Minecraft mc, File options) {
		startFROM();
		POP("key.smoothCamera");POP(0);
		startTO();
		POP("key.smoothCamera");POP(Keyboard.KEY_F8);
		stop();
	}
	
	@HookReplacer(targetMethod = "<init>")
	public static void ModelCreeper(ModelCreeper thiz, float size) {
		startFROM();
		POP(4);ISTORE("2");
		startTO();
		POP(6);ISTORE("2");
		stop();
	}
	
	@HookReplacer(removePop = true)
	public static Set<String> getResourceDomains(FileResourcePack frp) {
		startFROM();
		HookReplacer.Replacer.<String>ALOAD("7").toLowerCase();
		startTO();
		ALOAD("7").toString();
		stop();
		
		return null;
	}
	
	@HookReplacer(removePop = true)
	public static Set<String> getResourceDomains(FolderResourcePack frp) {
		startFROM();
		HookReplacer.Replacer.<String>ALOAD("7").toLowerCase();
		startTO();
		ALOAD("7").toString();
		stop();
		
		return null;
	}
	
	
	// String -> NBTTagByteArray fix
	@HookReplacer(targetMethod = "func_150489_a", removePop = true)
	public static NBTBase fixSingleElement(JsonToNBT.Primitive primitive) {
		startFROM();
		new NBTTagIntArray(new int[] {Integer.parseInt(HookReplacer.Replacer.<String>ALOAD("1").trim())});
		startTO();
		deserialize(ALOAD("2"));
		stop();

		return null;
	}

	public static NBTBase deserialize(String[] elements) {
		if (elements.length == 1) {
			String element = elements[0];
			if (element.endsWith("B") || element.endsWith("b")) {
				return new NBTTagByteArray(new byte[]{Byte.parseByte(element.substring(0, element.length() - 1))});
			} else {
				return new NBTTagIntArray(new int[]{Integer.parseInt(element)});
			}
		} else {
			if (elements[0].endsWith("B") || elements[0].endsWith("b")) {
				return new NBTTagByteArray(ASJHookReplacerHandlerKt.javaStreamsAreShitSB(elements));
			} else {
				return new NBTTagIntArray(ASJHookReplacerHandlerKt.javaStreamsAreShitSI(elements));
			}
		}
	}

	@HookReplacer(targetMethod = "func_150489_a")
	public static NBTBase blockExtraCode(JsonToNBT.Primitive primitive) {
		startFROM();
		POP(HookReplacer.Replacer.<String[]>ALOAD("2").length);
		startTO();
		POP(0);
		stop();
		
		return null;
	}
	
	@HookReplacer
	public static boolean addComponentParts(ComponentScatteredFeaturePieces.SwampHut hut, World world, Random rand, StructureBoundingBox box) {
		startFROM();
		world.spawnEntityInWorld(ALOAD("11"));
		startTO();
		fixWitchDespawn(world, ALOAD("11"));
		stop();
		
		return false;
	}
	
	public static void fixWitchDespawn(World world, EntityWitch witch) {
		witch.func_110163_bv();
		world.spawnEntityInWorld(witch);
	}
}
//@formatter:on