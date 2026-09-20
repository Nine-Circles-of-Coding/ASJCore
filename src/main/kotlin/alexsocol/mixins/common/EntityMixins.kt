@file:Suppress("unused")

package alexsocol.mixins.common

import alexsocol.patcher.asm.hook.ASJHookHandler
import net.minecraft.entity.DataWatcher
import net.minecraft.entity.DataWatcher.WatchableObject
import net.minecraft.entity.Entity
import net.minecraft.entity.monster.EntityCreeper
import net.minecraft.entity.monster.EntityGhast
import net.minecraft.entity.monster.EntityIronGolem
import net.minecraft.world.World
import net.minecraft.world.biome.BiomeGenBase
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

/*
 * Entity and world-state fixes. All targets are vanilla, so everything here remaps.
 *
 * The two `<clinit>` replacements in this slice (EntityList, EntityEnderman) are Java and live in
 * their own files, because Mixin needs a static handler for a static target.
 */

@Mixin(BiomeGenBase::class)
abstract class MixinBiomeGenBase {

	/**
	 * Was a plain `@Hook(targetMethod = "<init>")`, i.e. injected at HEAD. Mixin forbids HEAD in a
	 * constructor, and RETURN is equivalent here: the body only appends `this` to a static list.
	 */
	@Inject(method = ["<init>(IZ)V"], at = [At("RETURN")])
	fun asjBiomeGenBaseInit(id: Int, register: Boolean, ci: CallbackInfo?) {
		ASJHookHandler.BiomeGenBase(this as Any as BiomeGenBase, id, register)
	}
}

/** Works around mods that read data watcher slot 0 with the wrong accessor. */
@Mixin(DataWatcher::class)
abstract class MixinDataWatcher {

	/** Was `@Hook(returnCondition = ON_TRUE, returnAnotherMethod = "getWatchableObjectByteBody")`. */
	@Inject(method = ["getWatchableObjectByte"], at = [At("HEAD")], cancellable = true)
	fun asjGetWatchableObjectByte(index: Int, cir: CallbackInfoReturnable<Byte>?) {
		val dw = this as Any as DataWatcher
		if (ASJHookHandler.getWatchableObjectByte(dw, index))
			cir?.returnValue = ASJHookHandler.getWatchableObjectByteBody(dw, index)
	}

	@Inject(method = ["getWatchableObjectInt"], at = [At("HEAD")], cancellable = true)
	fun asjGetWatchableObjectInt(index: Int, cir: CallbackInfoReturnable<Int>?) {
		val dw = this as Any as DataWatcher
		if (ASJHookHandler.getWatchableObjectInt(dw, index))
			cir?.returnValue = ASJHookHandler.getWatchableObjectIntBody(dw, index)
	}
}

@Mixin(WatchableObject::class)
abstract class MixinWatchableObject {

	@Inject(method = ["<init>(IILjava/lang/Object;)V"], at = [At("RETURN")])
	fun asjWatchableObjectInit(objectType: Int, dataValueId: Int, watchedObject: Any?, ci: CallbackInfo?) {
		ASJHookHandler.`WatchableObject$init`(this as Any as WatchableObject, objectType, dataValueId, watchedObject)
	}

	@Inject(method = ["setObject"], at = [At("RETURN")])
	fun asjSetObject(watchedObject: Any?, ci: CallbackInfo?) {
		ASJHookHandler.setObject(this as Any as WatchableObject, watchedObject)
	}
}

/** Gives the iron golem an attack cooldown instead of letting it hit every tick. */
@Mixin(EntityIronGolem::class)
abstract class MixinEntityIronGolem {

	@Inject(method = ["entityInit"], at = [At("RETURN")])
	fun asjEntityInit(ci: CallbackInfo?) {
		ASJHookHandler.entityInit(this as Any as EntityIronGolem)
	}

	@Inject(method = ["onLivingUpdate"], at = [At("RETURN")])
	fun asjOnLivingUpdate(ci: CallbackInfo?) {
		ASJHookHandler.onLivingUpdate(this as Any as EntityIronGolem)
	}

	/** Was `ON_TRUE` with `booleanReturnConstant = false`, so the constant is the return value. */
	@Inject(method = ["attackEntityAsMob"], at = [At("HEAD")], cancellable = true)
	fun asjAttackEntityAsMobPre(target: Entity?, cir: CallbackInfoReturnable<Boolean>?) {
		if (ASJHookHandler.attackEntityAsMobPre(this as Any as EntityIronGolem, target)) cir?.returnValue = false
	}

	@Inject(method = ["attackEntityAsMob"], at = [At("RETURN")])
	fun asjAttackEntityAsMobPost(target: Entity?, cir: CallbackInfoReturnable<Boolean>?) {
		ASJHookHandler.attackEntityAsMobPost(this as Any as EntityIronGolem, target)
	}
}

@Mixin(EntityCreeper::class)
abstract class MixinEntityCreeper {

	@Inject(method = ["<init>(Lnet/minecraft/world/World;)V"], at = [At("RETURN")])
	fun asjEntityCreeperInit(world: World?, ci: CallbackInfo?) {
		ASJHookHandler.EntityCreeper(this as Any as EntityCreeper, world)
	}
}

@Mixin(EntityGhast::class)
abstract class MixinEntityGhast {

	/** Was `@Hook(returnCondition = ALWAYS)`. */
	@Inject(method = ["getSoundVolume"], at = [At("HEAD")], cancellable = true)
	fun asjGetSoundVolume(cir: CallbackInfoReturnable<Float>?) {
		cir?.returnValue = ASJHookHandler.getSoundVolume(this as Any as EntityGhast)
	}
}

/** Both overloads return void, so `ON_TRUE` meant "skip the vanilla body". */
@Mixin(World::class)
abstract class MixinWorldNeighbors {

	@Inject(method = ["notifyBlocksOfNeighborChange(IIILnet/minecraft/block/Block;)V"], at = [At("HEAD")], cancellable = true)
	fun asjNotifyBlocksOfNeighborChange(x: Int, y: Int, z: Int, block: net.minecraft.block.Block?, ci: CallbackInfo?) {
		if (ASJHookHandler.notifyBlocksOfNeighborChange(this as Any as World, x, y, z, block)) ci?.cancel()
	}

	@Inject(method = ["notifyBlocksOfNeighborChange(IIILnet/minecraft/block/Block;I)V"], at = [At("HEAD")], cancellable = true)
	fun asjNotifyBlocksOfNeighborChangeSided(x: Int, y: Int, z: Int, block: net.minecraft.block.Block?, side: Int, ci: CallbackInfo?) {
		if (ASJHookHandler.notifyBlocksOfNeighborChange(this as Any as World, x, y, z, block, side)) ci?.cancel()
	}
}
