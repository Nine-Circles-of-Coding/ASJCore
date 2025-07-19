package alexsocol.patcher.asm.hook;

import com.KAIIIAK.classManipulators.HookReplacer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.MathHelper;

import static com.KAIIIAK.classManipulators.HookReplacer.Replacer.*;
import static com.KAIIIAK.classManipulators.HookReplacer.Replacer.stop;

public class CapeRotationsFix {
	
	@HookReplacer(targetMethod = "renderEquippedItems")
	@SideOnly(Side.CLIENT)
	public static void fixCapeAxisX(RenderPlayer render, AbstractClientPlayer player, float ticks) {
		startFROM();
		POPLine();POP(6F + FLOAD("20"/*19+1*/) / 2F + FLOAD("19"/*18+1*/));
		POPLine();startTO();
		POPLine();POP(MathHelper.clamp_float(6F + FLOAD("20"/*19+1*/) / 2F + FLOAD("19"/*18+1*/), 0f, 90f));
		POPLine();stop();
	}
	
	@HookReplacer(targetMethod = "renderEquippedItems")
	@SideOnly(Side.CLIENT)
	public static void fixCapeAxisZ(RenderPlayer render, AbstractClientPlayer player, float ticks) {
		startFROM();
		POPLine();POP(FLOAD("21"/*20+1*/) / 2F);
		POPLine();startTO();
		POPLine();POP(MathHelper.clamp_float(FLOAD("21"/*20+1*/) / 2F, -45f, 45f));
		POPLine();stop();
	}
	
	@HookReplacer(targetMethod = "renderEquippedItems")
	@SideOnly(Side.CLIENT)
	public static void fixCapeAxisY(RenderPlayer render, AbstractClientPlayer player, float ticks) {
		startFROM();
		POPLine();POP(-FLOAD("21"/*20+1*/) / 2F);
		POPLine();startTO();
		POPLine();POP(MathHelper.clamp_float(-FLOAD("21"/*20+1*/) / 2F, -45f, 45f));
		POPLine();stop();
	}
}
