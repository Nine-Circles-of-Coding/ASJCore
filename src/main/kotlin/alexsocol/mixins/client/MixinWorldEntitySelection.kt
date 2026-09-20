package alexsocol.mixins.client

import alexsocol.patcher.asm.hook.NoEntityInteractionHandler
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.world.World
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

/**
 * The client half of the no-entity-interaction toggle. [World] is a common class, but this mixin is
 * registered under `"client"` on purpose: [NoEntityInteractionHandler] is `@SideOnly(Side.CLIENT)`,
 * so on a dedicated server the method this calls is stripped and the injection would blow up with a
 * [NoSuchMethodError]. The old `@Hook` was client-only for the same reason.
 */
@Mixin(World::class)
abstract class MixinWorldEntitySelection {

	/**
	 * Was `@Hook(returnCondition = ON_NOT_NULL)`. The descriptor is spelled out because Forge adds a
	 * three-argument `IEntitySelector` overload of this method.
	 */
	@Inject(
		method = ["getEntitiesWithinAABBExcludingEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;"],
		at = [At("HEAD")],
		cancellable = true
	)
	fun asjGetEntitiesWithinAABBExcludingEntity(from: Entity?, aabb: AxisAlignedBB?, cir: CallbackInfoReturnable<List<*>>?) {
		NoEntityInteractionHandler.getEntitiesWithinAABBExcludingEntity(this as Any as World, from, aabb)
			?.let { cir?.returnValue = it }
	}
}
