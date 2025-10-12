package alexsocol.patcher.asm.hook;

import alexsocol.asjlib.ExtensionsKt;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;

import java.util.List;

//@SuppressWarnings("DataFlowIssue")
@SuppressWarnings("unused") // used in class transformer 
public class ItemCollisionFix { // TODO uncomment when HookReplacer has 'withRecalc=false' option
	
//	@HookReplacer(targetMethod = "func_145771_j")
//	public static boolean collisionsCheckFix(Entity entity, double x, double y, double z) {
//		startFROM();
//		POPLine();POP(HookReplacer.Replacer.<List<AxisAlignedBB>>ALOAD("16").isEmpty());
//		POPLine();startTO();
//		POPLine();POP(checkCollisions(entity, ALOAD("16")));
//		POPLine();stop();
//		
//		return false;
//	}
	
	public static boolean checkCollisions(Entity entity, List<AxisAlignedBB> list) {
		if (list.isEmpty()) return true;
		
		AxisAlignedBB bb = ExtensionsKt.boundingBox(entity, 0);
		for (AxisAlignedBB it : list)
			if (it.intersectsWith(bb))
				return false;
		
		return true;
	}
	
//	@HookReplacer(targetMethod = "func_145771_j")
//	public static boolean ignoreIfInSolidBlock(Entity entity, double x, double y, double z) {
//		startFROM();
//		POPLine();POP(entity.worldObj.func_147469_q(ILOAD("7"), ILOAD("8"), ILOAD("9")));
//		POPLine();startTO();
//		POPLine();POP(false);
//		POPLine();stop();
//
//		return false;
//	}
}
