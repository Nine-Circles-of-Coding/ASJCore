package alexsocol.patcher.asm.hook;

import com.KAIIIAK.classManipulators.HookReplacer;
import static com.KAIIIAK.classManipulators.HookReplacer.Replacer.*;

import net.minecraft.client.gui.GuiScreenRealmsProxy;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.realms.DisconnectedOnlineScreen;
import net.minecraft.util.IChatComponent;

public class RealmsDeleterHR {
	
	@HookReplacer(targetMethod = "onDisconnect")
	public static void onDisconnect1(NetHandlerPlayClient nhpc, IChatComponent reason) {
		startFROM();
		POPLine();POP(nhpc.guiScreenServer instanceof GuiScreenRealmsProxy);
		POPLine();startTO();
		POPLine();POP(false);
		POPLine();stop();
	}
	
	@HookReplacer(targetMethod = "onDisconnect")
	public static void onDisconnect2(NetHandlerPlayClient nhpc, IChatComponent reason) {
		startFROM();
		POPLine();POP((new DisconnectedOnlineScreen(((GuiScreenRealmsProxy)nhpc.guiScreenServer).func_154321_a(), "disconnect.lost", reason)).getProxy());
		POPLine();startTO();
		POPLine();POP(null);
		POPLine();stop();
	}
}
