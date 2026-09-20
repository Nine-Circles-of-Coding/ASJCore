@file:Suppress("unused")

package alexsocol.mixins.client

import alexsocol.patcher.asm.hook.ASJHookHandler
import cpw.mods.fml.client.FMLClientHandler
import cpw.mods.fml.client.GuiModList
import net.minecraft.block.BlockDirt
import net.minecraft.block.BlockTallGrass
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.GuiIngameMenu
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiSnooper
import net.minecraft.client.gui.GuiCreateWorld
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.settings.GameSettings
import net.minecraft.util.MouseHelper
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityFurnace
import net.minecraft.util.ResourceLocation
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.io.File

/*
 * Client-side game, GUI and settings fixes.
 *
 * BlockTallGrass / BlockDirt / TileEntityFurnace are common classes, but the methods hooked here are
 * @SideOnly(CLIENT) in vanilla, so these mixins are registered client-only.
 */

@Mixin(Minecraft::class)
abstract class MixinMinecraft {
	
	/** Target returns void, so `ON_TRUE` meant "skip the vanilla body". */
	@Inject(method = ["displayGuiScreen"], at = [At("HEAD")], cancellable = true)
	fun asjDisplayGuiScreen(gui: GuiScreen?, ci: CallbackInfo?) {
		if (ASJHookHandler.displayGuiScreen(this as Any as Minecraft, gui)) ci?.cancel()
	}
	
	@Inject(method = ["toggleFullscreen"], at = [At("RETURN")])
	fun asjToggleFullscreen(ci: CallbackInfo?) {
		ASJHookHandler.toggleFullscreen(this as Any as Minecraft)
	}
	
	@Inject(method = ["isSnooperEnabled"], at = [At("HEAD")], cancellable = true)
	fun asjIsSnooperEnabled(cir: CallbackInfoReturnable<Boolean>?) {
		cir?.returnValue = false
	}
	
	@Inject(method = ["displayDebugInfo"], at = [At("HEAD")])
	fun asjDisplayDebugInfoPre(deltaTime: Long, ci: CallbackInfo?) {
		ASJHookHandler.displayDebugInfoPre(this as Any as Minecraft, deltaTime)
	}
	
	@Inject(method = ["displayDebugInfo"], at = [At("RETURN")])
	fun asjDisplayDebugInfoPost(deltaTime: Long, ci: CallbackInfo?) {
		ASJHookHandler.displayDebugInfoPost(this as Any as Minecraft, deltaTime)
	}
}

@Mixin(MouseHelper::class)
abstract class MixinMouseHelper {
	
	@Inject(method = ["mouseXYChange"], at = [At("RETURN")])
	fun asjMouseXYChange(ci: CallbackInfo?) {
		ASJHookHandler.mouseXYChange(this as Any as MouseHelper)
	}
}

@Mixin(EntityPlayerSP::class)
abstract class MixinEntityPlayerSP {
	
	@Inject(method = ["onLivingUpdate"], at = [At("HEAD")])
	fun asjOnLivingUpdate(ci: CallbackInfo?) {
		ASJHookHandler.onLivingUpdate(this as Any as EntityPlayerSP)
	}
}

@Mixin(GameSettings::class)
abstract class MixinGameSettings {
	
	@Inject(method = ["<init>()V"], at = [At("RETURN")])
	fun asjGameSettingsInit(ci: CallbackInfo?) {
		ASJHookHandler.GameSettings(this as Any as GameSettings)
	}
	
	@Inject(method = ["<init>(Lnet/minecraft/client/Minecraft;Ljava/io/File;)V"], at = [At("RETURN")])
	fun asjGameSettingsInitFull(mc: Minecraft?, file: File?, ci: CallbackInfo?) {
		ASJHookHandler.GameSettings(this as Any as GameSettings, mc, file)
	}
	
	@Inject(method = ["loadOptions"], at = [At("RETURN")])
	fun asjLoadOptionsPost(ci: CallbackInfo?) {
		ASJHookHandler.loadOptionsPost(this as Any as GameSettings)
	}
	
	/** Target returns void, so `ON_TRUE` meant "skip the vanilla body". */
	@Inject(method = ["setOptionValue"], at = [At("HEAD")], cancellable = true)
	fun asjSetOptionValue(option: GameSettings.Options?, value: Int, ci: CallbackInfo?) {
		if (ASJHookHandler.setOptionValue(this as Any as GameSettings, option, value)) ci?.cancel()
	}
}

