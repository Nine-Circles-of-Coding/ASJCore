package alexsocol.patcher.asm;

import alexsocol.asjlib.asm.HookField;
import net.minecraft.entity.player.EntityPlayer;

public class ASJFieldHookHandler {
	
	@HookField(targetClassName = "net.minecraft.util.FoodStats")
	public EntityPlayer host;

	@HookField(targetClassName = "net.minecraft.world.chunk.Chunk")
	public String[] WorldEngine_SubBiomeList;
}
