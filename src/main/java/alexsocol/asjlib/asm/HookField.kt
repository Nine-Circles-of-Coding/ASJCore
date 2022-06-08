package alexsocol.asjlib.asm

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
/**
 * WARNING! Incompatible with kotlin properties! Please, use ONLY java for classes with HookFields
 *
 * @param targetClassName full class name separated with dots
 */
annotation class HookField(val targetClassName: String)