/** FML class: no SRG names of its own, so `remap = false`. Both targets return void. */
@Mixin(value = [FMLClientHandler::class], remap = false)
abstract class MixinFMLClientHandler {
	
	@Inject(method = ["trackBrokenTexture"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjTrackBrokenTexture(resourceLocation: ResourceLocation, error: String?, ci: CallbackInfo?) {
		if (ASJHookHandler.trackBrokenTexture(this as Any as FMLClientHandler, resourceLocation, error)) ci?.cancel()
	}
	
	@Inject(method = ["showInGameModOptions"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjShowInGameModOptions(guiIngameMenu: GuiIngameMenu?, ci: CallbackInfo?) {
		if (ASJHookHandler.showInGameModOptions(this as Any as FMLClientHandler, guiIngameMenu)) ci?.cancel()
	}
}

@Mixin(GuiCreateWorld::class)
abstract class MixinGuiCreateWorld {
	
	@Inject(method = ["actionPerformed"], at = [At("HEAD")], cancellable = true)
	fun asjActionPerformed(button: GuiButton, ci: CallbackInfo?) {
		if (ASJHookHandler.actionPerformed(this as Any as GuiCreateWorld, button)) ci?.cancel()
	}
}

@Mixin(GuiContainer::class)
abstract class MixinGuiContainer {
	
	@Inject(method = ["func_146977_a"], at = [At("HEAD")])
	fun asjDrawSlot(slot: Slot, ci: CallbackInfo?) {
		ASJHookHandler.func_146977_a(this as Any as GuiContainer, slot)
	}
}

@Mixin(GuiOptions::class)
abstract class MixinGuiOptions {
	
	@Inject(method = ["initGui"], at = [At("RETURN")])
	fun asjInitGui(ci: CallbackInfo?) {
		ASJHookHandler.initGui(this as Any as GuiOptions)
	}
}

@Mixin(GuiSnooper::class)
abstract class MixinGuiSnooper {
	
	@Inject(method = ["initGui"], at = [At("RETURN")])
	fun asjInitGui(ci: CallbackInfo?) {
		ASJHookHandler.initGui(this as Any as GuiSnooper)
	}
}

@Mixin(GuiModList::class)
abstract class MixinGuiModList {
	
	@Inject(method = ["initGui"], at = [At("RETURN")])
	fun asjInitGui(ci: CallbackInfo?) {
		ASJHookHandler.initGui(this as Any as GuiModList)
	}
}

@Mixin(BlockTallGrass::class)
abstract class MixinBlockTallGrass {
	
	@Inject(method = ["getSubBlocks"], at = [At("HEAD")])
	fun asjGetSubBlocksPre(item: Item?, tab: CreativeTabs?, list: MutableList<ItemStack?>, ci: CallbackInfo?) {
		ASJHookHandler.getSubBlocksPre(this as Any as BlockTallGrass, item, tab, list)
	}
	
	@Inject(method = ["getSubBlocks"], at = [At("RETURN")])
	fun asjGetSubBlocksPost(item: Item?, tab: CreativeTabs?, list: MutableList<ItemStack?>, ci: CallbackInfo?) {
		ASJHookHandler.getSubBlocksPost(this as Any as BlockTallGrass, item, tab, list)
	}
}

@Mixin(BlockDirt::class)
abstract class MixinBlockDirt {
	
	@Inject(method = ["getSubBlocks"], at = [At("RETURN")])
	fun asjGetSubBlocks(item: Item?, tab: CreativeTabs?, list: MutableList<ItemStack?>, ci: CallbackInfo?) {
		ASJHookHandler.getSubBlocks(this as Any as BlockDirt, item, tab, list)
	}
}

@Mixin(TileEntityFurnace::class)
abstract class MixinTileEntityFurnace {
	
	@Inject(method = ["getBurnTimeRemainingScaled"], at = [At("HEAD")], cancellable = true)
	fun asjGetBurnTimeRemainingScaled(mod: Int, cir: CallbackInfoReturnable<Int>?) {
		cir?.returnValue = ASJHookHandler.getBurnTimeRemainingScaled(this as Any as TileEntityFurnace, mod)
	}
}
