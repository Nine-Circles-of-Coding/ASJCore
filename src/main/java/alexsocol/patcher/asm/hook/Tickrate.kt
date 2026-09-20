@file:Suppress("unused")

package alexsocol.patcher.asm.hook

import alexsocol.patcher.*
import com.KAIIIAK.classManipulators.*
import com.KAIIIAK.classManipulators.HookReplacer.*
import com.KAIIIAK.classManipulators.HookReplacer.Replacer.*
import com.google.common.collect.*
import io.github.crucible.*
import net.minecraft.client.*
import net.minecraft.server.*
import net.minecraft.util.*
import net.tclproject.mysteriumlib.asm.fixes.MysteriumPatchesFixesMagicka
import java.io.*
import java.net.*

@CreateHRG(name = "tickrate")
@HookReplacer(targetMethod = "run", mandatoryGroups = ["tickrate"])
fun replaceTickRateVanilla(ms: MinecraftServer) {
    startFROM()
    POP(50L)
    startTO()
    POP(getMsPT())
    stop()
}

@HookReplacer(targetMethod = "run", mandatoryGroups = ["tickrate"])
fun replaceTickRateCauldron(ms: MinecraftServer) {
    startFROM()
    POP(50_000_000L)
    startTO()
    POP(getNsPT())
    stop()
}

@HookReplacer(targetMethod = "run", mandatoryGroups = ["tickrate"])
fun replaceTickRateCrucible(ms: MinecraftServer) {
    startFROM()
    POP(CrucibleConfigs.configs.tickTime)
    startTO()
    POP(getNsPTi())
    stop()
}

@HookReplacer(targetMethod = "run", mandatoryGroups = ["tickrate"])
fun replaceTickRateMysteriumLibInArsMagica(ms: MinecraftServer) {
    startFROM()
    POP(MysteriumPatchesFixesMagicka.servertickrate)
    startTO()
    POP(getMsPT())
    stop()
}

@HookReplacer(targetMethod = "<init>")
fun replaceTickRate(mc: Minecraft, session: Session?, displayWidth: Int, displayHeight: Int, fullscreen: Boolean, isDemo: Boolean, mcDataDir: File?, fileAssets: File?, fileResourcepacks: File?, proxy: Proxy?, launchedVersion: String?, launchArgs: Multimap<*, *>?, mcDefaultResourcePackIndex: String?) {
    startFROM()
    POP(20f)
    startTO()
    POP(getTPS())
    stop()
}

@HookReplacer // in case someone (Mysterium) changes timer
fun updateTimer(timer: Timer) {
    startFROM()
    POP(timer.ticksPerSecond)
    startTO()
    POP(getTPS())
    stop()
}

fun getTPS(): Float = PatcherConfigHandler.tps
fun getMsPT(): Long = PatcherConfigHandler.msPerTick.toLong()
fun getNsPT(): Long = PatcherConfigHandler.msPerTick * 1_000_000L
fun getNsPTi(): Int = PatcherConfigHandler.msPerTick * 1_000_000