package alexsocol.patcher.duck;

/**
 * Implemented on {@link net.minecraft.server.MinecraftServer} by {@code alexsocol.mixins.common.MixinMinecraftServer},
 * and answered for real by the {@code IntegratedServer} and {@code DedicatedServer} mixins.
 * <p>
 * Replaces a {@code @Hook(createMethod = true, isAbstract = true)} that spliced an abstract
 * {@code isMultiPlayer} into {@code MinecraftServer}. Mixin cannot merge an abstract method into a
 * target, but a Java 8 default serves the same purpose: the two concrete servers override it, and
 * anything else gets the same {@link AbstractMethodError} the spliced method would have thrown.
 * <p>
 * The name is kept as {@code isMultiPlayer} on purpose, in case anything resolves it reflectively.
 */
public interface IMultiPlayerAware {

	default boolean isMultiPlayer() {
		throw new AbstractMethodError();
	}
}
