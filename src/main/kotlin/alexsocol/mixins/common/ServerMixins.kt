@file:Suppress("unused")

package alexsocol.mixins.common

import alexsocol.patcher.asm.hook.ASJHookHandler
import alexsocol.patcher.duck.IMultiPlayerAware
import com.llamalad7.mixinextras.injector.ModifyReturnValue
import cpw.mods.fml.common.network.handshake.NetworkDispatcher
import cpw.mods.fml.common.registry.LanguageRegistry
import net.minecraft.block.BlockPortal
import net.minecraft.block.BlockRotatedPillar
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.ContainerEnchantment
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.profiler.PlayerUsageSnooper
import net.minecraft.server.MinecraftServer
import net.minecraft.server.management.ItemInWorldManager
import net.minecraft.world.GameRules
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.storage.AnvilChunkLoader
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

/*
 * Server-side and world-persistence fixes.
 *
 * Note the two FML targets (NetworkDispatcher, LanguageRegistry): FML classes have no SRG names, so
 * those injectors are remap = false.
 */

/**
 * Carries [IMultiPlayerAware] onto every server. The interface's default throws, exactly as the old
 * spliced-in abstract method did; [MixinIntegratedServerMP] and [MixinDedicatedServerMP] answer it.
 */
@Mixin(MinecraftServer::class)
abstract class MixinMinecraftServer: IMultiPlayerAware {

	/** Was `@Hook(returnCondition = ALWAYS)` returning a constant `false`. */
	@Inject(method = ["isSnooperEnabled"], at = [At("HEAD")], cancellable = true)
	fun asjIsSnooperEnabled(cir: CallbackInfoReturnable<Boolean>?) {
		cir?.returnValue = false
	}
}

@Mixin(PlayerUsageSnooper::class)
abstract class MixinPlayerUsageSnooper {

	@Inject(method = ["startSnooper"], at = [At("RETURN")])
	fun asjStartSnooper(ci: CallbackInfo?) {
		ASJHookHandler.startSnooper(this as Any as PlayerUsageSnooper)
	}
}

@Mixin(ItemInWorldManager::class)
abstract class MixinItemInWorldManager {

	/** Was `ON_TRUE` with `booleanReturnConstant = false`: refuse to use an empty stack. */
	@Inject(method = ["tryUseItem"], at = [At("HEAD")], cancellable = true)
	fun asjTryUseItem(player: EntityPlayer?, world: World?, stack: ItemStack, cir: CallbackInfoReturnable<Boolean>?) {
		if (ASJHookHandler.tryUseItem(this as Any as ItemInWorldManager, player, world, stack)) cir?.returnValue = false
	}
}

@Mixin(NetworkDispatcher::class)
abstract class MixinNetworkDispatcher {

	/**
	 * Was `injectOnExit` + `ON_TRUE` + `intReturnConstant = 0` with an `@ReturnValue`: a player in a
	 * dimension that no longer exists gets sent to dimension 0 instead of failing the handshake.
	 */
	@ModifyReturnValue(method = ["serverInitiateHandshake"], at = [At("RETURN")], remap = false)
	fun asjServerInitiateHandshake(original: Int): Int =
		if (ASJHookHandler.serverInitiateHandshake(this as Any as NetworkDispatcher, original)) 0 else original
}

@Mixin(LanguageRegistry::class)
abstract class MixinLanguageRegistry {

	/** FML class, and the target returns void, so `ON_TRUE` meant "skip it". */
	@Inject(method = ["injectLanguage"], at = [At("HEAD")], cancellable = true, remap = false)
	fun asjInjectLanguage(lang: String, parsedLangFile: HashMap<String, String>, ci: CallbackInfo?) {
		if (ASJHookHandler.injectLanguage(this as Any as LanguageRegistry, lang, parsedLangFile)) ci?.cancel()
	}
}

/** WorldEngine sub-biome persistence. Both targets are private, which Mixin handles fine. */
@Mixin(AnvilChunkLoader::class)
abstract class MixinAnvilChunkLoader {

	@Inject(method = ["writeChunkToNBT"], at = [At("RETURN")])
	fun asjWriteChunkToNBT(chunk: Chunk, world: World, nbt: NBTTagCompound, ci: CallbackInfo?) {
		ASJHookHandler.writeChunkToNBT(this as Any as AnvilChunkLoader, chunk, world, nbt)
	}

	@ModifyReturnValue(method = ["readChunkFromNBT"], at = [At("RETURN")])
	fun asjReadChunkFromNBT(original: Chunk, world: World, nbt: NBTTagCompound): Chunk =
		ASJHookHandler.readChunkFromNBT(this as Any as AnvilChunkLoader, world, nbt, original)
}

@Mixin(BlockPortal::class)
abstract class MixinBlockPortal {

	@Inject(method = ["func_150000_e"], at = [At("HEAD")], cancellable = true)
	fun asjTryToCreatePortal(world: World, x: Int, y: Int, z: Int, cir: CallbackInfoReturnable<Boolean>?) {
		if (ASJHookHandler.tryToCreatePortal(this as Any as BlockPortal, world, x, y, z)) cir?.returnValue = true
	}
}

@Mixin(BlockRotatedPillar::class)
abstract class MixinBlockRotatedPillar {

	/** Was `ON_TRUE` with `returnAnotherMethod = "placeAllsided"`: keeps all-sided log rotation. */
	@Inject(method = ["onBlockPlaced"], at = [At("HEAD")], cancellable = true)
	fun asjOnBlockPlaced(world: World?, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float, hitZ: Float, meta: Int, cir: CallbackInfoReturnable<Int>?) {
		val block = this as Any as BlockRotatedPillar
		if (ASJHookHandler.onBlockPlaced(block, world, x, y, z, side, hitX, hitY, hitZ, meta))
			cir?.returnValue = ASJHookHandler.placeAllsided(block, world, x, y, z, side, hitX, hitY, hitZ, meta)
	}
}

@Mixin(ContainerEnchantment::class)
abstract class MixinContainerEnchantment {

	@Inject(method = ["<init>(Lnet/minecraft/entity/player/InventoryPlayer;Lnet/minecraft/world/World;III)V"], at = [At("RETURN")])
	fun asjContainerEnchantmentInit(inv: InventoryPlayer?, world: World?, x: Int, y: Int, z: Int, ci: CallbackInfo?) {
		ASJHookHandler.ContainerEnchantment(this as Any as ContainerEnchantment, inv, world, x, y, z)
	}
}

@Mixin(GameRules::class)
abstract class MixinGameRules {

	@Inject(method = ["<init>()V"], at = [At("RETURN")])
	fun asjGameRulesInit(ci: CallbackInfo?) {
		ASJHookHandler.`GameRules$init`(this as Any as GameRules)
	}
}
