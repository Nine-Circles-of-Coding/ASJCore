package alexsocol.patcher.asm.hook;

import alexsocol.asjlib.asm.HookField;
import net.minecraft.entity.player.EntityPlayer;

@SuppressWarnings("ALL")
public class ASJFieldHookHandler {
	
	@HookField(targetClassName = "net.minecraft.util.FoodStats")
	public EntityPlayer ASJCore_host;

	@HookField(targetClassName = "net.minecraft.world.chunk.Chunk")
	public String[] WorldEngine_SubBiomeList;
}
