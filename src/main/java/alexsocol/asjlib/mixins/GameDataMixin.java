package alexsocol.asjlib.mixins;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameData.class)
public interface GameDataMixin  {
    @Invoker("getMain")
    static GameData invokeGetMain() {
        throw new AssertionError();
    }

    @Invoker("registerItem")
    int invokeRegisterItem(Item item, String name, int idHint);
}
