package alexsocol.patcher.asm.hook;

import alexsocol.patcher.compat.AngelicaCompat;
import com.KAIIIAK.classManipulators.HookReplacer;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.discovery.JarDiscoverer;
import cpw.mods.fml.common.discovery.ModCandidate;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.clayium.item.gadget.GadgetLongArm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.achievement.GuiStats;
import net.minecraft.client.model.ModelCreeper;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.resources.FileResourcePack;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.stats.StatCrafting;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.ComponentScatteredFeaturePieces;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.glu.Project;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.KAIIIAK.classManipulators.HookReplacer.Replacer.*;

@SuppressWarnings({"unused", "ConstantValue", "UnusedAssignment", "JavaExistingMethodCanBeUsed", "DataFlowIssue"})
//@formatter:off
public class ASJHookReplacerHandler {
	
	// reach distance
	@HookReplacer.CreateHRG(name = "reachDistance-getMouseOver")
	@SideOnly(Side.CLIENT)
	@HookReplacer(mandatoryGroups = { "reachDistance-getMouseOver" })
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

	@SideOnly(Side.CLIENT)
	@HookReplacer(targetMethod = "getMouseOver", mandatoryGroups = { "reachDistance-getMouseOver" })
	public static void getMouseOverClayium(EntityRenderer er, float partialTick) {
		if (er.mc.renderViewEntity != null) {
			if (er.mc.theWorld != null) {
				double d0 = 0;
				double d1 = 0;
				Vec3 vec3 = null;

				startFROM();
				POPLine();if (er.mc.playerController.extendedReach()) {
					d0 = GadgetLongArm.hookBlockReachDistance(6.0F);
					d1 = GadgetLongArm.hookBlockReachDistance(6.0F);
				} else {
					if (d0 > GadgetLongArm.hookBlockReachDistance(3.0F)) {
						d1 = GadgetLongArm.hookBlockReachDistance(3.0F);
					}

					d0 = d1;
				}
				startTO();stop();
			}
		}
	}
	
	
	// Perspective vs Ortho proj config. Keeping old line for other mods like RiftFlux to mixin/hook into
	@HookReplacer.CreateHRG(name = "orthoProjection")
	@HookReplacer(mandatoryGroups = "orthoProjection")
	public static void setupCameraTransform(EntityRenderer er, float f, int i) {
		startFROM();
		Project.gluPerspective(FSKIP(), FSKIP(), FSKIP(), er.farPlaneDistance * 2F);
		startTO();
		Project.gluPerspective(FSKIP(), FSKIP(), FSKIP(), er.farPlaneDistance * 2F);
		AngelicaCompat.switchToOrtho(er, f, er.farPlaneDistance * 2F);
		stop();
	}
	
	@HookReplacer(targetMethod = "setupCameraTransform", mandatoryGroups = "orthoProjection")
	public static void setupCameraTransformOF(EntityRenderer er, float f, int i) {
		startFROM();
		Project.gluPerspective(FSKIP(), FSKIP(), FSKIP(), FLOAD("4"));
		startTO();
		Project.gluPerspective(FSKIP(), FSKIP(), FSKIP(), FLOAD("4"));
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
		HookReplacer.Replacer.<String>ASKIP().toLowerCase();
		startTO();
		stop();
		
		return null;
	}
	
	@HookReplacer(removePop = true)
	public static Set<String> getResourceDomains(FolderResourcePack frp) {
		startFROM();
		HookReplacer.Replacer.<String>ASKIP().toLowerCase();
		startTO();
		stop();
		
		return null;
	}
	
	
	// String -> NBTTagByteArray fix
	@HookReplacer(targetMethod = "func_150489_a", removePop = true)
	public static NBTBase fixSingleElement(JsonToNBT.Primitive primitive) {
		startFROM();
		new NBTTagIntArray(new int[] {Integer.parseInt(HookReplacer.Replacer.<String>ALOAD("1").trim())}); // TODO VARANY
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
	
	
	@SuppressWarnings("ALL")
	@HookReplacer(removePop = true)
	public static void func_148213_a(GuiStats.Stats gui, StatCrafting stat, int x, int y) {
		startFROM();
		("" + I18n.format(HookReplacer.Replacer.<Item>ALOAD("4").getUnlocalizedName() + ".name", new Object[0])).trim();
		startTO();
		new ItemStack(HookReplacer.Replacer.<Item>ALOAD("4")).getDisplayName();
		stop();
	}
	
	// Idea from Factorization by purpleposeidon, fixes this (in a proper way):
	// Place 7 blocks in a tower. On the edge of the top block, place another block.
	// Place a slab on the ground below it. Look up, try to place a block against the top one.
	@HookReplacer(removePop = true, isMandatory = false)
	public static void processPlayerBlockPlacement(NetHandlerPlayServer nhps, C08PacketPlayerBlockPlacement packet) {
		startFROM();
		nhps.playerEntity.getDistanceSq((double) ILOAD("6") + 0.5D, (double) ILOAD("7") + 0.5D, (double) ILOAD("8") + 0.5D); // TODO VARANY
		startTO();
		playerEyeBlockDistanceSq(ILOAD("6"), ILOAD("7"), ILOAD("8"), nhps.playerEntity);
		stop();
	}

	public static double playerEyeBlockDistanceSq(double x, double y, double z, EntityPlayerMP player) {
		double d3 = player.posX - x;
		double d4 = (player.posY + player.eyeHeight) - y;
		double d5 = player.posZ - z;
		return d3 * d3 + d4 * d4 + d5 * d5;
	}

	@HookReplacer(removePop = true)
	public static List<ModContainer> discover(JarDiscoverer jd, ModCandidate candidate, ASMDataTable table) {
		startFROM();
		HookReplacer.Replacer.<String>ASKIP().startsWith("__MACOSX");
		startTO();
		shouldIgnoreJarEntry(ASKIP());
		stop();

		return null;
	}

	public static boolean shouldIgnoreJarEntry(String name) {
		return name.startsWith("__MACOSX") || name.startsWith("META-INF/versions") || name.endsWith("module-info.class") || name.endsWith(".kotlin_module");
	}
}
//@formatter:on