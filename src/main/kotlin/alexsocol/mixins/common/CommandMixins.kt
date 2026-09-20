@file:Suppress("unused")

package alexsocol.mixins.common

import alexsocol.patcher.asm.hook.ASJHookHandler
import net.minecraft.command.CommandDefaultGameMode
import net.minecraft.command.CommandGameMode
import net.minecraft.command.ICommandSender
import net.minecraft.command.CommandShowSeed
import net.minecraft.command.server.CommandSummon
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

/*
 * Command tweaks: short aliases for the gamemode commands, and a /summon that can place any entity.
 *
 * `getCommandAliases` and `getCommandUsage` were `createMethod = true` - neither command class
 * declares them, they inherit from CommandBase - so here they are plain un-annotated methods, which
 * Mixin merges into the target as overrides.
 */

@Mixin(CommandGameMode::class)
abstract class MixinCommandGameMode {

	open fun getCommandAliases(): List<String> = ASJHookHandler.getCommandAliases(this as Any as CommandGameMode)
}

@Mixin(CommandDefaultGameMode::class)
abstract class MixinCommandDefaultGameMode {

	open fun getCommandAliases(): List<String>? = ASJHookHandler.getCommandAliases(this as Any as CommandDefaultGameMode)
}

@Mixin(CommandSummon::class)
abstract class MixinCommandSummon {

	open fun getCommandUsage(sender: ICommandSender?): String = ASJHookHandler.getCommandUsage(this as Any as CommandSummon, sender)

	/** Target returns void, so `ON_TRUE` meant "skip the vanilla body". */
	@Inject(method = ["processCommand"], at = [At("HEAD")], cancellable = true)
	fun asjProcessCommand(sender: ICommandSender?, args: Array<String?>, ci: CallbackInfo?) {
		if (ASJHookHandler.processCommand(this as Any as CommandSummon, sender, args)) ci?.cancel()
	}

	@Inject(method = ["func_147182_d"], at = [At("HEAD")], cancellable = true)
	fun asjSummonableEntities(cir: CallbackInfoReturnable<Array<String>>?) {
		cir?.returnValue = ASJHookHandler.func_147182_d(this as Any as CommandSummon)
	}

	@Inject(method = ["addTabCompletionOptions"], at = [At("HEAD")], cancellable = true)
	fun asjAddTabCompletionOptions(sender: ICommandSender?, args: Array<String?>, cir: CallbackInfoReturnable<MutableList<*>>?) {
		ASJHookHandler.addTabCompletionOptions(this as Any as CommandSummon, sender, args)?.let { cir?.returnValue = it }
	}
}

@Mixin(CommandShowSeed::class)
abstract class MixinCommandShowSeed {

	@Inject(method = ["processCommand"], at = [At("RETURN")])
	fun asjProcessCommand(sender: ICommandSender, args: Array<String?>?, ci: CallbackInfo?) {
		ASJHookHandler.processCommand(this as Any as CommandShowSeed, sender, args)
	}
}
