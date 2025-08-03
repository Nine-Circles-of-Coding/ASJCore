package alexsocol.patcher.asm.hook;

import alexsocol.patcher.PatcherConfigHandler;
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
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import java.io.File;
import java.util.Set;

import static com.KAIIIAK.classManipulators.HookReplacer.Replacer.*;

@SuppressWarnings("ALL")
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
			d0 = Math.pow(nhps.playerEntity.theItemInWorldManager.getBlockReachDistance(), 2);
			stop();
		}
	}
	
	@HookReplacer // fucking Crucible bitches overwriting my changes so post-transforming -_-
	public static double getBlockReachDistance(ItemInWorldManager iiwm) {
		startFROM();
		POPLine();POP(iiwm.blockReachDistance);
		POPLine();startTO();
		POPLine();POP(iiwm.thisPlayerMP.getEntityAttribute(PlayerReachDistanceHandler.INSTANCE.getReachDistance()).getAttributeValue());
		POPLine();stop();
		
		return 0;
	}
	
	@HookReplacer // fucking Crucible bitches overwriting my changes so post-transforming -_-
	public static void setBlockReachDistance(ItemInWorldManager iiwm, double distance) {
		startFROM();
		POPLine();iiwm.blockReachDistance = distance;
		POPLine();startTO();
		POPLine();iiwm.thisPlayerMP.getEntityAttribute(PlayerReachDistanceHandler.INSTANCE.getReachDistance()).setBaseValue(distance);
		POPLine();stop();
	}
	
	
	// Perspective vs Ortho proj config
	@HookReplacer
	public static void setupCameraTransform(EntityRenderer er, float f, int i) {
		startFROM();
		POPLine();Project.gluPerspective(er.getFOVModifier(f, true), (float)er.mc.displayWidth / (float)er.mc.displayHeight, 0.05F, er.farPlaneDistance * 2F);
		POPLine();startTO();
		POPLine();selectProjection(er, f);
		POPLine();stop();
	}
	
	public static void selectProjection(EntityRenderer er, float f) {
		if (PatcherConfigHandler.INSTANCE.getOrthoProjection()) {
			double mod = er.getFOVModifier(f, true) * 2;
			GL11.glOrtho(er.mc.displayWidth / -mod, er.mc.displayWidth / mod, er.mc.displayHeight / -mod, er.mc.displayHeight / mod, 0.05F, er.farPlaneDistance * 2F);
		} else {
			Project.gluPerspective(er.getFOVModifier(f, true), (float)er.mc.displayWidth / (float)er.mc.displayHeight, 0.05F, er.farPlaneDistance * 2F);
		}
	}
	
	// bind smooth camera key
	@HookReplacer(targetMethod = "<init>")
	public static void GameSettings(GameSettings thiz) {
		startFROM();
		POPLine();POP("key.smoothCamera");POP(0);
		POPLine();startTO();
		POPLine();POP("key.smoothCamera");POP(Keyboard.KEY_F8);
		POPLine();stop();
	}
	
	@HookReplacer(targetMethod = "<init>")
	public static void GameSettings(GameSettings thiz, Minecraft mc, File options) {
		startFROM();
		POPLine();POP("key.smoothCamera");POP(0);
		POPLine();startTO();
		POPLine();POP("key.smoothCamera");POP(Keyboard.KEY_F8);
		POPLine();stop();
	}
	
	@HookReplacer(targetMethod = "<init>")
	public static void ModelCreeper(ModelCreeper thiz, float size) {
		startFROM();
		POPLine();{byte b0 = 4;}
		POPLine();startTO();
		POPLine();{byte b0 = 6;}
		POPLine();stop();
	}
	
	@HookReplacer
	public static Set getResourceDomains(FileResourcePack frp) {
		startFROM();
		POPLine();POP(HookReplacer.Replacer.<String>ALOAD("7").toLowerCase());
		POPLine();startTO();
		POPLine();POP(ALOAD("7").toString());
		POPLine();stop();
		
		return null;
	}
	
	@HookReplacer
	public static Set getResourceDomains(FolderResourcePack frp) {
		startFROM();
		POPLine();POP(HookReplacer.Replacer.<String>ALOAD("7").toLowerCase());
		POPLine();startTO();
		POPLine();POP(ALOAD("7").toString());
		POPLine();stop();
		
		return null;
	}
}